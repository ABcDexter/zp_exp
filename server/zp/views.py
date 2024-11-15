# imports
import os
import json
import logging
import datetime
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

from .models import Rate, Supervisor
from .models import Driver, User, Vehicle, Delivery
from .models import Place, Trip, Progress, Location, Route

from .utils import HttpJSONError, ZPException, DummyException, HttpJSONResponse, HttpRecordsResponse, log
from .utils import saveTmpImgFile, doOCR, aadhaarNumVerify, getClientAuth, renameTmpImgFiles, getOTP, doOCRback
from .utils import getRoutePrice, getTripPrice, getRentPrice, getRidePrice, getRiPrice
from .utils import handleException, extractParams, checkAuth, checkTripStatus, retireEntity
from .utils import headers
from .utils import sendTripInvoiceMail, sendDeliveryInvoiceMail
from .utils import googleDistAndTime

from url_magic import makeView

from zp.view import schedule
from zp.view import rent, ride, deliver, pwa, shop, service

from hypertrack.rest import Client
from hypertrack.exceptions import HyperTrackException

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
def registerUserNoAadhaar(_, dct: Dict):
    '''
    User registration
    Creates a user record for the aadhar number OCR'd from the image via Google Vision
    Aadhaar scans are archived in settings.AADHAAR_DIR as
    <aadhaar>_front.jpg and <aadhaar>_back.jpg
    -----------------------------------------

    HTTP Args:
        name: name of the user
        phone; phone number of the user without the ISD code
        home: home state of the user
        gender: gender of the user
    -----------------------------------------
    Response:
        dictionary of the User data
    -----------------------------------------

    Notes:
        Registration is done atomically since we also need to save aadhar scans after DB write
    '''

    log('User Registration request. Dct : %s ' % (str(dct)))

    sPhone = str(dct['phone'])
    sAn = str(91) + sPhone
    sAuth = getClientAuth(sAn, sPhone)

    qsUser = User.objects.filter(an=int(sAn))
    bUserExists = len(qsUser) != 0
    if not bUserExists:
        sAuth = getClientAuth(sAn, sPhone)
        user = User()
        user.name = dct['name']
        # user.age = int(clientDetails['age'])
        user.gdr = dct['gender']
        user.auth = sAuth
        user.an = int(sAn)
        user.pn = sPhone
        user.hs = dct['home'].lower()
        user.fcm = dct['fcm']
        user.save()
        log('New user registered: %s' % user.name)
    else:
        # "91+mobile" exists, check what has been changed
        user = qsUser[0]
        if user.pn != sPhone:
            sAuth = getClientAuth(str(qsUser[0].sAn), str(qsUser[0].pn))
            user.pn = sPhone
            user.auth = sAuth
            user.fcm = dct['fcm']
            user.save()
            log('Auth changed for: %s' % user.name)
        else:
            # Aadhaar exists, phone unchanged, just return existing auth
            user.fcm = dct['fcm']
            user.save()
            sAuth = user.auth
            log('Auth exists for: %s' % user.name)

    # return the whole user record
    return HttpJSONResponse(model_to_dict(user))


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
    qsUser = User.objects.filter(adhar=int(sAadhaar))
    bUserExists = len(qsUser) != 0
    if not bUserExists:
        sAuth = getClientAuth(sAadhaar, sPhone)
        user = User()
        user.name = clientDetails['name']
        user.an = str(91) + sPhone
        user.age = int(clientDetails['age'])
        user.gdr = clientDetails['gender']
        user.auth = sAuth
        user.adhar = int(sAadhaar)
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
        driver.fcm = dct['fcm']
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
                driver.fcm = dct['fcm']
                driver.save()
                sAuth =  getClientAuth(driver.an, driver.pn)
                log('Auth changed for driver: %s' % sAadhaar)
            else:
                # Aadhaar exists, phone unchanged, just return existing auth
                driver.fcm = dct['fcm']
                driver.save()
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
    -----------------------------------------
    HTTP args :
        token : which was sent in response to  the driver registration request
        an : aadhaar number
        pn : phone number
    -----------------------------------------
    Response:
        status: true or false
        auth : iff the status is true
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

@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@transaction.atomic
@checkAuth()
def userTripGetStatus(_dct, user):
    '''
    Gets the current trips detail for a user
    This must be polled continuously by the user app to detect any state change
    after a ride request is made
    -----------------------------------------
    HTTP args:
        auth : auth key of the user
    -----------------------------------------

    Returns:
        active(bool): Whether a trip is in progress
        status(str): Trip status
        tid(str): trip ID

        For each of the following statuses, additional data is returned:
            AS: otp, dan, van
            ST: progress (percent)
            TR, FN: price, time (seconds), dist (meters), speed (m/s average)
    -----------------------------------------

        Note: If active is false, no other data is returned
    '''
    # Get the users current trip if any
    if user.tid != -1:
        qsTrip = Trip.objects.filter(id=user.tid)
        trip = qsTrip[0]
        ret = {'st': trip.st, 'tid': trip.id, 'active': trip.st in Trip.USER_ACTIVE, 'rtype': trip.rtype}

        # For assigned trip send OTP, and 'an' of vehicle and driver
        if trip.st == 'AS':

            # print(ret['vno'])
            locDriver = Location.objects.filter(an=trip.dan)[0]
            locUser = Location.objects.filter(an=trip.uan)[0]
            # print(locDriver,locDriver.lat,locDriver.lng, locUser, locUser.lat, locUser.lng)

            srcCoOrds = ['%s,%s' % (locDriver.lat, locDriver.lng)]  # Driver's location
            dstCoOrds = ['%s,%s' % (locUser.lat, locUser.lng)]
            # print('################',dstCoOrds)

            # print(srcCoOrds, dstCoOrds)
            gMapsRet = googleDistAndTime(srcCoOrds, dstCoOrds)
            nDist, nTime = gMapsRet['dist'], gMapsRet['time']

            if trip.rtype == '1':  # RENTAL
                ret['price'] = getRentPrice(trip.hrs)['price']
                currTime = datetime.now(timezone.utc)
                # print(currTime, trip.atime)
                diffTime = (currTime - trip.atime).total_seconds() // 60  # minutes
                ret['time'] = 30-diffTime

            else:  # RIDE
                ret['time'] = nTime
                ret['otp'] = getOTP(trip.uan, trip.dan, trip.atime)
                # print(trip.van)
                vehicle = Vehicle.objects.filter(an=trip.van)[0]
                ret['vno'] = vehicle.regn  # moves to ST state

        # For started trips send trip progress percent
        # this is redundant, this functionality is provided by authProgressPercent()
        if trip.st == 'ST':
            # progress = Progress.objects.filter(tid=trip.id)[0]
            # ret['pct'] = progress.pct
            if trip.rtype == '1':
                vehicle = Vehicle.objects.filter(an=trip.van)[0]
                currTime = datetime.now(timezone.utc)
                diffTime = (currTime - trip.stime).total_seconds() // 60  # converted to minutes
                remTimeMins = trip.hrs*60 - diffTime
                ret['time'] = int(remTimeMins)
                ret['vno'] = vehicle.regn  # moves to ST state

        # For ended trips that need payment send the price data
        if trip.st in Trip.PAYABLE:  # FN, TR states
            if trip.rtype == '0':
                if trip.st == 'TR':
                    rate = Rate.objects.filter(id='ride'+str(trip.id))[0]
                    price = rate.money
                else :
                    price = getTripPrice(trip)['price']

            else:  # rental
                remPrice = int(float(getTripPrice(trip)['price'])-float(getRentPrice(trip.hrs)['price']))
                price = str(remPrice) + '.00' if remPrice >= 0 else '0.00'

            ret['price'] = price
        
        if trip.st not in ['RQ', 'TO']:
            # send photo of the current driver or supervisor
            dAuth = Driver.objects.filter(an=trip.dan)[0].auth if trip.rtype == '0' else Supervisor.objects.filter(an=trip.dan)[0].auth
            ret['photourl'] = "https://api.zippe.in:8090/media/dp_" + dAuth + "_.jpg"

    # else the trip is not active
    else:
        ret = {'active': False, 'st': 'NONE', 'tid': -1}

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
    User calls this to request a ride/rent
    -----------------------------------------

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
    -----------------------------------------
    Response:
        tid: trip id
        price: how much money did it cost

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
    else:  # Rent
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

    ret = getRentPrice(dct['hrs'])
    ret['tid'] = trip.id

    return HttpJSONResponse(ret)


@makeView()
@csrf_exempt
@handleException(IndexError, 'Driver NOT found', 501)
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@checkAuth()
@checkTripStatus('AS')
def userRideGetDriver(_dct, _entity, trip):
    '''
    Returns aadhaar, name and phone of current assigned driver
    -----------------------------------------
    HTTP args :
        auth : auth key of the user
    -----------------------------------------
    Response:
        pn : phone number of driver
        an : an of the driver
        name : name of driver
        photourl : photo of driver
    -----------------------------------------
    Note : static images don't work with Product On in settings
    '''
    driver = Driver.objects.filter(an=trip.dan)[0]
    ret = {'pn': driver.pn, 'an': driver.an, 'name': driver.name, 'photourl' : "https://api.zippe.in:8090/media/dp_" + driver.auth + "_.jpg"}
    return HttpJSONResponse(ret)


@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@transaction.atomic
@checkAuth()
@checkTripStatus(['SC','RQ', 'AS', 'ST'])
def userTripCancel(_dct, user, trip):
    '''
    Cancel the ride for a user if requested, assigned or started
    A Terminated trip will still require payment confirmation to end
    -----------------------------------------
    HTTP args :
        auth : auth key
    -----------------------------------------
    Response:
        {}
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
def userTripRetire(_dct, user, trip):
    '''
    Resets users active trip
    This is called when the user has seen the message pertaining to trip end for these states:
    'TO', 'DN', 'PD',
    because these states are initiated by adminrefresh or driver actions

    Following states do not need user to retire trip
    CN : user has already retired in userRideCancel()
    FL : admin retires this via adminHandleFailedTrip()
    TR/FN : Driver will retire via driverConfirmPayment() after user pays money
    -----------------------------------------
    HTTP args :
        auth : auth key
    -----------------------------------------
    Response:
        {}
    '''
    # reset the tid to -1
    retireEntity(user)
    
    return HttpJSONResponse({})


##############################################################################
# Auth views
##############################################################################

@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@checkAuth()
def authVehicleGetAvail(_dct, entity):
    '''
    Returns the available vehicles at the pid of the entity
    -----------------------------------------
    HTTP args :
        auth : auth key
    -----------------------------------------
    Response:
        vehicles: list of vehicles
    '''
    # TODO give vehicles from the nearest "hub", say 5 km radius
    qsVehicles = Vehicle.objects.filter(tid=-1, dan=-1)
    ret = {'vehicles': [model_to_dict(vehicle) for vehicle in qsVehicles]}
    return HttpJSONResponse(ret)


@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@checkAuth()
def authTripGetInfo(dct, entity):
    '''
    Returns trip info for this driver or user for any past or current trip
    -----------------------------------------
    HTTP args :
        auth : auth key
    -----------------------------------------
    Response:
        price: price for the trip
        st : status of the trip
        rtype: 0 for ride, 1 for rent
    -----------------------------------------
    Note : give remaining price when the Trip is in FN, TR, PD
    '''
    # get the trip and ensure entity was in it
    trip = Trip.objects.filter(id=dct['tid'])[0]
    if not (trip.uan == entity.an or trip.dan == entity.an):
        raise ZPException('Invalid trip ID', 400)

    # get the trip and append pricing info if complete
    ret = {'st': trip.st, 'rtype':trip.rtype}
    if trip.st in ['FN', 'TR', 'PD']:
        remPrice = int(float(getTripPrice(trip)['price']) - float(getRentPrice(trip.hrs)['price']))
        price = str(remPrice) + '.00' if remPrice >= 0 else '0.00'
        ret.update({'price': price})

    if trip.st == 'TR' and trip.rtype=='0':
        rate = Rate.objects.filter(id='ride'+str(trip.id))[0]
        ret['price'] = rate.money

    return HttpJSONResponse(ret)


@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@checkAuth()
def authPlaceGet(_dct, _entity):
    '''
    Returns a list of places data corresponding to zbee stations.
    -----------------------------------------
    HTTP args :
        auth : auth key
    -----------------------------------------
    Response:
        hublist : list of places
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
    -----------------------------------------
    HTTP args :
        auth : auth key
    -----------------------------------------
    Response :
        {}
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
    -----------------------------------------
    HTTP args :
        auth : auth key
    -----------------------------------------
    Response:
        {}
    -----------------------------------------
    Note:
        In case of Driver reset mode and release vehicle
    '''
    print(dct, " | entity : ", entity, " |  trip :", trip)
    if type(entity) is Driver:
        entity.mode = 'AV'

        # Reset the vehicle tid to available
        vehicle = Vehicle.objects.filter(tid=trip.id)[0]
        vehicle.tid = Vehicle.AVAILABLE
        vehicle.save()
    else:
        import yagmail
        from codecs import encode
        eP_S_W_D = encode(str(settings.GM_PSWD), 'rot13')

        receiver = str(entity.email)
        body = "Hi, \n Your Trip costed Rs " + str(getTripPrice(trip)['price'])+"\n Thanks for riding with Zippe!\n -VillageConnect"
        # attachment = "some.pdf"
        yag = yagmail.SMTP("villaget3ch@gmail.com", eP_S_W_D)
        yag.send( to = receiver, subject = "Zippe bill email ", contents = body)

    
    retireEntity(entity)

    return HttpJSONResponse({})


@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
#@handleException(IndexError, 'Invalid an', 502)
@extractParams
@checkAuth()
def authLocationUpdate(dct, entity):
    '''
    Driver/User app calls this every 30 seconds to update the location table
    -----------------------------------------
        an  (driver/user/vehicle id: string) = Aadhaar number for user/driver table and id for vehicle
        lat  (latitude : float) = last Latitude of driver
        long (longitude: float) = last longitude of driver

    HTTP args :
        auth: driver/user/vehicle auth token
        an: Aadhaar number for User/Driver, AN for Vehicle
        lat,lng: location
    -----------------------------------------
    Response :
            {}
    -----------------------------------------
    TODO: Add some sanity checks - check that d/dt delta from previous location is not unreasonably high!
    '''
    if not dct['lat'] or not dct['lng']:
        print(type(entity) , " with an :", entity.an , "sent empty lat/lng")
        return HttpJSONResponse({})

    # Get the location object for this entity
    qsLoc = Location.objects.filter(an=entity.an)

    # Create or edit
    if len(qsLoc) == 0:
        recLoc = Location()
        recLoc.an = dct['an'] if 'an' in dct else '0'
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
    -----------------------------------------
    HTTP args :
        auth : auth key of the ADMIN
        name :
        lat  :
        lng  :
        alt  :
        wt   :
    -----------------------------------------
    Respose:
        {}
    -----------------------------------------
    Note : see Place table
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
    -----------------------------------------
    HTTP args :
        auth : auth key of ADMIN
        name :
    -----------------------------------------
    Response :
        {}
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
    4) Handle delivery request and timeouts
    -----------------------------------------
    HTTP args :
        auth : auth key of the ADMIN
    -----------------------------------------

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
        # this can be differentiated from above by simply looking at atime field,
        # if not NULL, then Trip Timed Out after going into AS
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
    -----------------------------------------
    HTTP args :
        adminAuth : auth key of ADMIN
        auth: auth of entity to update
        adminAuth: admins authentication key
        *: Any fields that are relevant to the entity
    -----------------------------------------

    Note:
        Method requires the entity auth key as 'auth' for checkAuth,
        But also adminAuth so that only admins can call this
        This implies that auth cannot be changed by this method

        No checking is done for fields - passing an invalid field will be silently ignored by the DB
        TODO: add checking for valid fields
    '''
    # Check if adminAuth is valid, and remove the key
    # print("dictionary : ", dct)
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

    -----------------------------------------
    HTTP args :
        auth : auth key of ADMIN
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
    -----------------------------------------
    HTTP args :
        auth : auth key
        an: driver aadhar
        *: Any other fields that need to be updated/corrected (except state)
    -----------------------------------------

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
    -----------------------------------------
    HTTP args :
        auth : auth key
    -----------------------------------------
    Response :
        lockedList : list of locked entities
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
    -----------------------------------------
    HTTP args :
        auth : auth key
        rtype
        vtype
        npas
        srcid
        dstid
        pmode

        hrs
    -----------------------------------------
    Response:
        price:

    '''
    print("Ride Estimate param : ", dct)
    if dct['rtype'] == '0':
        ret =  getRidePrice(dct['srclat'], dct['srclng'], dct['dstlat'], dct['dstlng'], dct['vtype'], dct['pmode'], 0)
        #getRoutePrice(user.pid, dct['dstid'], dct['vtype'], dct['pmode'])

    else:
        #ret = getRentPrice(dct['srcid'],  dct['dstid'], dct['vtype'], dct['pmode'], dct['hrs'])
        ret = getRentPrice(dct['hrs'])

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
    -----------------------------------------
    HTTP args :
        auth: auth of entity to update
        name: name of the entity
        gdr:  gender of the entity
    -----------------------------------------
    Response :
        {}
    -----------------------------------------
    Note: the keys should be there in the dct otherwise they won't be updated
    '''
    if 'gdr' in dct:
        entity.gdr = dct['gdr']
    if 'name' in dct:
        entity.name = dct['name']
    if 'email' in dct:
        entity.email = dct['email']

    # only for servitor
    if 'job1' in dct:
        entity.job1 = dct['job1']
    if 'job2' in dct:
        entity.job2 = dct['job2']
    if 'job3' in dct:
        entity.job3 = dct['job3']

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
    if len(qsTrip) :
        states = []

        for i in qsTrip:
            # print(str(i['stime'])[:19])
            strSTime = str(i['stime'])[:21]
            strETime = str(i['etime'])[:21]
            sTime = datetime.strptime(strSTime, '%Y-%m-%d %H:%M:%S.%f').date()
            if i['st'] in ['FN', 'TR', 'PD']:
                eTime = datetime.strptime(strETime, '%Y-%m-%d %H:%M:%S.%f').date()
            else :
                eTime = 'ONGOING'
            ithTrip = {'id':i['id'],'st':i['st'], 'price':getRiPrice(Trip.objects.filter(id=i['id'])[0])['price'],
                       'sdate': str(sTime),
                       'edate': str(eTime)
                       }
            states.append(ithTrip)
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


@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@transaction.atomic
@checkAuth()
@checkTripStatus(['PD'])
def authTripRate(dct, entity, trip):
    '''
    rating system...
    -----------------------------------------
    HTTP args :
        auth : auth key
        rate : good or bad
        rev  : review in a bit detail
    -----------------------------------------
    Response:
        {}

    '''
    print(dct, entity, trip)

    bIsUser = True if type(entity) is User else False  # user or driver
    print(bIsUser)
    if bIsUser :
        if trip.rtype == '0':
            driver = Driver.objects.filter(an=trip.dan)[0]
            numTrips = Trip.objects.filter(dan=driver.an).count()
            driver.mark = (driver.mark+int(dct['rate']))/(numTrips+1)
            driver.save()

            rate = Rate.objects.filter(id='ride' + str(trip.id))[0]

        else:
            # rate the super visor
            # give the ratings
            rate = Rate()
            rate.id = 'rent' + str(trip.id)
            rate.type = 'rent'
            rate.money = float(getTripPrice(trip)['price'])

        rate.rev = dct['rev']
        rate.dan = trip.dan

        if 'attitude' in dct['rev'].lower():
            rate.rating = 'attitude'
        elif 'condition' in dct['rev'].lower():
            rate.rating = 'vehiclecon'
        elif 'clean' in dct['rev'].lower():
            rate.rating = 'cleanliness'
        else:
            rate.rating = dct['rev']

        rate.save()

    else :
        user = User.objects.filter(an=trip.uan)[0]
        numTrips = Trip.objects.filter(uan=user.an).count()
        user.mark = (user.mark+int(dct['rate']))/(numTrips+1)
        user.save()

    #think maybe mark the trip as RATED, do we need an extra state ...?
    # NOPE as per 30/7/2020

    return HttpJSONResponse({})


@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@transaction.atomic
@checkAuth()
def authProfilePhotoSave(dct, entity):
    '''
    entity sends and saves his/her profile photo

    -----------------------------------------
    HTTP Args:
        auth        : auth of the entity
        profiePhoto : Display photo base 64 encoded
    -----------------------------------------
    Response :
        {}
    -----------------------------------------

    Notes:
        needs production settings
    '''
    #from codecs import encode
    #entityAdhar = encode(str(entity.auth), 'rot13')
    #entityAdhar = encode(entityAdhar, 'rot13') #could be removed
    entityAdhar = str(entity.auth)
    sProfilePhoto = saveTmpImgFile(settings.PROFILE_PHOTO_DIR, dct['profilePhoto'], 'dp')
    log('Profile photo saved for - %s saved as %s' % (entityAdhar, sProfilePhoto))
    dpFileName = 'dp_' + entityAdhar + '_.jpg'
    os.rename(sProfilePhoto, os.path.join(settings.PROFILE_PHOTO_DIR, dpFileName))
    log('New photo saved: %s' % dpFileName)
    return HttpJSONResponse({})


@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@transaction.atomic
@checkAuth()
def authAadhaarSave(dct, entity):
    '''
    entity sends and saves his/her profile photo

    -----------------------------------------

    HTTP Args:
        auth
        Aadhaar photo base 64 encoded
            aadhaarFront, aadhaarBack
    -----------------------------------------
    Respnse:
        {}
    -----------------------------------------
    Notes:
        needs production settings
    '''
    
    sAadharFrontTemp = saveTmpImgFile(settings.AADHAAR_DIR, dct['aadhaarFront'], 'front-tmp')
    sAadharBackTemp = saveTmpImgFile(settings.AADHAAR_DIR, dct['aadhaarBack'], 'back-tmp')
    entityAdhar = str(entity.an)
    log('Aadhaar photo saved for - %s saved as %s' % (entityAdhar, sAadharFrontTemp))
    
    # Get aadhaar as 3 groups of 4 digits at a time via google vision api
    clientDetails = doOCR(sAadharFrontTemp)
    sAadhaar = clientDetails['an']
    log('Aadhaar number read from %s - %s' % (sAadharFrontTemp, sAadhaar))
    clientDetails2 = doOCRback(sAadharBackTemp)
    sAadhaar2 = clientDetails2['an']
    log('Aadhaar number read from %s - %s' % (sAadharBackTemp, sAadhaar2))

    # verify aadhaar number via Verhoeff algorithm
    if not aadhaarNumVerify(sAadhaar):
        raise ZPException(501,'Aadhaar number not valid!')
    log('Aadhaar is valid')

    if sAadhaar != sAadhaar2:
        raise ZPException(501, 'Aadhaar number front doesn\'t match Aadhaar number back!')

    entity.hs = clientDetails2['hs']
    entity.save()   

    sAdharFrontFileName = 'ad_' + entityAdhar + '_front.jpg'
    sAdharBackFileName = 'ad_' + entityAdhar + '_back.jpg'
    log('HS updated for : %s | %s' % (entity.an, entity.hs))

    os.rename(sAadharFrontTemp, os.path.join(settings.AADHAAR_DIR, sAdharFrontFileName))
    os.rename(sAadharBackTemp, os.path.join(settings.AADHAAR_DIR, sAdharBackFileName))
    log('New photo saved: front %s | back %s' % (sAdharFrontFileName, sAdharBackFileName))
    return HttpJSONResponse({})



@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
#@handleException(IndexError, 'Invalid trip', 501) #should NOT tell the API user whether a trip exists or not
@extractParams
@checkAuth()
def authTripData(dct, _entity):
    '''
    Returns all the data of a Trip

    -----------------------------------------
    HTTP args :
        auth : auth key
        tid : trip id
    -----------------------------------------
    Response:
        returns:
      # for user
      
      # for driver
      # date
      # pickup point #can be dine
      # pickup time (stime) #sdate and rdate are same
      # cancel 0 or 1 whether SC state is there
    -----------------------------------------

    '''
    trip = Trip.objects.filter(id=dct['tid']).values('id','st','uan','dan','van','rtime','stime','etime','srcid','dstid','srclat','srclng','dstlat','dstlng','hrs','rtype','rvtype','srcname','dstname')
    lstTrip = list(trip)
    dctTrip = lstTrip[0]
    
    #for key, val in dctTrip.items():
    #    dctRet.update({str(key): str(val)})
    
    strRTime = str(dctTrip['rtime'])[:19]
    rDate = datetime.strptime(strRTime, '%Y-%m-%d %H:%M:%S').date().strftime('%d-%m-%Y')
    sTime = 'None'

    srchub = dctTrip['srcname'] #Place.objects.filter(id=dctTrip['srcid'])[0].pn
    dsthub = dctTrip['dstname'] #Place.objects.filter(id=dctTrip['dstid'])[0].pn


    if dctTrip['rtype'] == '1' :
            # rental
            time = float(dctTrip['hrs']*60)

            idxNext = [0, 60, 120, 180, 240, 300, 300, 420, 480, 540, 600, 660, 720]
            #             780, 840, 900, 960, 1020, 1080, 1140, 1200, 1260, 1320, 1380, 1440]  # for next hours charges

            idxPrice = [0.00, 1.00, 0.90, 0.80, 0.75, 0.70, 0.65, 0.60, 0.55, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50]
            iTimeActualMins = int(time)

            try:
                idxMul = next(x[0] for x in enumerate(idxNext) if x[1] >= iTimeActualMins)  # Find the correct value from idx
            except StopIteration:
                idxMul = 12

            rate = idxPrice[idxMul]

    else:
            #ride
            if dctTrip['stime'] is None:
                #ride has not yet started
                sTime = 'notStarted'
                time = 0.00
            else :
                #ride has started, save start time
                strSTime = str(dctTrip['stime'])[:19]
                sTime = datetime.strptime(strSTime, '%Y-%m-%d %H:%M:%S')
            
                if dctTrip['etime'] is None:
                    #ride has not yet ended
                    eTime = 'notEnded'
                    time = 1.00
            
                else:
                    #ride has ended
                    strETime = str(dctTrip['etime'])[:19]
                    eTime = datetime.strptime(strETime, '%Y-%m-%d %H:%M:%S').date()
                    time = int(((dctTrip['etime'] - dctTrip['stime']).seconds)/60)

            rate = settings.RIDE_PER_MIN_COST * Vehicle.TIME_FARE[int(dctTrip['rvtype'])]

    time = float(time)
    print(time, rate, rate*time)
    price = rate*time  # 2 chars
    tax = price*0.05   # tax of 5%
    cost = price - tax
    total = cost + tax
    print(tax, total)
    sc = 1 if dctTrip['st'] == 'SC' else 0
    dctRet = {
            'cancel' : sc,
            'id': str(dctTrip['id']),
            'st':str(dctTrip['st']),
            #'uan': str(dctTrip['uan']),
            #'dan': str(dctTrip['dan']),
            'van': str(dctTrip['van']),
            'sdate': str(rDate),
            'srchub': str(srchub),
            'srclat': str(dctTrip['srclat']),
            'srclng':str(dctTrip['srclng']),

            'dsthub': str(dsthub),
            'dstlat':str(dctTrip['dstlat']),
            'dstlng':str(dctTrip['dstlng']),
            
            'rtype':str(dctTrip['rtype']),
            'rvtype': str(dctTrip['rvtype']),
            'stime': str(sTime),
            
            'time': str(time),
            'rate': str(rate),
            'price': str(round(float('%.2f' % cost), 0))+'0',
            'tax':  str(round(float('%.2f' % tax), 0))+'0',
            'total': str(round(float('%.2f' % total), 0))+'0',
            
             }
    
    return HttpJSONResponse(dctRet)


@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@handleException(IndexError, 'Invalid request', 501)
@extractParams
def bankValidateData(_, dct:Dict):
    '''
        Https:
             "auth" : auth designed for bank, as stated in settings file
            
        returns:
            status : True
    '''
    if dct['auth'] == settings.BANK_AUTH:
        return HttpJSONResponse({'status': 200, 'done':True, 'error':None })
    else:
        raise ZPException(502,'Auth not valid!')


@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@handleException(HyperTrackException, 'No route found', 501)
@extractParams
@transaction.atomic
@checkAuth()
@checkTripStatus(['ST'])
def userTripTrack(dct, user, trip):
    '''
    Generates a hypertrack url for the current trip
    -----------------------------------------
    HTTP args :
        auth : auth of the entity (user)
        devid: device id of the android device from hypertrack SDK

    -----------------------------------------
    Response:
        htid: trip_id of the hypertrack
        hurl : url of the live location link form hypertrack

    '''
    if trip.url == None:
        hypertrack = Client(settings.HYPERTRACK_ACCOUNT_ID , settings.HYPERTRACK_SECRET_KEY)
        userName = User.objects.filter(an=trip.uan)[0].name

        hypertrack.devices.change_name(dct['devid'], userName)

        trip_data = {"device_id": dct['devid'],
                     "destination": {"geometry": {"type": "Point", "coordinates": [trip.dstlng, trip.dstlat] }}}
                    # NOTE : [29.34856700, 79.5446500]}}} this takes longitude, latitude instead of lat,lng

        htrip = hypertrack.trips.create(trip_data)
        trip.htid = htrip['trip_id']
        trip.url = htrip['views']['share_url']
        trip.save()
        resp = {'htid': trip.htid, 'hurl': trip.url}
    else:
        resp = {'hurl': trip.url}

    return HttpJSONResponse(resp)
