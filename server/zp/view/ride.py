# imports
import datetime
from datetime import datetime
from decimal import getcontext

from django.db import transaction
from django.utils import timezone
from django.views.decorators.csrf import csrf_exempt

from url_magic import makeView
from ..models import Place, Trip, Progress, Rate
from ..models import User, Vehicle, Driver, Location
from ..utils import ZPException, HttpJSONResponse, googleDistAndTime
from ..utils import getOTP, log
from ..utils import getRoutePrice, getTripPrice, getRidePrice, getRiPrice
from ..utils import handleException, extractParams, checkAuth, checkTripStatus, retireEntity

import googlemaps
from django.conf import settings
from django.db.utils import OperationalError, IntegrityError

###########################################
# Types
Filename = str


###########################################
# Constants

makeView.APP_NAME = 'zp'


# ============================================================================
# Driver views
# ============================================================================



@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@transaction.atomic
@extractParams
def loginDriver(_, dct):
    '''
    Driver login
    makes the driver logun with phone number
    
    HTTP Args:
        pno: phone number of the driver without the ISD code
        key: auth rot 13 of driver
        

    Notes:
        Rot 13 is important
    '''

    log('Driver login request. Dct : %s ' % (str(dct)))

    sPhone = str(dct['pn'])
    from codecs import encode
    sAuth = encode(str(dct['key']), 'rot13')
    log('Driver login key request. Dct : %s ' % (str(sAuth)))
    qsDriver = Driver.objects.filter(auth=sAuth, pn=sPhone)
    bDriverExists = len(qsDriver) != 0
    if not bDriverExists:
        log('Driver not registered with phone : %s' % (dct['pn']))
        return HttpJSONResponse({'status':'false'})
    else:
        log('Auth exists for: %s' % (dct['pn']))
        ret = {'status': True, 'auth':qsDriver[0].auth, 'an':qsDriver[0].an, 'pn':qsDriver[0].pn, 'name':qsDriver[0].name }    
        return HttpJSONResponse(ret)



@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@checkAuth()
def driverGetMode(dct, driver):
    '''
    Driver calls this to get his status
    Returns:
        status(str): The current status

    Note:
        Status changes externally due to trip failure or admin intervention
    '''
    ret = {'st': driver.mode}
    return HttpJSONResponse(ret)


@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@checkAuth()
def driverSetMode(dct, driver):
    '''
    Driver calls this to set his status - AV, OF
    Returns:
        status(str): The current status

    Note:
        Status can be changed only from
            AV to OF and vice versa
            BK to AV/OF iff driver tid is -1

        Otherwise the state is not changed
    '''
    if (driver.mode in ['OF', 'AV']  or driver.tid == -1)  and dct['st'] in ['OF', 'AV']:
        driver.mode = dct['st']
        driver.save()

    ret = {'st': driver.mode}
    return HttpJSONResponse(ret)


@makeView()
@csrf_exempt
@extractParams
@checkAuth(['AV', 'BK'])
def driverRideGetStatus(_dct, driver):
    '''
    Driver calls this to get the status of the current active trip if any
    It must be polled continuously to detect state changes
    Returns:
        active: boolean - means trip is in AS, ST, FN/TR
        status(str): Trip status
        For each of the following statuses, additional data is returned:
            AS: uan, van, id
            ST: progress (percent)
            TR, FN: price, time (seconds), dist (meters), speed (m/s average)
    '''

    # Get the last trip with this driver if any

    ret = {'active': False}

    qsTrip = Trip.objects.filter(id=driver.tid)
    if len(qsTrip):
        trip = qsTrip[0]

        # For assigned trip return user and vehicle an
        if trip.st == 'AS':
            #ret = {'uan': trip.uan, 'van': trip.van}
            ret = {'srclat': trip.srclat, 'srclng':trip.srclng, 'dstlat': trip.dstlat, 'dstlng':trip.dstlng}
            user = User.objects.filter(an=trip.uan)[0]
            ret.update({'uname': user.name, 'uphone': user.pn})

        # For started trip send progress
        elif trip.st == 'ST':
            #pct = Progress.objects.filter(tid=trip.id)[0].pct
            #ret = {'pct': pct}
            #showing progress in Google maps
            ret = {'dstlat': trip.dstlat, 'dstlng': trip.dstlng}
            # maybe in future, we allow the User to update destination whilst being in Trip

        # For ended trips that need payment send the price data
        if trip.st in Trip.PAYABLE:
            ret = getTripPrice(trip)
            # get the acutal price for TRminated trips
            if trip.st in ['TR'] : 
                rate = Rate.objects.filter(id='ride'+str(trip.id))[0]
                ret['price'] = rate.money

        ret['active'] = trip.st in Trip.DRIVER_ACTIVE
        ret['st'] = trip.st
        ret['tid'] = trip.id
        uAuth = User.objects.filter(an=trip.uan)[0].auth
        ret['photourl'] = "https://api.villageapps.in:8090/media/dp_" + uAuth + "_.jpg"


    return HttpJSONResponse(ret)


@makeView()
@csrf_exempt
@handleException()
@extractParams
@checkAuth(['AV'])
def driverRideCheck(_dct, driver):
    '''
    Returns a list of requested trips
    Only trips which start from this drivers PID are returned
    No trips are returned if there are no vehicles there
    '''
    # Get available vehicles at this hub, if none return empty
    #qsVehicles = Vehicle.objects.filter(pid=driver.pid, tid=-1)

    #if len(qsVehicles) == 0:
    #    return HttpJSONResponse({}) # making it easy for Volley to handle JSONArray and JSONObject

    # Get the first requested trip from drivers place id
    qsTrip = Trip.objects.filter(st='RQ').order_by('-rtime') #10 km radius
    #TODO give closest ride first, but how?
    ret = {} if len(qsTrip) == 0 else {'tid': qsTrip[0].id, 'srclat': qsTrip[0].srclat, 'srclng': qsTrip[0].srclng }
    return HttpJSONResponse(ret)


# Do we allow drivers this choice or jsut assign them automatically as I am doing with deliveries.
@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@transaction.atomic
@checkAuth(['AV'])
def driverRideAccept(dct, driver):
    '''
    Accept requested ride by driver
    HTTP args:
        tid : Trip id
        van : an of the Vehicle chosen by driver
    '''
    # Ensure that this driver is not in another active trip (for safety)
    qsActiveTrip = Trip.objects.filter(dan=driver.an, st__in=Trip.DRIVER_ACTIVE)
    if len(qsActiveTrip):
        raise ZPException(400, 'Driver already in trip')

    ret = {}
    # Assign driver to trip and create a trip progress entry
    trip = Trip.objects.filter(id=dct['tid'])[0]
    if trip.st == 'RQ':
        # Ensure that the chosen vehicle is here and not assigned to a trip
        vehicle = Vehicle.objects.filter(an=dct['van'])[0] #, pid=trip.srcid)[0] #think think
        if vehicle.tid != -1:
            raise ZPException(400, 'Vehicle already in trip')

        # Make the trip
        trip.st = 'AS'
        trip.dan = driver.an
        trip.van = vehicle.an
        trip.atime = datetime.now(timezone.utc)
        trip.save()

        # Make the progress
        progress = Progress()
        progress.tid = trip.id
        progress.pct = 0
        progress.save()

        # Set the driver to booked, set tid
        driver.mode = 'BK'
        driver.tid = trip.id
        driver.save()

        # set the vehicles tid
        vehicle.tid = trip.id
        vehicle.save()

        #ret.update({'dstid': trip.dstid})

        user = User.objects.filter(an=trip.uan)[0]
        ret.update({'name': user.name, 'phone': user.pn})
        #src = Place.objects.filter(id=trip.srcid)[0]
        #dst = Place.objects.filter(id=trip.dstid)[0]
        #ret.update({'srcname': src.pn, 'dstname': dst.pn})
        print("Accepting trip : ", ret)
    else:
        raise ZPException(400, 'Trip already assigned')

    return HttpJSONResponse(ret)


@makeView()
@csrf_exempt
@handleException()
@extractParams
@transaction.atomic
@checkAuth(['BK'])
@checkTripStatus(['AS'])
def driverRideCancel(_dct, driver, trip):
    '''
    Called by driver to deny a trip that was assigned (AS)
    '''
    # Change trip status from assigned to  denied
    # Set the state for the trip and driver - driver is set to OF on failure
    if trip.st == 'AS':
        driver.mode = 'AV'
        trip.st = 'DN'

    # Reset driver tid, but not users since they need to see the DN state
    retireEntity(driver)

    # Note the time of trip cancel/fail and save
    trip.etime = datetime.now(timezone.utc)
    trip.save()

    # Reset the vehicle tid
    vehicle = Vehicle.objects.filter(tid=trip.id)[0]
    retireEntity(vehicle)

    return HttpJSONResponse({})


@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@transaction.atomic
@checkAuth(['BK'])
@checkTripStatus(['AS'])
def driverRideStart(dct, _driver, trip):
    '''
    Driver calls this to start the trip providing the OTP that the user shared
    HTTP Args:
        OTP
    '''
    if str(dct['otp']) == str(getOTP(trip.uan, trip.dan, trip.atime)):
        trip.st = 'ST'
        trip.stime = datetime.now(timezone.utc)
        trip.save()
    else:
        raise ZPException(403, 'Invalid OTP')

    return HttpJSONResponse({})


@makeView()
@csrf_exempt
@extractParams
@transaction.atomic
@checkAuth(['BK'])
@checkTripStatus(['ST'])
def driverRideEnd(_dct, driver, trip):
    '''
    Driver calls this to end ride
    TODO: Verify via vehicle/driver/user location that the trip actually happened
    '''
    trip.st = 'FN'
    trip.etime = datetime.now(timezone.utc)
    trip.save()

    # Get the vehicle
    recVehicle = Vehicle.objects.filter(an=trip.van)[0]

    # Calculate price
    dctPrice = getRidePrice(trip.srclat, trip.srclng, trip.dstlat, trip.dstlng, recVehicle.vtype, trip.pmode, (trip.etime - trip.stime).seconds)
    return HttpJSONResponse(dctPrice)


@makeView()
@csrf_exempt
@extractParams
@transaction.atomic
@checkAuth(['BK'])
@checkTripStatus(['FN', 'TR'])
def driverPaymentConfirm(_dct, driver, trip):
    '''
    Driver calls this to confirm money received

    Note:
        Since state goes to PD, the trip retiring is done here
    '''
    trip.st = 'PD'
    trip.save()

    # driver.mode = 'AV'
    # retiring is NOT done here but in the driver
    #retireEntity(driver)
    
    # user = User.objects.filter(an=trip.uan)[0]
    # user.tid = -1
    # user.save()
    # User retires via userRideRetire

    # Get the vehicle
    # vehicle = Vehicle.objects.filter(an=trip.van)[0]
    # retireEntity(vehicle)
    # NOT required AS PER date 9/11/2020

    return HttpJSONResponse({})



@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@transaction.atomic
@checkAuth(['BK'])
@checkTripStatus(['CN', 'TO', 'PD'])
def driverRideRetire(dct, driver, trip):
    '''
    Resets driver's and vehicles active trip
    This is called when the driver has seen the message pertaining to trip end states:
    'TO', 'CN'

    These states occur by admin refresh or user cancel

    Following states when reached, have already retired the driver and vehicle
    DN : driver already retired from driverRideCancel()
    # PD : driver already retired from driverPaymentConfirm #NOT anymore 9/11/2020
    FL : admin already retired from adminHandleFailedTrip()

    TODO: move this common code to a function
    '''
    # made the driver AV and reset the tid to -1
    driver.mode = 'AV'
    retireEntity(driver)

    # Reset the vehicle tid to available
    vehicle = Vehicle.objects.filter(tid=trip.id)[0]
    vehicle.tid = Vehicle.AVAILABLE
    vehicle.save()
    return HttpJSONResponse({})



@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@checkAuth(['AV'])
def driverIsVehicleSet(dct, driver):
    '''
    checks whether Driver has a vehicle assigned or not
    HTTP args :
        auth
    '''
    ret = driver.van != -1
    return HttpJSONResponse({'set' : ret})


@makeView()
@csrf_exempt
@handleException(Vehicle.DoesNotExist, 'Vehicle not found', 404)
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@checkAuth(['AV'])
def driverVehicleSet(dct, driver):
    '''
    Driver chooses a vehicle for ride
    HTTP args :
        auth,
        van
    '''
    driver.van = dct['van']
    driver.save()
    vehicle = Vehicle.objects.filter(an=dct['van'])[0]
    vehicle.dan = driver.an
    vehicle.save()
    return HttpJSONResponse({})


@makeView()
@csrf_exempt
@handleException(IndexError, 'Vehicle not found', 404)
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@transaction.atomic
@checkAuth(['AV'])
def driverVehicleRetire(dct, driver):
    '''
    Resets driver's and vehicles active trip
    '''
    if driver.van !=-1:
        vehicle = Vehicle.objects.filter(an=driver.van)[0]
        vehicle.dan = -1
        vehicle.save()

        driver.van = -1
        driver.save()
    return HttpJSONResponse({})


# ============================================================================
# User views
# ============================================================================

@makeView()
@csrf_exempt
@handleException(googlemaps.exceptions.ApiError, 'INVALID_REQUEST', 503)
@handleException(googlemaps.exceptions.TransportError, 'Internet Connectivity Problem', 503)
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@checkAuth()
def userIsDriverAv(dct, user):
    '''
    Returns the Driver count around 5 km radius of a particular lat and lng

    HTTP args:
        auth : auth of user
        srclat : latitude
        srclng : longitude
        vtype: for drivers of that particualr vehicle

    '''

    srcCoOrds = ['%s,%s' % (dct['srclat'], dct['srclng'])]

    #getcontext().prec = 50
    qsDrivers = Driver.objects.filter(mode='AV').values()
    # Driver.objects.raw('''SELECT * FROM  location INNER JOIN driver WHERE location.an = driver.an AND driver.mode='AV';''');

    ret = {}
    drivers = []
    print('drivers are here ' ,len(qsDrivers))
    # TODO make this view API optimal and Integrate with PUSH notification of FCM
    for driver in qsDrivers:
        #print(driver)
        vehicles = list(Vehicle.objects.filter(vtype=dct['vtype']).values('an','vtype'))
        #print(vehicles)
        gaddi = [veh['an'] for veh in vehicles]
        #print(gaddi)
        #print(int(driver['van']), int(driver['van']) in gaddi)
        if int(driver['van']) in gaddi:
            qsLocs = Location.objects.filter(an=driver['an']).values()
            # print('##############',qsLocs)
            arrLocs = [recPlace for recPlace in qsDrivers]
            dstCoOrds = ['%s,%s' % (recPlace['lat'], recPlace['lng']) for recPlace in qsLocs]
            # print('################',dstCoOrds)

            #print(srcCoOrds, dstCoOrds)

            gMapsRet = googleDistAndTime(srcCoOrds, dstCoOrds)
            nDist, nTime = gMapsRet['dist'], gMapsRet['time']

            #if nTime or nDist:
            if nDist < 50_000:  # 10 kms
                print({'an': driver['an'], 'name': driver['name'], 'dist': nDist, 'time': nTime, 'van':driver['van']})
                drivers.append({'an': driver['an'], 'name': driver['name'], 'dist': nDist, 'time': nTime})
    print("drivers found in 10km radius : ", len(drivers))
    ret.update({'count': len(drivers)}) # {'drivers': drivers})
    return HttpJSONResponse(ret)


@makeView()
@csrf_exempt
@handleException(googlemaps.exceptions.TransportError, 'Internet Connectivity Problem', 503)
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@checkAuth()
@checkTripStatus(['INACTIVE'])
def userRideEstimate(dct, _user, _trip):
    '''
    Returns the estimated price for the trip

    HTTP args:
        srclat, srclng,
        dstlat, dstlng,

        vtype
        npas

        pmode

        hrs=0 for ride
    '''
    print("Ride Estimate param : ", dct)
    ret = getRidePrice(dct['srclat'], dct['srclng'], dct['dstlat'], dct['dstlng'], dct['vtype'], dct['pmode'], 0)
    return HttpJSONResponse(ret)



@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@handleException(IntegrityError, 'Null values sent', 501)
@extractParams
@transaction.atomic
@checkAuth()
#@checkTripStatus(['INACTIVE'])
def userRideRequest(dct, user):#, _trip):
    '''
    User calls this to request a ride

    HTTP args:
    Ride :
        srclat, srclng,
        dstlat, dstlng,
        npas - number of passengers
        #srcid - id of the selected start place
        #dstid - id of the selected destination
        rtype - rent or ride
        vtype - vehicle type
        pmode - payment mode (cash / upi)


    '''
    print("Ride Request param : ", dct)

    trip = Trip()
    trip.uan = user.an
    trip.srclat, trip.srclng = dct['srclat'], dct['srclng']
    trip.dstlat, trip.dstlng = dct['dstlat'], dct['dstlng']
    trip.srcid = user.pid  #TODO automate these two things
    trip.dstid = user.pid+1
    if dct['rtype'] == '0': # Ride
        trip.npas = dct['npas']
        # trip.srcid, trip.dstid = 0,0

    trip.rtype = dct['rtype']
    trip.pmode = dct['pmode']
    trip.rvtype = dct['vtype']
    trip.rtime = datetime.now(timezone.utc)
    trip.save()

    progress = Progress()
    progress.tid = trip.id
    progress.pct = 0
    progress.save()

    user.tid = trip.id
    user.save()

    # we are using only Zbees and Cash only payments right now.
    #ret = getRoutePrice(trip.srcid, trip.dstid, Vehicle.ZBEE, Trip.CASH)
    # getRoutePrice(trip.srcid, trip.dstid, dct['vtype'], dct['pmode'])
    ret = getRiPrice(trip)
    ret['tid'] = trip.id

    # to get the exact price even if the user TRminated the ride en route.
    if trip.rtype=='0':
        rate = Rate()
        rate.id = 'ride' + str(trip.id)
        rate.type = 'ride' if trip.rtype == '0' else 'rent'
        rate.rev = ''
        rate.money = float(getRidePrice(dct['srclat'], dct['srclng'], dct['dstlat'], dct['dstlng'], dct['vtype'], dct['pmode'], 0)['price'])
        rate.save()        


    return HttpJSONResponse(ret)

