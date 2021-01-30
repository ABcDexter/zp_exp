# imports
import datetime
from datetime import datetime, timedelta
from decimal import getcontext
import random
from django.db import transaction
from django.utils import timezone
from django.views.decorators.csrf import csrf_exempt
from django.conf import settings
from django.db.utils import OperationalError, IntegrityError

from url_magic import makeView
from ..models import Location
from ..models import User, Product, Rate, Servitor
from ..utils import ZPException, HttpJSONResponse, saveTmpImgFile, doOCR, log, aadhaarNumVerify, renameTmpImgFiles, \
    googleDistAndTime
from ..utils import getOTP
from ..utils import handleException, extractParams, checkAuth, retireDelEntity, getClientAuth

import googlemaps
from ..utils import extract_name_from_pin, getClientAuth
from django.forms.models import model_to_dict
import json
import urllib.request
from woocommerce import API
from ast import literal_eval
from ..models import Job

###########################################
# Types
Filename = str


###########################################
# Constants

makeView.APP_NAME = 'zp'


# ============================================================================
# Services auth views
# ============================================================================


@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@handleException(IndexError, 'No bookings', 502)
@extractParams
@checkAuth()
def authBookingGet(_dct, _entity):
    '''
    get all the bookings from woocommerce

    HTTP Args:
        auth : auth of the entity

    Respose:
    bookings : [
        {
            bid                 : booking id
            "start"             : Starting date and time of the booking
            "end"               : Ending date and time of the booking
            "job"               : type of the job
            "customer_name"     : customer name,
            "customer_phone"    : customer phone number
            "customer_address"  : customer address with PIN code
            "customer_note"     : cusotmer_note if any
        }
    ]
    '''
   
    #WooCommerce update
    wcapi = API(url="https://zippe.in", consumer_key=settings.WP_CONSUMER_KEY, consumer_secret=settings.WP_CONSUMER_SECRET_KEY, version="wc/v3")
    ret = wcapi.get("orders?per_page=100") # get 100 requests
    # print(ret.status_code)
    ordersResp = []
    for i in ret.json():
        orig = "'" + str(i['line_items']).replace('\'', '\"')[1:-1] + "'"

        rep = orig.replace("None", "\"None\"").replace("False", "\"False\"").replace("True", "\"True\"")

        from ast import literal_eval
        a = literal_eval(rep)
        y = json.loads(a)

        zz = str(y['meta_data']).replace('\'','\"')[1:-1]

        arrOrder = json.loads( "[" + zz + "]")
        z = arrOrder[0]

        x = i
        if 'start' in z['value']:
            orders = {
                      #"servitor": arrOrder,
                      "bid" : x['id'],
                      "job": x['line_items'][0]['name'],

                      "start": str(datetime.strptime(z['value']['start']['date'][:-7], '%Y-%m-%d %H:%M:%S')),
                      "end" : str(datetime.strptime(z['value']['end']['date'][:-7], '%Y-%m-%d %H:%M:%S')),

                      #"billing": i['billing'],
                      "customer_name": i['billing']['first_name'] + " " + i['billing']['last_name'] ,
                      "customer_phone": i['billing']['phone'],
                      "customer_note": i['customer_note'],
                      "customer_address": i['billing']['address_1'] + " , " + i['billing']['address_2'] + " , " +
                                          i['billing']['city'] + " , " + i['billing']['state'] + " , " +
                                          i['billing']['postcode']

            }
            #print(orders)
            ordersResp.append(orders)
    #print(ordersResp)

    return HttpJSONResponse({"booking":ordersResp})

# ============================================================================
# Servitor views
# ============================================================================

@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@checkAuth()
def servitorBookingGet(_dct, servitor):
    '''
    get the bookings for servitor

    HTTP Args:
        auth : auth of the entity

    Respose:
    bookings : [
        {
            bid                 : booking id
            "start"             : Starting date and time of the booking
            "end"               : Ending date and time of the booking
            "job"               : type of the job
            "customer_name"     : customer name,
            "customer_phone"    : customer phone number
            "customer_address"  : customer address with PIN code
            "customer_note"     : cusotmer_note if any
        }
    ]
    '''

    # WooCommerce update
    wcapi = API(url="https://zippe.in", consumer_key=settings.WP_CONSUMER_KEY,
                consumer_secret=settings.WP_CONSUMER_SECRET_KEY, version="wc/v3")
    ret = wcapi.get("orders?per_page=10") #get 10 orders
    # print(ret.status_code)
    ordersRelevant = []
    ordersOther = []
    for i in ret.json():
        orig = "'" + str(i['line_items']).replace('\'', '\"')[1:-1] + "'"

        rep = orig.replace("None", "\"None\"").replace("False", "\"False\"").replace("True", "\"True\"")

        from ast import literal_eval
        a = literal_eval(rep)
        y = json.loads(a)

        zz = str(y['meta_data']).replace('\'', '\"')[1:-1]

        arrOrder = json.loads("[" + zz + "]")
        z = arrOrder[0]

        x = i
        if 'start' in z['value']:
            orders = {
                # "servitor": arrOrder,
                "bid": x['id'],
                "job": x['line_items'][0]['name'],

                "start": str(datetime.strptime(z['value']['start']['date'][:-7], '%Y-%m-%d %H:%M:%S')),
                "end": str(datetime.strptime(z['value']['end']['date'][:-7], '%Y-%m-%d %H:%M:%S')),

                # "billing": i['billing'],
                "customer_name": i['billing']['first_name'] + " " + i['billing']['last_name'],
                "customer_phone": i['billing']['phone'],
                "customer_note": i['customer_note'],
                "customer_address": i['billing']['address_1'] + " , " + i['billing']['address_2'] + " , " +
                                    i['billing']['city'] + " , " + i['billing']['state'] + " , " +
                                    i['billing']['postcode']

            }
            # print(orders)
            # print(orders['job'], servitor.job1,servitor.job2, servitor.job3, servitor.job4, servitor.job5)
            if orders['job'] in [servitor.job1, servitor.job2, servitor.job3]:
                ordersRelevant.append(orders)
            elif orders['job'] in [servitor.job4, servitor.job5]:
                ordersOther.append(orders)

    # print(ordersResp)

    return HttpJSONResponse({"booking": ordersRelevant, 'other':ordersOther})




@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@transaction.atomic
@extractParams
def registorServitor(_, dct):
    '''
    Servitor registration
    makes the Servitor register with phone number

    HTTP Args:
        1. name         : name of Servitor
        2. pn           : phone number without the ISD code
        3. photo        : selfie photo in base64
        4. aadhaarFront : Front of the aadhaar
           aadhaarBack  : Back of the aadhaar
        5. gdr          : gender
        6. ps           : name of the nearest Police station

        7. job1         : primary job of servitor (Compulsary)
           job2         : secondary job of servitor
           job3         : tertiary job of servitor

        8,9?
        hs           : home state (this we can automatically pick from aadhaar card)
        bank         : Bank details of the servitor


    Resposnse:
        auth : auth key of the servitor
        name : name of the servitor

    '''

    # save the details of Servitor
    sPhone = str(dct['pn'])
    sAn = '91' + sPhone
    sName = str(dct['name'])
    sJob1 = str(dct['job1'])
    sJob2 = str(dct['job2']) if 'job2' in dct else ''
    sJob3 = str(dct['job3']) if 'job3' in dct else ''
    sJob4 = str(dct['job4']) if 'job4' in dct else ''
    sJob5 = str(dct['job5']) if 'job5' in dct else ''

    # address proof
    sAadharFrontFilename = saveTmpImgFile(settings.AADHAAR_DIR, dct['aadhaarFront'], 'ser_' + sPhone + '_front')
    sAadharBackFilename = saveTmpImgFile(settings.AADHAAR_DIR, dct['aadhaarBack'], 'ser_' + sPhone + '_back')
    log('Servitor Registration request - Aadhaar images saved at %s, %s' % (sAadharFrontFilename, sAadharBackFilename))

    # bank details
    sBank = str(dct['bank']) if 'bank' in dct else ''

    # picture
    sPhotoFileName = saveTmpImgFile(settings.PROFILE_PHOTO_DIR, dct['photo'], 'dp_' + sPhone)
    log('User Registration request - Aadhar images saved at %s' % (sPhotoFileName))

    # check whether servitor with the same phone number exists or not
    qsServitor = Servitor.objects.filter(pn=sPhone)

    bServitorExists = len(qsServitor) != 0
    if not bServitorExists:
        log('Servitor not registered with phone : %s' % (dct['pn']))

        servitor = Servitor()
        servitor.an = sAn
        servitor.auth = getClientAuth(sAn, sPhone)[:6]  # shortened the auth key
        servitor.name = sName
        servitor.pn = sPhone
        servitor.bank = sBank
        servitor.job1 = sJob1
        servitor.job2 = sJob2
        servitor.job3 = sJob3
        servitor.job4 = sJob4
        servitor.job5 = sJob5
        servitor.ps = dct['ps']
        servitor.save()

        ret= {'auth': servitor.auth, 'name': servitor.name, 'an': servitor.an}
    else:
        log('Auth exists for: %s, %s' % (dct['pn'], qsServitor[0].name))

        qsServitor[0].job1 = sJob1
        qsServitor[0].job2 = sJob2
        qsServitor[0].job3 = sJob3
        qsServitor[0].job4 = sJob4
        qsServitor[0].job5 = sJob5
        qsServitor[0].ps = dct['ps']

        qsServitor[0].save()

        ret = {'status': True, 'auth': qsServitor[0].auth, 'an': qsServitor[0].an, 'pn': qsServitor[0].pn,
               'name': qsServitor[0].name}
    return HttpJSONResponse(ret)



@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@transaction.atomic
@extractParams
def loginServitor(_, dct):
    '''
    Servitor login
    makes the Servitor login with phone number
    
    HTTP Args:
        pn  : phone number of the Servitor without the ISD code
        key : rot13(auth) of Servitor

    Response :
        status : true or false depending on whether the login was successful

        if true, then

        auth: auth key of the servitor
        an: an of the servitor
        pn: phone number of the servitor
        name: name of the servitor

    '''

    sPhone = str(dct['pn'])
    from codecs import encode
    sAuth = encode(str(dct['key']), 'rot13')

    qsServitor = Servitor.objects.filter(auth=sAuth, pn=sPhone)
    bServitorExists = len(qsServitor) != 0
    if not bServitorExists:
        log('Servitor not registered with phone : %s' % (dct['pn']))
        return HttpJSONResponse({'status':'false'})
    else:
        log('Auth exists for: %s' % (dct['pn']))
        ret = {'status': True, 'auth':qsServitor[0].auth, 'an': qsServitor[0].an, 'pn': qsServitor[0].pn,
               'name': qsServitor[0].name}
        return HttpJSONResponse(ret)



@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@checkAuth()
def servitorJobsList(dct, servitor):
    '''
    Get the list of the 5 jobs that this servitor can get

    HTTP params:
        auth
    Response:
        json of 5 jobs

    '''
    return HttpJSONResponse({'job1': str(servitor.job1), 'job2': str(servitor.job2), 'job3': str(servitor.job3),
                            'job4': str(servitor.job4), 'job5': str(servitor.job5)})


@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@checkAuth()
def authJobGet(dct, entity):
    '''
    Get the list of all jobs from Job table for this entity

    HTTP params:
        auth

    Reponse:
        json Object which contains the list of all jobs

    '''

    qsJob = Job.objects.all().values('id', 'jname', 'jtype')
    return HttpJSONResponse({'job': list(qsJob)})


