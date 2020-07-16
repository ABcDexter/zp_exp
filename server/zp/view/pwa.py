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
from ..models  import Place, Trip, Progress, Location, Route

from ..utils import HttpJSONError, ZPException, DummyException, HttpJSONResponse, HttpRecordsResponse, log
from ..utils import saveTmpImgFile, doOCR, aadhaarNumVerify, getClientAuth, renameTmpImgFiles, getOTP, doOCRback
from ..utils import getRoutePrice, getTripPrice, getRentPrice
from ..utils import handleException, extractParams, checkAuth, checkTripStatus, retireEntity
from ..utils import headers

from url_magic import makeView

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
                             "redirect_url":"https:/webapp-zippe.web.app/choice.html"})

