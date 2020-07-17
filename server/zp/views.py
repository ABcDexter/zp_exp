# imports
import binascii
import datetime
import json
import logging
import os
import random
from datetime import datetime, timedelta
from decimal import getcontext
from functools import wraps
from json import JSONDecodeError
from typing import Dict

from django.conf import settings
from django.core.serializers.json import DjangoJSONEncoder
from django.db import transaction
from django.db.utils import OperationalError, IntegrityError
from django.http import HttpResponse
from django.utils import timezone
from django.views.decorators.csrf import csrf_exempt
from django.forms.models import model_to_dict

from .models import Driver, User, Vehicle, Delivery
from .models import Place, Trip, Progress, Location, Route

from .utils import HttpJSONError, ZPException, DummyException, HttpJSONResponse, HttpRecordsResponse, log
from .utils import saveTmpImgFile, doOCR, aadhaarNumVerify, getClientAuth, renameTmpImgFiles, getOTP, doOCRback
from .utils import getRoutePrice, getTripPrice, getRentPrice
from .utils import handleException, extractParams, checkAuth, checkTripStatus, retireEntity
from .utils import headers

from url_magic import makeView
from zp.view import rent, ride, deliver, pwa

###########################################
# Types
Filename = str


###########################################
# Constants

makeView.APP_NAME = 'zp'


##############################################################################
# General Views
##############################################################################

@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@transaction.atomic
@extractParams
def registerUser(_, dct: Dict):
    '''
    User registration
    Creates a user record for the aadhar number OCR'd from the image via Google Vision
    Aadhaar scans are archived in settings.AADHAAR_DIR as
    <aadhaar>_front.jpg and <aadhaar>_back.jpg

    HTTP Args:
        aadhaarFront - aadhar front scan
        aadhaarBack - aadhar back scan

    Notes:
        Registration is done atomically since we also need to save aadhar scans after DB write
    '''
    sPhone = str(dct['phone'])
    sAadharFrontFilename = saveTmpImgFile(settings.AADHAAR_DIR, dct['aadhaarFront'], 'front')
    sAadharBackFilename = saveTmpImgFile(settings.AADHAAR_DIR, dct['aadhaarBack'], 'back')
    log('User Registration request - Aadhar images saved at %s, %s' % (sAadharFrontFilename, sAadharBackFilename))

    # Get aadhaar as 3 groups of 4 digits at a time via google vision api
    clientDetails = doOCR(sAadharFrontFilename)
    sAadhaar = clientDetails['an']
    log('Aadhaar number read from %s - %s' % (sAadharFrontFilename, sAadhaar))
    clientDetails2 = doOCRback(sAadharBackFilename)
    sAadhaar2 = clientDetails2['an']
    log('Aadhaar number read from %s - %s' % (sAadharBackFilename, sAadhaar2))

    # verify aadhaar number via Verhoeff algorithm
    if not aadhaarNumVerify(sAadhaar):
        raise ZPException(501,'Aadhaar number not valid!')
    log('Aadhaar is valid')

    if sAadhaar != sAadhaar2:
        raise ZPException(501, 'Aadhaar number front doesn\'t match Aadhaar number back!')

    # Check if aadhaar has been registered before
    qsUser = User.objects.filter(an=int(sAadhaar))
    bUserExists = len(qsUser) != 0
    if not bUserExists:
        sAuth = getClientAuth(sAadhaar, sPhone)
        user = User()
        user.name = clientDetails['name']
        user.age = int(clientDetails['age'])
        user.gdr = clientDetails['gender']
        user.auth = sAuth
        user.an = int(sAadhaar)
        user.pn = sPhone
        user.hs = clientDetails2['hs']
        user.save()

        renameTmpImgFiles(settings.AADHAAR_DIR, sAadharFrontFilename, sAadharBackFilename, sAadhaar)
        log('New user registered: %s' % sAadhaar)
    else:
        # Aadhaar exists, if mobile has changed, get new auth
        user = qsUser[0]
        if user.pn != sPhone:
            sAuth = getClientAuth(str(qsUser[0].an), str(qsUser[0].pn))
            user.pn = sPhone
            user.auth = sAuth
            user.save()
            log('Auth changed for: %s' % sAadhaar)
        else:
            # Aadhaar exists, phone unchanged, just return existing auth
            sAuth = user.auth
            log('Auth exists for: %s' % sAadhaar)

    # return the whole user record
    return HttpJSONResponse(model_to_dict(user))


@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@transaction.atomic
@extractParams
def registerDriver(_, dct: Dict):
    '''
    Driver registration
    Creates a driver entry in the database, pending verification (state RG)
    Once admins have verified offline, registration is completed successfully
    with adminDriverRegister

    HTTP Args:
        aadhaarFront, aadhaarBack - aadhar scans
        licenseFront, licenseBack = driving license scans

    Notes:
        A registration token is returned to the client which is to be sent to
        isDriverVerified by the client while polling for registration status
        Registration has to be atomic since we save files
    '''

    sPhone = dct['phone']
    sAadharFrontFilename = saveTmpImgFile(settings.AADHAAR_DIR, dct['aadhaarFront'], 'front')
    sAadharBackFilename = saveTmpImgFile(settings.AADHAAR_DIR, dct['aadhaarBack'], 'back')

    sLicFrontFilename = saveTmpImgFile(settings.DL_DIR, dct['licenseFront'], 'front')
    sLicBackFilename = saveTmpImgFile(settings.DL_DIR, dct['licenseBack'], 'back')

    log('Driver Registration request - Aadhar images saved at %s, %s' % (sAadharFrontFilename, sAadharBackFilename))

    # Get aadhaar as 3 groups of 4 digits at a time via google vision api
    clientDetails = doOCR(sAadharFrontFilename)
    sAadhaar = clientDetails['an']
    log('Aadhaar number read from %s - %s' % (sAadharFrontFilename, sAadhaar))

    # verify aadhaar number via Verhoeff algorithm
    if not aadhaarNumVerify(sAadhaar):
        raise ZPException(501,'Aadhaar number not valid!')
    log('Aadhaar is valid')

    # Check if this driver exists
    qsDriver = Driver.objects.filter(an=sAadhaar)
    driverExists = len(qsDriver) != 0
    if not driverExists:
        driver = Driver()
        driver.an = int(sAadhaar)
        driver.pn = sPhone
        driver.name = clientDetails.get('name', '')
        driver.gdr = clientDetails.get('gender', '')
        driver.age = clientDetails.get('age', '')
        driver.mode = 'RG'

        # Dummy values set by admin team manually
        driver.dl = 'UK01-AB1234'
        driver.hs = 'UK'

        # No place set
        driver.pid = -1

        # Set a random auth so that this driver wont get authed
        driver.auth = str(random.randint(0, 0xFFFFFFFF))
        driver.save()

        # licenses are also stored with the aadhar in the file name but under settings.DL_DIR
        renameTmpImgFiles(settings.AADHAAR_DIR, sAadharFrontFilename, sAadharBackFilename, sAadhaar)
        renameTmpImgFiles(settings.DL_DIR, sLicFrontFilename, sLicBackFilename, sAadhaar)
        log('New driver registered: %s' % sAadhaar)
    else:
        # Only proceed if status is not 'RG' else throw error
        driver = qsDriver[0]
        if driver.mode != 'RG':
            # Aadhaar exists, if mobile has changed, get new auth
            if driver.pn != sPhone:
                driver.pn = sPhone
                sAuth =  getClientAuth(driver.an, driver.pn)
                log('Auth changed for driver: %s' % sAadhaar)
            else:
                # Aadhaar exists, phone unchanged, just return existing auth
                sAuth = driver.auth
                log('Auth exists for driver: %s' % sAadhaar)
            return HttpJSONResponse({'auth': sAuth})
        else:
            raise ZPException('Registration pending', 501)

    # Deterministic registration token will be checked by isDriverVerified
    ret = {'token': getClientAuth(sAadhaar, sPhone + '-register'), 'an': sAadhaar, 'pn': sPhone }
    return HttpJSONResponse(ret)


@makeView()
@csrf_exempt
@handleException(IndexError, 'Driver not found', 404)
@extractParams
def isDriverVerified(_, dct):
    '''
    Returns the drivers registration status and valid auth once the mode
    is changed from 'RG'
    The mode is changed by the call center after human verification of driver bona fides

    HTTP args:
        token : which was sent in response to  the driver registration request
        an : aadhaar number
        pn : phone number
    '''
    # Fetch this driver based on aadhaar - if confirmed, send the auth back
    driver = Driver.objects.filter(an=dct['an'])[0]
    ret = {'status': False}
    if driver.mode != 'RG' and dct['token'] == getClientAuth(dct['an'], dct['pn'] + '-register'):
        ret = {'status': True, 'auth': driver.auth}

    return HttpJSONResponse(ret)


##############################################################################
# User views
##############################################################################

# TODO: We need a way for drivers and users to select a vehicle type
# Currently we are not saving a requested vtype in Trip DB, only ZBEE

@makeView()
@csrf_exempt
#@handleException()
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@transaction.atomic
@checkAuth()
def userTripGetStatus(_dct, user):
    '''
    Gets the current trips detail for a user
    This must be polled continuously by the user app to detect any state change
    after a ride request is madega

    Returns:
        active(bool): Whether a trip is in progress
        status(str): Trip status
        tid(str): trip ID

        For each of the following statuses, additional data is returned:
            AS: otp, dan, van
            ST: progress (percent)
            TR, FN: price, time (seconds), dist (meters), speed (m/s average)

        Note: If active is false, no other data is returned
    '''
    # Get the users current trip if any
    if user.tid != -1:
        qsTrip = Trip.objects.filter(id=user.tid)
        trip = qsTrip[0]
        ret = {'st': trip.st, 'tid': trip.id, 'active': trip.st in Trip.USER_ACTIVE, 'rtype': trip.rtype}

        # For assigned trip send OTP, and 'an' of vehicle and driver
        if trip.st == 'AS':
            ret['otp'] = getOTP(trip.uan, trip.dan, trip.atime)
            #print(trip.van)
            vehicle = Vehicle.objects.filter(an=trip.van)[0]
            ret['vno'] = vehicle.regn
            #print(ret['vno'])
            if trip.rtype == '1':
                ret['price'] = getRentPrice(trip.srcid,  trip.dstid, vehicle.vtype, trip.pmode, trip.hrs)['price']
                currTime = datetime.now(timezone.utc)
                #print(currTime, trip.atime)
                diffTime = (currTime - trip.atime).total_seconds() // 60  # minutes
                #print(currTime - trip.atime, (currTime - trip.atime).total_seconds())
                ret['time'] = 30-diffTime
            
        # For started trips send trip progress percent
        # this is redundant, this functionality is provided by authProgressPercent()
        if trip.st == 'ST':
            #progress = Progress.objects.filter(tid=trip.id)[0]
            #ret['pct'] = progress.pct
            if trip.rtype == '1':
                currTime = datetime.now(timezone.utc)
                diffTime = (currTime - trip.stime).total_seconds() // 60 # minutes
                remHrs = trip.hrs*60 - diffTime
                ret['time'] = remHrs
                
            # TODO In case of rental make the rental send the number of minutes remaining .

        # For ended trips that need payment send the price data
        if trip.st in Trip.PAYABLE:
            if trip.rtype == '0':
                price = getTripPrice(trip)
            else : #renta
                vehicle = Vehicle.objects.filter(an=trip.van)[0]
                currTime = datetime.now(timezone.utc)
                diffTime = (currTime - trip.stime).total_seconds() // 60 # minutes
                remHrs = diffTime - trip.hrs 
                price = getRentPrice(trip.srcid,  trip.dstid, vehicle.vtype, trip.pmode, remHrs)

            ret.update(price)
    else:
        ret = {'active': False}

    return HttpJSONResponse(ret)


@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@handleException(IntegrityError, 'Null values sent', 501)
@extractParams
@transaction.atomic
@checkAuth()
@checkTripStatus(['INACTIVE'])
def userTripRequest(dct, user, _trip):
    '''
    User calls this to request a ride

    HTTP args:
    Ride :
        npas - number of passengers
        srcid - id of the selected start place
        dstid - id of the selected destination
        rtype - rent or ride
        vtype - vehicle type
        pmode - payment mode (cash / upi)

     Rent :
        srcid - id of the selected start place
        dstid - id of the selected destination
        rtype - rent or ride
        vtype - vehicle type
        pmode - payment mode (cash / upi)
        hrs   - number of hours
    '''
    print("Trip Request param : ", dct)

    # Even though we can use IDs directly, look them up in the DB to prevent bogus IDs
    placeSrc = Place.objects.filter(id=dct['srcid'])[0]
    placeDst = Place.objects.filter(id=dct['dstid'])[0]

    trip = Trip()
    trip.uan = user.an
    trip.srcid = placeSrc.id
    trip.dstid = placeDst.id
    if dct['rtype'] == '0': # Ride
        trip.npas = dct['npas']
    else: # Rent
        trip.npas = 2
        iHrs = int(dct['hrs'])
        trip.hrs = iHrs
        # this is again updated then the vehicle is actually assigned.

    trip.rtype = dct['rtype']
    trip.pmode = dct['pmode']
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
    ret = getRoutePrice(trip.srcid, trip.dstid, dct['vtype'], dct['pmode'])
    ret['tid'] = trip.id

    return HttpJSONResponse(ret)


@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@checkAuth()
@checkTripStatus('AS')
def userRideGetDriver(_dct, entity, trip):
    '''
    Returns aadhaar, name and phone of current assigned driver, TODO: Photo
    '''
    driver = Driver.objects.filter(an=trip.dan)[0]
    ret = {'pn': driver.pn, 'an': driver.an, 'name': driver.name}
    return HttpJSONResponse(ret)


@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@transaction.atomic
@checkAuth()
@checkTripStatus(['RQ', 'AS', 'ST'])
def userTripCancel(_dct, user, trip):
    '''
    Cancel the ride for a user if requested, assigned or started
    A Terminated trip will still require payment confirmation to end
    '''
    # Set the status of the trip to CN or TR based on current state
    trip.st = 'TR' if trip.st == 'ST' else 'CN'
    trip.etime = datetime.now(timezone.utc)
    trip.save()

    if trip.st == 'CN':
        retireEntity(user)

     # Do not set our trip ID to -1 or driver state because TR is like FN - needs payment
    return HttpJSONResponse({})


@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@transaction.atomic
@checkAuth()
@checkTripStatus(['TO', 'DN', 'PD'])
def userTripRetire(_dct, user, _trip):
    '''
    Resets users active trip
    This is called when the user has seen the message pertaining to trip end for these states:
    'TO', 'DN', 'PD',
    because these states are initiated by adminrefresh or driver actions

    Following states do not need user to retire trip
    CN : user has already retired in userRideCancel()
    FL : admin retires this via adminHandleFailedTrip()
    TR/FN : Driver will retire via driverConfirmPayment() after user pays money
    '''
    # reset the tid to -1
    retireEntity(user)
    return HttpJSONResponse({})


##############################################################################
# Auth views
##############################################################################

@makeView()
@csrf_exempt
@extractParams
@checkAuth()
def authVehicleGetAvail(_dct, entity):
    '''
    Returns the available vehicles at the pid of the entity
    '''
    # TODO give vehicles from the nearest "hub", say 5 km radius
    qsVehicles = Vehicle.objects.filter(tid=-1, dan=-1)
    ret = {'vehicles': [model_to_dict(vehicle) for vehicle in qsVehicles]}
    return HttpJSONResponse(ret)


@makeView()
@csrf_exempt
@extractParams
@checkAuth()
def authTripGetInfo(dct, entity):
    '''
    Returns trip info for this driver or user for any past or current trip
    '''
    # get the trip and ensure entity was in it
    trip = Trip.objects.filter(id=dct['tid'])[0]
    if not (trip.uan == entity.an or trip.dan == entity.an):
        raise ZPException('Invalid trip ID', 400)

    # get the trip and append pricing info if complete
    ret = {'st': trip.st}
    if trip.st in ['FN', 'TR', 'PD']:
        ret.update(getTripPrice(trip))
    return HttpJSONResponse(ret)


@makeView()
@csrf_exempt
@extractParams
@checkAuth()
def authPlaceGet(_dct, _entity):
    '''
    Returns a list of places data corresponding to zbee stations.
    '''
    getcontext().prec = 20
    qsPlaces = Place.objects.all().values()
    return HttpJSONResponse({'hublist': list(qsPlaces)})


@makeView()
@csrf_exempt
@handleException()
@extractParams
@transaction.atomic
@checkAuth()
@checkTripStatus(['AS', 'ST'])
def authTripFail(dct, entity, trip):
    '''
    Called by driver or user in an emergency which causes the trip to end
    '''
    # Note the time of trip cancel/fail and set state to failed
    trip.st = 'FL'
    trip.etime = datetime.now(timezone.utc)
    trip.save()

    # Reset the vehicle tid to failed so it wont be able to be selected
    vehicle = Vehicle.objects.filter(tid=trip.id)[0]
    vehicle.tid = Vehicle.FAILED
    vehicle.save()

    if trip.rtype == '0':
        # lock the driver
        driver = entity if type(entity) is Driver else Driver.objects.filter(an=trip.dan)[0]
        driver.mode = 'LK'
        driver.save()

    return HttpJSONResponse({})


@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@transaction.atomic
@checkAuth()
@checkTripStatus(['TO', 'CN', 'DN', 'PD'])
def authTripRetire(dct, entity, trip):
    '''
    Resets this entities active trip
    This is called when an entity has seen the message pertaining to trip end
    The entity causing the trip end does not call this method, since
    the methods ending the trip reset it directly
    'TO', 'CN', 'DN', 'PD', 'FL'

    In case of Driver reset mode and release vehicle
    '''
    print(dct, " | entity : ", entity, " |  trip :", trip)
    if type(entity) is Driver:
        entity.mode = 'AV'

        # Reset the vehicle tid to available
        vehicle = Vehicle.objects.filter(tid=trip.id)[0]
        vehicle.tid = Vehicle.AVAILABLE
        vehicle.save()

    retireEntity(entity)

    return HttpJSONResponse({})


@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@checkAuth()
def authLocationUpdate(dct, entity):
    '''
    Driver/User app calls this every 30 seconds to update the location table
    _____
    an  (driver/user/vehicle id: string) = Aadhaar number for user/driver table and id for vehicle
    lat  (latitude : float) = last Latitude of driver
    long (longitude: float) = last longitude of driver

    HTTP args:
    an: Aadhaar number for User/Driver, AN for Vehicle
    lat,lng: location
    auth: driver/user/vehicle auth token

    TODO: Add some sanity checks - check that d/dt delta from previous location is not unreasonably high!
    '''

    # Get the location object for this entity
    qsLoc = Location.objects.filter(an=entity.an)

    # Create or edit
    if len(qsLoc) == 0:
        recLoc = Location()
        recLoc.an = dct['an']
    else:
        recLoc = qsLoc[0]

    recLoc.kind = Location.KINDS[entity.__class__]
    recLoc.lat = dct['lat']
    recLoc.lng = dct['lng']
    recLoc.save()

    return HttpJSONResponse({})


@makeView()
@csrf_exempt
@handleException(Trip.DoesNotExist, 'Trip not found', 404)
@handleException(IndexError, 'No trip exists', 501)
@extractParams
@checkAuth()
@checkTripStatus(['FN','ST'])
def authProgressPercent(_dct, entity, trip):
    '''
    HTTPs args: auth of User or Driver
    Returns the progress % of the current Trip
    '''
    qsProg = Progress.objects.filter(tid=trip.id)
    return HttpJSONResponse({'pct': qsProg[0].pct, 'pid': entity.pid})

##############################################################################
# methods for simulation and admin
##############################################################################

@makeView()
@csrf_exempt
@extractParams
@transaction.atomic
@checkAuth()
def adminProgressAdvance(dct):
    '''
    This method advances the trip completion %age in the progress table
    also updates the locations of the 3 entities

    HTTP args:
        pct: how many % to advance
        tid: trip id
    '''
    prog = Progress.objects.get(tid=dct['tid'])

    currPct = prog.pct + int(dct['pct'])
    if currPct > 100:
        currPct = 100
    if currPct < 0:
        currPct = 0

    # Update all three entities location, set pid if 100%
    trip = Trip.objects.filter(id=dct['tid'])[0]
    #src = trip. Place.objects.filter(id=trip.srcid)[0]
    #dst = Place.objects.filter(id=trip.dstid)[0]

    x1 = trip.srclng #src.lng
    x2 = trip.dstlng # dst.lng
    y1 = trip.srclat #src.lat
    y2 = trip.dstlat #dst.lat

    f = currPct / 100
    x = x1 + (x2 - x1) * f
    y = y1 + (y2 - y1) * f


    def updateLoc(e, lat, lng):
        # Get the location object for this entity
        qsLoc = Location.objects.filter(an=e.an)

        # Create or edit
        if len(qsLoc) == 0:
            recLoc = Location()
            recLoc.an = e.an
        else:
            recLoc = qsLoc[0]

        recLoc.kind = Location.KINDS[e.__class__]
        recLoc.lat = lat
        recLoc.lng = lng
        recLoc.save()

    vehicle = Vehicle.objects.filter(an=trip.van)[0]
    user = User.objects.filter(an=trip.uan)[0]
    updateLoc(vehicle, y, x)
    updateLoc(user, y, x)
    if trip.rtype == '0':
        driver = Driver.objects.filter(an=trip.dan)[0]
        updateLoc(driver, y, x)

    prog.pct = currPct
    prog.save()

    return HttpJSONResponse({'lat': y, 'lng': x, 'pct': currPct})



@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@checkAuth()
def adminPlaceSet(dct):
    '''
    Adds or edits a place with name, lat, long, alt, wt
    '''
    name = dct['name']
    qsPlace = Place.objects.filter(pn=name)
    if len(qsPlace) == 0:
        rec = Place()
        rec.pn = name
    else:
        rec = qsPlace[0]

    rec.lat = dct['lat']
    rec.lng = dct['lng']
    rec.alt = dct['alt']
    rec.wt = dct['wt']
    rec.save()

    return HttpJSONResponse({})


@makeView()
@csrf_exempt
@handleException(IndexError, 'Place not found', 404)
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@checkAuth()
def adminPlaceDel(dct):
    '''
    Deletes a place given the name
    '''
    Place.objects.filter(pn=dct['name'])[0].delete()
    return HttpJSONResponse({})


@makeView()
@csrf_exempt
@handleException(JSONDecodeError, 'Invalid Admin Params', 501)
@extractParams
@transaction.atomic
@checkAuth()
def adminRefresh(dct):
    '''
    This is called periodically to perform any book-keeping tasks centrally:
    1) Handle trip request and trip start timeouts
    2) Update the location data for the ride share web page
    3) Update any data in the team dashboard
    '''
    qsTrip = Trip.objects.filter(st__in=['AS', 'RQ'])
    for trip in qsTrip:

        # For requested trips if settings.TRIP_RQ_TIMEOUT seconds passed since trip.rtime set to TO (timeout)
        if trip.st == 'RQ':
            tmDelta = datetime.now(timezone.utc) - trip.rtime
            if trip.rtype == '0' and tmDelta.total_seconds() > settings.RIDE_RQ_TIMEOUT:
                trip.st = 'TO'
            if trip.rtype == '1' and tmDelta.total_seconds() > settings.RENT_RQ_TIMEOUT:
                trip.st = 'TO'

        # For assigned trips if settings.TRIP_AS_TIMEOUT seconds passed since trip.atime set to TO (user times out)
        # this can be differentiated from above by simply looking at atime field, if not NULL, then Trip Timed Out after going into AS
        else:
            tmDelta = datetime.now(timezone.utc) - trip.atime
            if trip.rtype == '0' and tmDelta.total_seconds() > settings.RIDE_AS_TIMEOUT:
                trip.st = 'TO'
            if trip.rtype == '1' and tmDelta.total_seconds() > settings.RENT_AS_TIMEOUT:
                trip.st = 'TO'
        trip.save()

    # For every started trip update the trip progress based on the drivers last location
    # We will assume for now that drivers location is bona fide
    # For now ignore this, simulation changes %age manually
    '''qsTrip = Trip.objects.filter(st='ST')
    for trip in qsTrip:
        loc = Location.objects.filter(an=trip.dan)
        updateTrip(loc, trip)'''
    qsDel = Delivery.objects.filter(st__in=['AS', 'RQ'])
    for deli in qsDel:

        # For requested deliveries if settings.DEL_RQ_TIMEOUT seconds passed since del.rtime set to TO (timeout)
        if deli.st == 'RQ':
            tmDelta = datetime.now(timezone.utc) - deli.rtime
            if tmDelta.total_seconds() > settings.DEL_RQ_TIMEOUT:
                deli.st = 'TO'

        # For assigned deliveries if settings.DEL_AS_TIMEOUT seconds passed since deli.atime set to TO (user times out)
        # this can be differentiated from above by simply looking at atime field, if not NULL, then Delivery Timed Out after going into AS
        else:
            tmDelta = datetime.now(timezone.utc) - deli.atime
            if tmDelta.total_seconds() > settings.DEL_AS_TIMEOUT:
                deli.st = 'TO'
        deli.save()

    return HttpJSONResponse({})


@makeView()
@csrf_exempt
@extractParams
@checkAuth()
def adminRouteUpdate(dct):
    '''
    Sets the distance between places in Routes table via google
    '''
    getcontext().prec = 20
    qsPlaces = Place.objects.all().values()
    arrLocs = [recPlace for recPlace in qsPlaces]
    arrLocCoOrds = ['%s,%s' % (recPlace['lat'], recPlace['lng']) for recPlace in qsPlaces]

    #log(arrLocCoOrds)

    import googlemaps
    gmaps = googlemaps.Client(key=dct['gmaps_key'])
    dctDist = gmaps.distance_matrix(arrLocCoOrds, arrLocCoOrds)
    #log(dctDist)

    if dctDist['status'] != 'OK':
        raise ZPException(501, 'Error fetching distance matrix')

    ret = []
    n = len(dctDist['origin_addresses'])
    for i in range(0, n):
        for j in range(i + 1, n):
            dctElem = dctDist['rows'][i]['elements'][j]
            if dctElem['status'] == 'OK':
                # Update route table
                nDist = dctElem['distance']['value']
                idx, idy = arrLocs[i]['id'], arrLocs[j]['id']
                qsRoute = Route.objects.filter(idx=idx, idy=idy)
                if len(qsRoute) > 0:
                    recRoute = qsRoute[0]
                else:
                    recRoute = Route()
                    recRoute.idx = idx
                    recRoute.idy = idy

                recRoute.dist = nDist
                recRoute.save()

                # Add entry to return
                entry = [nDist, arrLocs[i]['pn'], arrLocs[j]['pn']]
                ret.append(entry)

    return HttpJSONResponse(ret)


@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@handleException(IntegrityError, 'Transaction error', 500)
@extractParams
@checkAuth()
def authAdminEntityUpdate(dct, entity):
    '''
    Updates details for a registered entity (needs admin privileges)

    HTTP args:
        auth: auth of entity to update
        adminAuth: admins authentication key
        *: Any fields that are relevant to the entity

    Note:
        Method requires the entity auth key as 'auth' for checkAuth,
        But also adminAuth so that only admins can call this
        This implies that auth cannot be changed by this method

        No checking is done for fields - passing an invalid field will be silently ignored by the DB
        TODO: add checking for valid fields
    '''
    # Check if adminAuth is valid, and remove the key
    #print("dictionary : ", dct)
    adminAuth = dct.pop('adminAuth')
    if adminAuth != settings.ADMIN_AUTH:
        return HttpJSONResponse('Forbidden', 403)

    for key, val in dct.items():
        setattr(entity, key, val)
        entity.save()

    return HttpJSONResponse({})


@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@handleException(IntegrityError, 'Transaction error', 500)
@extractParams
@checkAuth()
def adminVehicleUpdate(dct):
    '''
    Updates details for a vehicle

    HTTP args:
        van: vehicle to update
        *: Any fields that are relevant to the entity

    '''
    vehicle = Vehicle.objects.filter(an=dct['van'])[0]
    vehicle.tid = Vehicle.AVAILABLE
    vehicle.save()

    return HttpJSONResponse({})




@makeView()
@csrf_exempt
@handleException(IndexError, 'Driver not found', 404)
@handleException(KeyError, 'Invalid parameters', 501)
@handleException(IntegrityError, 'Transaction error', 500)
@extractParams
@checkAuth()
def adminDriverRegister(dct):
    '''
    Completes driver registration after background verification is done offline

    HTTP args:
        an: driver aadhar
        *: Any other fields that need to be updated/corrected (except state)

    Note:
        No checking is done for fields - passing an invalid field will be silently ignored by the DB
        Auth is generated and stored
        Driver state is set to 'OF'
    '''
    # Get the driver and create an auth
    recDriver = Driver.objects.filter(an=dct['an'])[0]
    if recDriver.mode != 'RG':
        raise ZPException('Driver is already registered', 501)

    dct['auth'] = getClientAuth(str(recDriver.an), str(recDriver.pn))
    dct['st'] = 'OF'

    for key, val in dct.items():
        setattr(recDriver, key, val)
        recDriver.save()

    return HttpJSONResponse({})


@makeView()
@csrf_exempt
@extractParams
@checkAuth()
def adminDataGet(dct):
    '''
    Generic function to retrieve DB data
    HTTP args:
      pk: primary key of data to retrieve - if None retrieve all
      table: one of the table names
    '''
    import zp.models
    table = getattr(zp.models, dct['table'])

    if 'pk' in dct:
        ret = table.objects.filter(pk=dct['pk']).values()
    else:
        ret = table.objects.all().values()

    ret = json.dumps(list(ret), cls=DjangoJSONEncoder)
    return HttpResponse(ret, content_type='application/json')


@makeView()
@csrf_exempt
@extractParams
@transaction.atomic
@checkAuth()
def adminHandleFailedTrip(dct):
    '''
    Returns entities whose last trip failed together
    '''
    rawQuery = Trip.objects.raw('''SELECT T.id as id, V.an as van, D.auth as dauth, U.auth as uauth FROM trip T INNER JOIN vehicle V ON T.van=V.an INNER JOIN driver D ON T.dan=D.an INNER JOIN user  U on T.uan=U.an WHERE T.st = 'FL' and (D.tid=T.id or V.tid = -2  or U.tid=T.id);''');
    lockedList = json.dumps([{'tid': fld.id,'dauth': fld.dauth, 'uauth': fld.uauth, 'van': fld.van} for fld in rawQuery], cls=DjangoJSONEncoder)
    return HttpResponse(lockedList, content_type='application/json')


################################################################################################
# Methods to check if needed

@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@checkAuth()
@checkTripStatus(['INACTIVE'])
def userTripEstimate(dct, user, _trip):
    '''
    Returns the estimated price for the trip

    HTTP args:
        rtype
        vtype
        npas
        srcid
        dstid
        pmode

        hrs
    '''
    print("Ride Estimate param : ", dct)
    if dct['rtype'] == '0':
        ret = getRoutePrice(user.pid, dct['dstid'], dct['vtype'], dct['pmode'])
    #should it not allow the prices from point A(source) to point B(destination) instead of taking the pid of user?
    else:
        #make this ask srcid as well
        ret = getRentPrice(dct['srcid'],  dct['dstid'], dct['vtype'], dct['pmode'], dct['hrs'])

    return HttpJSONResponse(ret)


@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@handleException(IntegrityError, 'Transaction error', 500)
@extractParams
@checkAuth()
def authProfileUpdate(dct, entity):
    '''
    Updates details for a registered entity (self auth)

    HTTP args:
        auth: auth of entity to update
        name: name of the entity
        gdr:  gender of the entity
    Note:
    '''
    entity.gdr = dct['gdr']
    entity.name = dct['name']
    entity.save()

    return HttpJSONResponse({})


@makeView()
@csrf_exempt
@handleException()
@extractParams
@transaction.atomic
@checkAuth()
def authTripHistory(dct, entity):
    '''
    returns the history of all Trips for a entity
    '''
    qsTrip = Trip.objects.filter(uan=entity.an).values() if type(entity) is User else Trip.objects.filter(dan=entity.an).values()
    ret = {}
    #print(qsTrip)
    #print("REEEEEEEEEEEE ",len(qsTrip))
    if len(qsTrip) :
        states = []

        for i in qsTrip:
            thisOneBro = {'id':i['id'],'st':i['st'], 'price':getTripPrice(Trip.objects.filter(id=i['id'])[0])['price']}#,

                          #TODO 'stime':i['stime'], 'etime':i['etime']}
            states.append(thisOneBro)
        print(states)
        ret.update({'trips':states})

    return HttpJSONResponse(ret)


@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@transaction.atomic
@checkAuth()
@checkTripStatus(None)
def userGiveOtp(dct, user, _trip):
    '''
    test method
    takes OTP or Price,
    writes to a file
    '''
    otp = dct.get('otp','')
    sOTPfile = '/srv/data/tests/otp' + '.' + str(user.tid)
    if len(otp) > 1:
        with open(sOTPfile, 'w+') as f:
            json.dump({'otp': otp}, f)

    price = dct.get('price','')
    sPriceFile = '/srv/data/tests/money' + '.' + str(user.tid)
    if len(price) > 1:
        with open(sPriceFile, 'w+') as f:
            json.dump({'payment': price}, f)
    return HttpJSONResponse({})

