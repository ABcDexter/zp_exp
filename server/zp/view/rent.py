# imports
import datetime
from datetime import datetime, timedelta
from os import truncate

from django.db import transaction
from django.utils import timezone
from django.views.decorators.csrf import csrf_exempt

from url_magic import makeView
from ..models import Place, Trip, Progress, Supervisor
from ..models import User, Vehicle
from ..utils import ZPException, HttpJSONResponse
from ..utils import getOTP
from ..utils import getTripPrice, getRentPrice
from ..utils import handleException, extractParams, checkAuth, checkTripStatus, retireEntity

###########################################
# Types
Filename = str


###########################################
# Constants

makeView.APP_NAME = 'zp'


# ============================================================================
# Rental views
# ============================================================================


@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@transaction.atomic
@checkAuth()
@checkTripStatus(['RQ', 'AS', 'ST'])
def userRentalUpdate(dct, user, trip):
    '''
    Update the rental time for a user if requested, assigned or started
    this update the etime for the user
    HTTP args:
        hrs,
        dstid
    '''
    # take time in minutes
    # oldTime = (trip.etime - trip.rtime).total_seconds() / 60
    # startTime = trip.stime #datetime.now(timezone.utc)
    # tdISTdelta = timedelta(hours=iHrs, minutes=0)
    # endTime = startTime + tdISTdelta
    # trip.etime = endTime
    # dstId = trip.
    # if trip.st == 'RQ':
    #    trip.etime = trip.rtime + dct['hrs']
    # else :
    #    trip.etime = trip.rtime + dct['hrs']

    # trip.dstid = dct['dstid']
    if 'hrs' in dct:
        iHrs = int(dct['hrs'])
        trip.hrs += iHrs # increment and not update
    if 'dstid' in dct:
        trip.dstid = dct['dstid']
    trip.save()

    return HttpJSONResponse({})


@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@checkAuth()
@checkTripStatus('AS')
def userRentGetVehicle(_dct, entity, trip):
    '''
    Returns aadhaar, name and phone of current assigned vehicle,
    '''
    vehicle = Vehicle.objects.filter(an=trip.van)[0]
    ret = {'vno': vehicle.regn, 'otp': str(getOTP(trip.uan, trip.dan, trip.atime)),
           #'price': getRentPrice(trip.srcid, trip.dstid, vehicle.vtype, trip.pmode, trip.hrs)}
           'price': getRentPrice(trip.hrs)}
    return HttpJSONResponse(ret)


@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@checkAuth()
@checkTripStatus(['AS', 'ST'])
def userRentGetSup(_dct, _user, trip):
    '''
    Returns aadhaar, name and phone of hub supervisor
    '''
    if trip.st == 'AS':
        sup = Supervisor.objects.filter(pid=trip.srcid)[0]
        ret = {'pn': sup.pn, 'name': sup.name}
    else:
        sup = Supervisor.objects.filter(pid=trip.dstid)[0]
        ret = {'pn': sup.pn, 'name': sup.name}
    return HttpJSONResponse(ret)


@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@checkAuth()
@checkTripStatus('ST')
def userRentEnd(_dct, user, _trip):
    '''
    return nearest hub name, their distance from current location and lat,lng
    '''
    qsPrevHubs = Place.objects.raw(
        'SELECT pl.id, pl.pn, pl.lat, pl.lng FROM place pl WHERE pl.id < (SELECT plcurr.id FROM place plcurr WHERE plcurr.id= %s) ORDER BY pl.id DESC LIMIT 2;',
        [user.pid])
    qsCurrHub = Place.objects.raw('SELECT plcurr.id, plcurr.pn, plcurr.lat, plcurr.lng FROM place plcurr WHERE plcurr.id=%s', [user.pid])

    qsNextHubs = Place.objects.raw(
        'SELECT pl.id, pl.pn, pl.lat, pl.lng FROM place pl WHERE pl.id > (SELECT plcurr.id FROM place plcurr WHERE plcurr.id= %s ) ORDER BY pl.id ASC LIMIT 2;',
        [user.pid])

    #print(len(qsCurrHub), 'CURR HUB : ', qsCurrHub)
    #print(len(qsPrevHubs), 'PREV HUBS : ', qsPrevHubs)
    #print(len(qsNextHubs), 'NEXT HUBS : ', qsNextHubs)
    #print('###############################')
    #recRoute = Route.getRoute(idSrc, idDst)
    prev0Dst = round( 2048 / 999, 1)
    prev1Dst = round(1024 / 999,  1)
    currDst = round( 512 / 999, 1)
    next0Dst = round( 2048 / 999, 1)
    next1Dst = round( 4096 / 999, 1)

    close1 = Place()
    close2 = Place()
    #close3 = Place()

    if not len(qsPrevHubs):
        close1.pn, close1.lat, close1.lng, c1Dst = qsCurrHub[0].pn, qsCurrHub[0].lat,  qsCurrHub[0].lng,  currDst
        close2.pn, close2.lat, close2.lng, c2Dst = qsNextHubs[0].pn, qsNextHubs[0].lat,  qsNextHubs[0].lng, next0Dst
        #close3.pn, close3.lat, close3.lng, c3Dst = qsNextHubs[1].pn, qsNextHubs[1].lat,  qsNextHubs[1].lng, next1Dst

    elif not len(qsNextHubs):
        close1.pn, close1.lat, close1.lng, c1Dst = qsCurrHub[0].pn, qsCurrHub[0].lat, qsCurrHub[0].lng, currDst
        close2.pn, close2.lat, close2.lng, c2Dst = qsPrevHubs[1].pn, qsPrevHubs[1].lat, qsPrevHubs[1].lng, prev1Dst
        #close3.pn, close3.lat, close3.lng, c3Dst = qsPrevHubs[0].pn, qsPrevHubs[0].lat, qsPrevHubs[0].lng, prev0Dst

    else:
        close1.pn, close1.lat, close1.lng, c1Dst = qsCurrHub[0].pn, qsCurrHub[0].lat, qsCurrHub[0].lng, currDst
        if prev0Dst >= next0Dst:
            close2.pn, close2.lat, close2.lng, c2Dst = qsPrevHubs[0].pn, qsPrevHubs[0].lat, qsPrevHubs[0].lng, prev0Dst
            #close3.pn, close3.lat, close3.lng, c3Dst = qsNextHubs[0].pn, qsNextHubs[0].lat,  qsNextHubs[0].lng, next1Dst
        else:
            close2.pn, close2.lat, close2.lng, c2Dst = qsNextHubs[0].pn, qsNextHubs[0].lat, qsNextHubs[0].lng, next1Dst
            #close3.pn, close3.lat, close3.lng, c3Dst = qsPrevHubs[0].pn, qsPrevHubs[0].lat, qsPrevHubs[0].lng, prev0Dst

    return HttpJSONResponse({'close1pn': close1.pn, 'close1lat': close1.lat, 'close1lng': close1.lng, 'close1dst': c1Dst,
                            'close2pn': close2.pn, 'close2lat': close2.lat, 'close2lng': close2.lng, 'close2dst': c2Dst#,
                             #'close3pn': close3.pn, 'close3lat': close3.lat, 'close3lng': close3.lng, 'close3dst': c3Dst
                             })


@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@checkAuth()
@checkTripStatus('ST')
def authTimeRemaining(_dct, entity, trip):
    '''
    #Obsolete tells the remaining minutes.
    '''
    ret = {}
    currTime = datetime.now(timezone.utc)
    diffTime = (currTime - trip.stime).total_seconds() // 60  # minutes
    remHrs = trip.hrs * 60 - diffTime
    ret['time'] = int(remHrs)
    return HttpJSONResponse(ret)


@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@checkAuth()
@checkTripStatus('ST')
def userTimeUpdate(dct, _user, trip):
    '''
    HTTPS args:
        auth, newdrophub,
        updatedtime

    Returns price
    '''
    # newDropHub =  dct['newdrophub'] if 'newdrophub' in dct else  trip.dstid
    extraHrs = int(trip.hrs)+int(dct['updatedtime'])
    # recVehicle = Vehicle.objects.filter(an=trip.van)[0]
    oldPrice = getRentPrice(trip.hrs) #= getRentPrice(trip.srcid, trip.dstid, recVehicle.vtype, trip.pmode, trip.hrs)
    newPrice = getRentPrice(extraHrs) #= getRentPrice(trip.srcid, newDropHub, recVehicle.vtype, trip.pmode, extraHrs)
    print(oldPrice, newPrice)
    ret = {'price': str(max(20, int(float(newPrice['price']) - float(oldPrice['price'])))) + '.00'}

    return HttpJSONResponse(ret)


@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@transaction.atomic
@extractParams
@checkAuth()
@checkTripStatus('ST')
def authTripUpdate(dct, _entity, trip):
    '''
    HTTPS args:
        auth, newdrophub,
        updatedtime

    Returns price
    '''

    trip.dstid = dct['newdrophub'] if 'newdrophub' in dct else trip.dstid
    trip.hrs = dct['updatedtime'] if 'updatedtime' in dct else trip.hrs
    trip.save()

    return HttpJSONResponse({})


@makeView()
@csrf_exempt
@handleException(IndexError, 'Vehicle Not Found', 404)
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@checkAuth()
@checkTripStatus('AS')
def userVehicleHold(_dct, user, trip):
    '''
    Returns aadhaar, name and phone of current assigned vehicle,
    '''
    vehicle = Vehicle.objects.filter(an=trip.van)[0]
    #ret = {'price': getRentPrice(trip.srcid, trip.dstid, vehicle.vtype, trip.pmode, trip.hrs)['price'] + 100}
    ret = {'price': getRentPrice(trip.hrs)['price'] + 100}
    return HttpJSONResponse(ret)


@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@transaction.atomic
@checkAuth()
#@checkTripStatus(['INACTIVE'])
def userRentRequest(dct, user): #, _trip):
    '''
    #Obsolete
    User calls this to request a rental

    HTTP args:
        auth
     Rent :
        srcid - id of the selected start place
        dstid - id of the selected destination
        rtype - rent or ride
        vtype - vehicle type
        hrs   - number of hours
        pmode - 1 from the user

    '''
    print("Rental Request param : ", dct)

    # Even though we can use IDs directly, look them up in the DB to prevent bogus IDs
    placeSrc = Place.objects.filter(id=dct['srcid'])[0]
    placeDst = Place.objects.filter(id=dct['dstid'])[0]

    trip = Trip()
    trip.uan = user.an
    trip.srcid = placeSrc.id
    trip.dstid = placeDst.id

    #trip.npas = 0 #assuming 4 passengers for rental
    iHrs = int(dct['hrs'])
    trip.hrs = iHrs

    trip.rtype = dct['rtype']
    trip.pmode = dct['pmode']
    trip.rvtype = dct['vtype']
    trip.rtime = datetime.now(timezone.utc)
    trip.srclat = placeSrc.lat
    trip.srclng = placeSrc.lng
    trip.dstlat = placeDst.lat
    trip.dstlng = placeDst.lng

    trip.save()

    progress = Progress()
    progress.tid = trip.id
    progress.pct = 0
    progress.save()

    user.tid = trip.id
    user.save()

    # we are using only Zbees and Cash only payments right now.
    #ret = getRoutePrice(trip.srcid, trip.dstid, Vehicle.ZBEE, Trip.CASH)
    #ret = getRentPrice(trip.srcid, trip.dstid, dct['vtype'], dct['pmode'])
    ret = getRentPrice(dct['hrs'])
    ret['tid'] = trip.id

    return HttpJSONResponse(ret)

@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@transaction.atomic
@checkAuth()
@checkTripStatus(['AS', 'FN'])
def userRentPay(dct, _user, trip):
    '''
    User calls this to pay for the rental

    HTTP args:
        auth
    '''
    print("Rental payment confirm param : ", dct)
    return HttpJSONResponse({'otp': getOTP(trip.uan, trip.dan, trip.atime)})


# ============================================================================
# Supervisor views
# ============================================================================


@makeView()
@csrf_exempt
@handleException(IndexError, 'Trip/User/Vehicle not found', 404)
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@checkAuth()
def supRentCheck(_dct, sup):
    '''
    Returns a list of requested trips
    Only trips which start from this Supervisors PID are returned
    No trips are returned if there are no vehicles there
    '''
    # Get available vehicles at this hub, if none return empty
    qsVehicles = Vehicle.objects.filter(pid=sup.pid, tid=-1)
    if len(qsVehicles) == 0:
        return HttpJSONResponse({'count':0}) # making it easy for Volley to handle JSONArray and JSONObject
    #aasdf
    # Get the first requested trip from Supervisors place id
    qsTrip = Trip.objects.filter(srcid=sup.pid, st__in=['RQ', 'AS', 'TR', 'FN']).order_by('-rtime')
    rentals = []
    for trip in qsTrip :
        uName = User.objects.filter(an=trip.uan)[0].name
        rentals.append({'tid': trip.id, 'st': trip.st, 'rvtype': trip.rvtype, 'uname': uName})
    ret = {} if not len(qsTrip) else {'rentals': rentals}
    return HttpJSONResponse(ret)


@makeView()
@csrf_exempt
@handleException(IndexError, 'Place/Trip/User/Vehicle not found', 404)
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@transaction.atomic
@checkAuth()
def supRentVehicleAssign(dct, sup):
    '''
    Accept requested rent
    HTTP args:
        tid : Trip id
        van : an of the Vehicle chosen by Supervisor
    '''

    ret = {}
    trip = Trip.objects.filter(id=dct['tid'])[0]
    if trip.st == 'RQ':
        # Ensure that the chosen vehicle is here and not assigned to a trip
        vehicle = Vehicle.objects.filter(an=dct['van'], pid=trip.srcid)[0]
        if vehicle.tid != -1:
            raise ZPException(400, 'Vehicle already in trip!')
        elif vehicle.vtype != trip.rvtype:
            raise ZPException(400, 'Vehicle type not the same as requested!')

        # Make the trip
        trip.st = 'AS'
        trip.dan = sup.an #dan is sup.an
        trip.van = vehicle.an
        trip.atime = datetime.now(timezone.utc)
        trip.save()

        # Make the progress
        progress = Progress()
        progress.tid = trip.id
        progress.pct = 0
        progress.save()

        # set the vehicles tid
        vehicle.tid = trip.id
        vehicle.save()

        ret.update({'dstid': trip.dstid})

        user = User.objects.filter(an=trip.uan)[0]
        ret.update({'name': user.name, 'phone': user.pn})
        src = Place.objects.filter(id=trip.srcid)[0]
        dst = Place.objects.filter(id=trip.dstid)[0]
        ret.update({'srcname': src.pn, 'dstname': dst.pn, 'hrs': trip.hrs})
        # print("Accepting trip : ", ret)
    else:
        raise ZPException(400, 'Trip already assigned')

    return HttpJSONResponse(ret)

@makeView()
@csrf_exempt
@handleException(IndexError, 'Trip not found', 404)
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@checkAuth()
def supRentGetStatus(dct, _sup):
    '''
    Supervisor calls this to get the status of the trip with tid
    HTTPS args
    tid
    Returns:
        active: boolean - means trip is in AS, ST, FN/TR
        status(str): Trip status
        For each of the following statuses, additional data is returned:
            AS: uan, van, id
            ST: progress (percent)
            TR, FN: price, time (seconds), dist (meters), speed (m/s average)
    '''

    ret = {'active': False}

    qsTrip = Trip.objects.filter(id=dct['tid'])
    if len(qsTrip):
        trip = qsTrip[0]

        # For assigned trip return user and vehicle an
        if trip.st == 'AS':
            ret = {'uan': trip.uan, 'van': trip.van}

        # For started trip send progress
        if trip.st == 'ST':
            pct = Progress.objects.filter(tid=trip.id)[0].pct
            ret = {'pct': pct}

        # For ended trips that need payment send the price data
        if trip.st in Trip.PAYABLE:
            ret = getTripPrice(trip)

        ret['active'] = trip.st in Trip.SUPER_ACTIVE
        ret['st'] = trip.st
        ret['tid'] = trip.id

    return HttpJSONResponse(ret)


@makeView()
@csrf_exempt
@handleException(IndexError, 'Trip not found', 404)
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@transaction.atomic
@checkAuth()
def supRentCancel(dct, _sup):
    '''
    Called by supervisor to deny a trip that was assigned (AS)
    HTTP args :
    tid
    '''
    # Change trip status from assigned to  denied
    # Set the state for the trip
    qsTrip = Trip.objects.filter(id=dct['tid'])
    if len(qsTrip):
        trip = qsTrip[0]

    if trip.st == 'AS':
        trip.st = 'DN'

    # Note the time of trip cancel/fail and save
    trip.etime = datetime.now(timezone.utc)
    trip.save()

    # Reset the vehicle tid
    vehicle = Vehicle.objects.filter(tid=trip.id)[0]
    retireEntity(vehicle)

    return HttpJSONResponse({})


@makeView()
@csrf_exempt
@handleException(IndexError, 'Trip not found', 404)
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@transaction.atomic
@checkAuth()
def supRentStart(dct, _sup):
    '''
    Supervisor calls this to start the trip providing the OTP that the user shared
    HTTP Args:
        OTP,
        tid
    '''
    qsTrip = Trip.objects.filter(id=dct['tid'])
    trip = qsTrip[0]

    if str(dct['otp']) == str(getOTP(trip.uan, trip.dan, trip.atime)):
        trip.st = 'ST'
        trip.stime = datetime.now(timezone.utc)
        trip.save()
    else:
        raise ZPException(403, 'Invalid OTP')

    return HttpJSONResponse({})


@makeView()
@csrf_exempt
@handleException(IndexError, 'Trip not found', 404)
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@transaction.atomic
@checkAuth()
def supRentEnd(dct, _sup):
    '''
    Supervisor calls this to end ride
    HTTP args:
    tid
    '''
    qsTrip = Trip.objects.filter(id=dct['tid'])
    if len(qsTrip):
        trip = qsTrip[0]

    trip.st = 'FN'
    trip.etime = datetime.now(timezone.utc)
    trip.save()

    # Get the vehicle
    #recVehicle = Vehicle.objects.filter(an=trip.van)[0]

    # Calculate price
    # dctPrice = getRentPrice(trip.srcid, trip.dstid, recVehicle.vtype, trip.pmode, trip.hrs)
    #dctPrice = #getRentPrice(trip.hrs, (trip.etime - trip.stime).seconds //60 )
    return HttpJSONResponse({'price': int(float(getTripPrice(trip)['price']) - float(getRentPrice(trip.hrs)['price']))})


@makeView()
@csrf_exempt
@handleException(IndexError, 'Trip not found', 404)
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@transaction.atomic
@checkAuth()
def supPaymentConfirm(dct, _sup):
    '''
    #Obsolete Supervisor calls this to confirm money received
    '''
    qsTrip = Trip.objects.filter(id=dct['tid'])
    if len(qsTrip):
        trip = qsTrip[0]
    #TODO upgrade this to admin method
    #trip.st = 'PD'
    #trip.save()

    # Get the vehicle
    #vehicle = Vehicle.objects.filter(an=trip.van)[0]
    #retireEntity(vehicle)
    # DO NOT retire here, retire at supRentRetire
    return HttpJSONResponse({})


@makeView()
@csrf_exempt
@handleException(IndexError, 'Trip not found', 404)
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@transaction.atomic
@checkAuth()
def supRentRetire(dct, _sup):
    '''
    Resets vehicles active trip
    '''
    # set the tips state to PD
    # Reset the vehicle tid to available
    qsTrip = Trip.objects.filter(id=dct['tid'])
    trip = qsTrip[0]
    trip.st = 'PD'
    trip.save()

    vehicle = Vehicle.objects.filter(tid=trip.id)[0]
    vehicle.tid = Vehicle.AVAILABLE
    vehicle.save()
    return HttpJSONResponse({})
