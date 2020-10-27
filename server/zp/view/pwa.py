############################################################
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

from ..models import Driver, User, Vehicle, Delivery
from ..models  import Place, Trip, Progress, Location, Route, Rate

from ..utils import HttpJSONError, ZPException, DummyException, HttpJSONResponse, HttpRecordsResponse, log
from ..utils import saveTmpImgFile, doOCR, aadhaarNumVerify, getClientAuth, renameTmpImgFiles, getOTP, doOCRback
from ..utils import getRoutePrice, getTripPrice, getRentPrice
from ..utils import handleException, extractParams, checkAuth, checkTripStatus, retireEntity
from ..utils import headers

from url_magic import makeView

from ..utils import extract_lat_lng, getRidePrice, getRiPrice
import googlemaps

############################################################
# Extra

# @headers({'Access-Control-Allow-Origin': '*'})



@makeView()
@csrf_exempt
# @headers({'Refresh': '10', 'X-Bender': 'Bite my shiny, metal ass!'})
@handleException(KeyError, 'Invalid parameters', 501)
@transaction.atomic
@extractParams
def signUser(_, dct: Dict):
    '''
    User registration
    Creates a user record for the aadhar number OCR'd from the image via Google Vision
    Aadhaar scans are archived in settings.AADHAAR_DIR as
    <aadhaar>_front.jpg and <aadhaar>_back.jpg

    HTTP Args:
        name, an, phone, age, gender, hs

    Notes:
        Registration is done atomically since we also need to save aadhar scans after DB write
    '''
    print("DICT is :", dct)
    sPhone = str(dct['phone'])
    sAadhaar = str(dct['an'])
    sAuth = getClientAuth(sAadhaar, sPhone)
    sName = dct['name']
    iAge = int(dct['age'])
    sGdr = dct['gender']
    sHs = dct['hs']

    # verify aadhaar number via Verhoeff algorithm
    if not aadhaarNumVerify(sAadhaar):
        raise ZPException(501, 'Aadhaar number not valid!')
    log('Aadhaar is valid')

    # Check if aadhaar has been registered before
    qsUser = User.objects.filter(an=int(sAadhaar))
    bUserExists = len(qsUser) != 0
    if not bUserExists:
        sAuth = getClientAuth(sAadhaar, sPhone)
        user = User()
        user.name = sName
        user.age = iAge
        user.gdr = sGdr
        user.auth = sAuth
        user.an = int(sAadhaar)
        user.pn = sPhone
        user.hs = sHs
        # from the POST request

        user.pid = int('1')
        user.tid = int('-1')
        user.did = int("-1")
        user.dl = 'UK-1234'
        user.save()

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

    # return the padding for CORS
    return HttpJSONResponse({'auth': sAuth, 'callback': dct['callback'], '_': dct['_'],
                             'redirect':True,
                             #"redirect_url":"http://localhost:5005/choice.html"})
                             "redirect_url": "choice.html"})


@makeView()
@csrf_exempt
@handleException(googlemaps.exceptions.TransportError, 'Internet Connectivity Problem', 503)
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@checkAuth()
@checkTripStatus(['INACTIVE'])
def userGRideEstimate(dct, _user, _trip):
    '''
    Returns the estimated price for the trip

    HTTP args:
        srcname,
        dstname
        vtype
        npas
        pmode

        hrs=0 for ride
    '''
    print("Google Ride Estimate param : ", dct)
    srclat, srclng = extract_lat_lng(dct['srcname'])
    dstlat, dstlng = extract_lat_lng(dct['dstname'])
    print(srclat, srclng, " | ", dstlat, dstlng)
    ret = getRidePrice(srclat, srclng, dstlat, dstlng, dct['vtype'], dct['pmode'], 0)
    return HttpJSONResponse(ret)



@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@handleException(IntegrityError, 'Null values sent', 501)
@extractParams
@transaction.atomic
@checkAuth()
#@checkTripStatus(['INACTIVE'])
def userGRideRequest(dct, user):#, _trip):
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
    srclat, srclng = extract_lat_lng(dct['srcname'])
    dstlat, dstlng = extract_lat_lng(dct['dstname'])
    print(srclat, srclng, " | ", dstlat, dstlng)

    trip.srclat, trip.srclng = srclat, srclng
    trip.dstlat, trip.dstlng = dstlat, dstlng
    trip.srcid = user.pid
    trip.dstid = user.pid+1
    if dct['rtype'] == '0': # Ride
        trip.npas = dct['npas']
        trip.srcid, trip.dstid = 0,0

    else: # Rent
        trip.npas = 2
        iHrs = 2 #int(dct['hrs'])
        trip.hrs = iHrs
        # this is again updated then the vehicle is actually assigned.

    trip.rtype = dct['rtype']
    trip.pmode = dct['pmode']
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
    ret = getRiPrice(trip) #getRoutePrice(trip.srcid, trip.dstid, dct['vtype'], dct['pmode'])
    ret['tid'] = trip.id

    if trip.rtype=='0':
        rate = Rate()
        rate.id = 'ride' + str(trip.id)
        rate.type = 'ride' if trip.rtype == '0' else 'rent'
        rate.rev = ''
        rate.money = float(getRidePrice(srclat, srclng, dstlat, dstlng, dct['vtype'], dct['pmode'], 0)['price'])
        rate.save()        


    return HttpJSONResponse(ret)



