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
from django.db.utils import IntegrityError

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
        ret = {'pn': sup.pn, 'name': sup.name, "photourl": "https://api.villageapps.in:8090/media/dp_" + str(sup.auth) + "_.jpg" }
    else:
        sup = Supervisor.objects.filter(pid=trip.dstid)[0]
        ret = {'pn': sup.pn, 'name': sup.name, "photourl": "https://api.villageapps.in:8090/media/dp_" + str(sup.auth) + "_.jpg"}
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



@makeView()
@csrf_exempt
@handleException()
@extractParams
@transaction.atomic
@checkAuth()
#@checkTripStatus( ['RQ', 'AS', 'ST', 'FN', 'TR', 'TO', 'CN', 'DN', 'FL', 'PD'])
def userRentHistory(dct, user):
    '''
    returns the history of all the rental Trips for an entity (a User)
    '''
    #find all trips of the User
    qsTrip = Trip.objects.filter(uan=user.an, rtype='1').order_by('-id').values()  # if type(entity) is User else Trip.objects.filter(dan=entity.an).order_by('-rtime').values()
    
    ret = {}
    print(len(qsTrip))
    if len(qsTrip):
        trips = []
        for i in qsTrip:
            if i['rtype'] == '1':
                hs = user.hs

                #print("Trip state : ", str(i['st']))
                if i['st'] in ['ST', 'FN', 'TR', 'PD']:
                    vtype = Vehicle.objects.filter(an=i['van'])[0].vtype #select vtype of the vehicle of this trip
                    if i['stime'] is None : 
                        sTime = 'notSTARTED'
                    else:
                        strSTime = str(i['stime'])[:19]
                        sDate = datetime.strptime(strSTime, '%Y-%m-%d %H:%M:%S').date()
                    
                    price = float(getRentPrice(i['hrs'])['price'])

                else:
                    price = float(getRentPrice(i['hrs'])['price'])
                    sTime = 'NOTSTARTED'
                    
                if i['st'] in ['FN', 'TR' 'PD']:
                    vtype = Vehicle.objects.filter(an=i['van'])[0].vtype #select vtype of the vehicle of this trip                
                    price = float(getRentPrice(i['hrs'])['price'])
                    
                    if i['etime'] is None:
                        eTime = 'notEnded'

                    else:
                        strETime = str(i['etime'])[:19]
                        eTime = datetime.strptime(strETime, '%Y-%m-%d %H:%M:%S').date()
                else:
                    price = price if price > 1 else price # ooo weee, what an insipid line to code
                    eTime = 'NOTENDED'
                    
                tax = str(round(float('%.2f' % (price*0.05)),0))+'0'  # tax of 5%
                price = str(round(float('%.2f' % price),0))+'0' #2 chars
                
                srchub = Place.objects.filter(id=i['srcid']).name
                dsthub = Place.objects.filter(id=i['dstid']).name
                
                retJson = {   'tid': str(i['id']),
                              'st': str(i['st']),
                              'price': str(price),
                              'tax': str(tax),
                              'sdate': str(sTime),
                              'pickhub': str(srchub),
                              'drophub': str(dsthub),
                              'date': str(sTime),
                              'vtype': str(i['rvtype']),
                              'hrs': str(i['hrs'])
                              
                              }
                trips.append(retJson)
        #print(states)
        ret.update({'trips': trips})

    return HttpJSONResponse(ret)



# ============================================================================
# Supervisor views
# ============================================================================



@makeView()
@csrf_exempt
@handleException(IndexError, 'Sup Not found', 404)
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
def supRentLogin(_, dct):
    '''
    Makes the supervisor login 
    HTTPS args:
        pn : phone number,
        sa : super auth
    '''
    sup = Supervisor.objects.filter(pn=dct['pn'], auth=dct['sa'])[0]
    ret = {'auth': sup.auth, 'name':sup.name, 'redirect':True,"redirect_url": "dashboard.html"} #index.html"}
    return HttpJSONResponse(ret)


@makeView()
@csrf_exempt
@handleException(IndexError, 'Trip/User/Vehicle not found', 404)
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@checkAuth()
def supVehicleCheck(_dct, sup):
    '''
    Only vehicles which are parked at this Supervisors PID are returned
    '''
    # Get available vehicles at this hub, if none return empty
    qsVehicles = Vehicle.objects.filter(pid=sup.pid, tid=-1)
    if len(qsVehicles) == 0:
        return HttpJSONResponse({'count': 0}) # making it easy for Volley to handle JSONArray and JSONObject
    # print("vehicle found...")
    vehicles = []
    for veh in qsVehicles :
        vehicles.append({'id': veh.id, 'regn': veh.regn})

    ret = {} if not len(qsVehicles) else {'vehicles': vehicles}
    return HttpJSONResponse(ret)


@makeView()
@csrf_exempt
@handleException(IndexError, 'Trip/User/Vehicle not found', 404)
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@checkAuth()
def supRentCheck(dct, sup):
    '''
    Returns a list of requested trips
    Only trips which start from this Supervisors PID are returned
    # No trips are returned if there are no vehicles there

    HTTP args :
        state : for rentals are required
    '''
    # Get available vehicles at this hub, if none return empty
    # qsVehicles = Vehicle.objects.filter(pid=sup.pid, tid=-1)
    # if len(qsVehicles) == 0:
    #    return HttpJSONResponse({'count':0}) # making it easy for Volley to handle JSONArray and JSONObject
    # print("vehicle found...")
    # Get the first requested trip from Supervisors place id
    qsTrip = Trip.objects.filter(rtype=1, srcid=sup.pid, st=dct['state'])#__in=['\''+dct['state']+'\'']).order_by('-rtime')
    print("%d trips found" % (len(qsTrip)))
    rentals = []
    for trip in qsTrip :
        uName = User.objects.filter(an=trip.uan)[0].name
        vals = {'tid': trip.id, 'st': trip.st, 'uname': uName}

        if trip.rvtype == 0:
            vals['rvtype'] = 'CYCLE'
        elif trip.rvtype == 1:
            vals['rvtype'] = 'SCOOTY'
        elif trip.rvtype == 2:
            vals['rvtype'] = 'BIKE'
        elif trip.rvtype == 3:
            vals['rvtype'] = 'ZBEE'

        if trip.st == 'ST':
            vals['price'] = getTripPrice(trip)['price']
        elif trip.st == 'FN':
            vals['price'] = getTripPrice(trip)['price']
        else:
            vals['price'] = getRentPrice(trip.hrs)['price']
        uAuth = User.objects.filter(an=trip.uan)[0].auth
        vals['photourl'] = "https://api.villageapps.in:8090/media/dp_" + uAuth + "_.jpg"

        vals['van'] = trip.van
        rentals.append(vals)
        
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


# ============================================================================
# Admin views
# ============================================================================

@makeView()
@csrf_exempt
@handleException(IndexError, 'Vehicle not found', 404)
@handleException(KeyError, 'Invalid parameters', 501)
@handleException(IntegrityError, 'Transaction error', 500)
@extractParams
@transaction.atomic
@checkAuth()
def adminVehicleAssign(dct):
    '''
    Checks for rentals in RQ state and assign the vehicle from that hub.

    HTTP args:

    Note:
        assigns one of the free vehciles, ideally the vehicles should have enough charge for the trip.
    '''
    # Get the deliveries and look for RQ ones
    qsTrip = Trip.objects.filter(st__in=['RQ'], rtype=1) # get the trip
    if not len(qsTrip):
        return HttpJSONResponse({})
    else:
        print("%d trips found" % (len(qsTrip)))
    trip = qsTrip[0]
    # get the vehicles with no drivers and which are not on trip
    qsVeh = Vehicle.objects.filter(dan='-1', tid='-1', vtype=trip.rvtype)
    if not len(qsVeh):
        return HttpJSONResponse({'tid': trip.id})
    else:
        print("%d vehicles found" % (len(qsVeh)))

    vehicle = qsVeh[0]
    vid = 0

    if trip.st == 'RQ':
        trip.st = 'AS'
        sup = Supervisor.objects.filter(pid=trip.srcid)[0]
        trip.dan = sup.an  # dan is sup.an
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
        vid = vehicle.an
    else:
        raise ZPException(400, 'Trip already assigned')

    return HttpJSONResponse({'vid': vid, 'tid': trip.id})

@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
def adminRentLogin(_, dct):
    '''
    Makes the admin login 
    HTTPS args:
        pn : phone number,
        auth : admin auth
    '''
    
    ret = {'auth': 'adminAuth007', 'name':'admin', 'redirect':True,"redirect_url": "dashboard.html"}
    return HttpJSONResponse(ret)



@makeView()
@csrf_exempt
@handleException(IndexError, 'Trip/User/Vehicle not found', 404)
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
def admRentCheck(_, dct):
    '''
    Returns a list of requested trips
    Only trips which start from this Supervisors PID are returned
    # No trips are returned if there are no vehicles there

    HTTP args :
        state : for rentals are required
    '''
    if dct['auth'] != 'adminAuth007':
        raise ZPException(403, 'Admin auth wrong!')


    qsTrip = Trip.objects.filter(rtype=1, st=dct['state'])
    print("%d trips found" % (len(qsTrip)))
    rentals = []
    for trip in qsTrip :
        uName = User.objects.filter(an=trip.uan)[0].name
        vals = {'tid': trip.id, 'st': trip.st, 'uname': uName}
        if trip.rvtype == 0:
            vals['rvtype'] = 'CYCLE'
        elif trip.rvtype == 1:
            vals['rvtype'] = 'SCOOTY'
        elif trip.rvtype == 2:
            vals['rvtype'] = 'BIKE'
        elif trip.rvtype == 3:
            vals['rvtype'] = 'ZBEE'

        if trip.st == 'ST':
            vals['price'] = getTripPrice(trip)['price']
        elif trip.st == 'FN':
            vals['price'] = getTripPrice(trip)['price']
        else:
            vals['price'] = getRentPrice(trip.hrs)['price']
        uAuth = User.objects.filter(an=trip.uan)[0].auth
        vals['photourl'] = "https://api.villageapps.in:8090/media/dp_" + uAuth + "_.jpg"

        vals['van'] = trip.van
        rentals.append(vals)
        
    ret = {} if not len(qsTrip) else {'rentals': rentals}
    return HttpJSONResponse(ret)
