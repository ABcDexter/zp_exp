# imports
import datetime
from datetime import datetime, timedelta

from django.db import transaction
from django.utils import timezone
from django.views.decorators.csrf import csrf_exempt

from url_magic import makeView
from ..models import Place, Trip, Progress, Driver
from ..models import User, Vehicle
from ..utils import ZPException, HttpJSONResponse, getRentPrice
from ..utils import getOTP
from ..utils import getRoutePrice, getTripPrice
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
    '''
    # take time in minutes
    # oldTime = (trip.etime - trip.rtime).total_seconds() / 60
    iHrs = int(dct['hrs'])
    #startTime = trip.stime #datetime.now(timezone.utc)
    #tdISTdelta = timedelta(hours=iHrs, minutes=0)
    #endTime = startTime + tdISTdelta
    #trip.etime = endTime
    #dstId = trip.
    #if trip.st == 'RQ':
    #    trip.etime = trip.rtime + dct['hrs']
    #else :
    #    trip.etime = trip.rtime + dct['hrs']
    
    #trip.dstid = dct['dstid']
    trip.hrs = iHrs
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
    ret = {'vno': vehicle.regn, 'otp': 1243} #TODO complete this
    return HttpJSONResponse(ret)


@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@checkAuth()
@checkTripStatus('AS')
def userRentGetSup(_dct, entity, trip):
    '''
    Returns aadhaar, name and phone of hub supervisor
    '''
    driver = Driver.objects.filter(an=trip.dan)[0]
    ret = {'pn': driver.pn, 'name': driver.name}
    return HttpJSONResponse(ret)


@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@checkAuth()
@checkTripStatus('ST')
def authTimeRemaining(_dct, entity, trip):
    '''
    Returns aadhaar, name and phone of current assigned supervisor
    '''
    currTime = datetime.now(timezone.utc)
    diffTime = (currTime - trip.stime).total_seconds() / 60 # minutes 
    remHrs = diffTime - trip.hrs 
    ret = {}
    ret['time'] = remHrs
    return HttpJSONResponse(ret)

# ============================================================================
# Supervisor views
# ============================================================================


@makeView()
@csrf_exempt
@handleException(IndexError, 'Trip not found', 404)
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
        return HttpJSONResponse({}) # making it easy for Volley to handle JSONArray and JSONObject

    # Get the first requested trip from Supervisors place id
    qsTrip = Trip.objects.filter(srcid=sup.pid, st='RQ').order_by('-rtime')
    ret = {} if len(qsTrip) == 0 else {'tid': qsTrip[0].id}
    return HttpJSONResponse(ret)


@makeView()
@csrf_exempt
@handleException(IndexError, 'Trip not found', 404)
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@transaction.atomic
@checkAuth()
def supRentAccept(dct, sup):
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
            raise ZPException(400, 'Vehicle already in trip')

        # Make the trip
        trip.st = 'AS'
        trip.dan = sup.an # supervisor can be taken as driver
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
        #print("Accepting trip : ", ret)
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
    # Set the state for the trip and driver - driver is set to OF on failure
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
def superRentStart(dct, _sup):
    '''
    Supervisor calls this to start the trip providing the OTP that the user shared
    HTTP Args:
        OTP,
        tid
    '''
    qsTrip = Trip.objects.filter(id=dct['tid'])
    if len(qsTrip):
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
    recVehicle = Vehicle.objects.filter(an=trip.van)[0]

    # Calculate price
    dctPrice = getRentPrice(trip.srcid, trip.dstid, recVehicle.vtype, trip.pmode, trip.hrs)
    return HttpJSONResponse(dctPrice)


@makeView()
@csrf_exempt
@handleException(IndexError, 'Trip not found', 404)
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@transaction.atomic
@checkAuth()
def supPaymentConfirm(dct, _sup):
    '''
    Supervisor calls this to confirm money received
    '''
    qsTrip = Trip.objects.filter(id=dct['tid'])
    if len(qsTrip):
        trip = qsTrip[0]

    trip.st = 'PD'
    trip.save()

    # Get the vehicle
    vehicle = Vehicle.objects.filter(an=trip.van)[0]
    retireEntity(vehicle)

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
    # made the driver AV and reset the tid to -1
    # Reset the vehicle tid to available
    qsTrip = Trip.objects.filter(id=dct['tid'])
    if len(qsTrip):
        trip = qsTrip[0]

    vehicle = Vehicle.objects.filter(tid=trip.id)[0]
    vehicle.tid = Vehicle.AVAILABLE
    vehicle.save()
    return HttpJSONResponse({})
