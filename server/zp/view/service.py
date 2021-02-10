# imports
import os
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
from ..models import Job, Booking

from ..utils import encode, decode
from ..models import Rate

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


@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@checkAuth()
def authBookingSync(dct, entity):
    '''
    updated and syncs the mysql DB id with that from woocommerce

    HTTP params:
        auth
    '''

    def pros(m, n):
        wcapi = API(url="https://zippe.in", consumer_key="ck_97e691622c4bd5e13fb7b18cbb266c8277257372",
                    consumer_secret="cs_63badebe75887e2f94142f9484d06f257194e2c3", version="wc/v3")
        ret = wcapi.get("orders/?page=" + str(m) + "&per_page=" + str(n))
        print(ret.status_code)
        jobs = ["Nurse", "Doctor", "Chartered Accountant", "Accountant","Lawyer","Surveyor","Civil Engineer","Architect","Designer","Photographer","Teacher / Home Tutor","Fitness Trainer","Yoga Trainer","Dietician","Motor Training","Chef","Cook","Maid","Driver","Gardener","Errand Person","Deep Cleaning","House Keeping","Sweeper","Security Guard","Dry Cleaning","Laundry","Delivery","Tailor","Inspection Visit","Appliance Repair","Computer Repair Technician","Electrician","Plumber","Carpenter","Painter","Mason", "Masons Helper","Aluminium Fabricator","Iron Fabricator","Caterer","Mechanic","Coach","Towing Service"]

        ans = []
        for i in ret.json():
            x = i
            if i['line_items'][0]['name'] in jobs:
                print(i['id'], i['line_items'][0]['name'])
                resp = {"order_number": i['id'],
                        "order_status": i['status'],
                        "order_date": datetime.strptime(i['line_items'][0]['meta_data'][0]['value']['start']['date'][:], "%Y-%m-%d %H:%M:%S.%f"),
                        "customer_note": i['customer_note'],

                        "first_name_billing" : i['billing']['first_name'],
                        "last_name_billing" : i['billing']['last_name'],
                        # "company_Billing" : i['billing']['company'],  # CAN BE NULL
                        "address_1_2_billing" : i['billing']['address_1'] + " , " + i['billing']['address_2'] ,
                        "city_billing" : i['billing']['city'],
                        "state_code_billing": i['billing']['state'],
                        "postcode_billing": i['billing']['postcode'],
                        "country_code_billing": i['billing']['country'],
                        "email_billing" : i['billing']['email'],
                        "phone_billing": i['billing']['phone'],

                        "order_total_amount": i['line_items'][0]['total'],

                        "item_Name": 1,
                        "item_Name": i['line_items'][0]['name']

                }

            ans.append(resp)
        # print("ans :", ans)
        return ans

    ret = []
    for i in range(1, 3):
        resp = pros(str(i), str(20))
        ret += resp
        # time.sleep()

    # print(ret)
    status = 'false'
    from django.db import connection
    cursor = connection.cursor()
    for i in ret:
        try:
            # qsNextHubs = Product.objects.raw('update product set id = %s where sku = %s;', [ret[i], i])
            # cursor.execute('update product set id = %s where sku = %s;', [ret[i], i])
            #print(i)
            command = 'INSERT INTO booking(order_status,order_date,customer_note,first_name_billing,last_Name_billing,company_billing,address_1_2_billing,city_billing,state_code_billing,postcode_billing,country_code_billing,email_billing,phone_billing,first_name_shipping,last_name_shipping,address_1_2_shipping,city_shipping,state_code_shipping,postcode_shipping,country_code_shipping,payment_method_title,cart_discount_amount,order_subtotal_amount,shipping_method_title,order_shipping_amount,order_refund_amount,order_total_amount,order_total_tax_amount,sku,item_qty,item_name,quantity,item_cost,coupon_code,discount_amount,discount_amount_tax, order_number,rtime, status) VALUES ("%s", NOW(), NULL,"%s","%s",NULL,"%s","%s","%s","%s","%s","%s",%s,NULL,NULL,NULL,NULL,NULL,NULL,NULL,"%s",0,10,NULL,0,0,10,0,NULL,1,"%s",1,10,NULL,NULL,NULL,%s, NOW(),"%s");' \
                      % (i['order_status'], i['first_name_billing'], i['last_name_billing'], i['address_1_2_billing'], i['city_billing'], i['state_code_billing'], i['postcode_billing'], i['country_code_billing'], i['email_billing'], i['phone_billing'], "Pay with UPI QR Code", i['item_Name'], i['order_number'], "PROC")
            # print(command)
            print("################")
            cursor.execute(command)
        except IntegrityError:
            print('Order with ID : %s didn\'t get updated' % i)
        status = 'true'

    return HttpJSONResponse({'status': status})


# ============================================================================
# Servitor views
# ============================================================================

@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@checkAuth()
def servitorBookingCheck(_dct, servitor):
    '''

    Args:
        _dct:
        servitor:

    Returns:
        count

    '''
    qsBooking = Booking.objects.filter(item_name__in=[servitor.job1, servitor.job2,servitor.job3])
    return HttpJSONResponse({'count': str(len(qsBooking))})


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

    '''
    # WooCommerce
    wcapi = API(url="https://zippe.in", consumer_key=settings.WP_CONSUMER_KEY,
                consumer_secret=settings.WP_CONSUMER_SECRET_KEY, version="wc/v3")
    ret = wcapi.get("orders?per_page=10")  # get 10 orders
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
                "bid": x['id'],
                "job": x['line_items'][0]['name'],

                "date": str(datetime.strptime(z['value']['start']['date'][:-7], '%Y-%m-%d %H:%M:%S'))[:10],
                "time": str(datetime.strptime(z['value']['end']['date'][:-7], '%Y-%m-%d %H:%M:%S'))[11:-3],
                "hours": str(datetime.strptime(z['value']['end']['date'][:-7], '%Y-%m-%d %H:%M:%S') -
                             datetime.strptime(z['value']['start']['date'][:-7], '%Y-%m-%d %H:%M:%S')).split(":")[0],
                "earn": "500"
            }
            # print(orders)
            # print(orders['job'], servitor.job1,servitor.job2, servitor.job3, servitor.job4, servitor.job5)
            if orders['job'].upper().lower() in [servitor.job1.upper().lower(), servitor.job2.upper().lower(),
                                                 servitor.job3.upper().lower()]:
                ordersRelevant.append(orders)
            elif orders['job'].upper().lower() in [servitor.job4.upper().lower(), servitor.job5.upper().lower()]:
                ordersOther.append(orders)
    
    # print(ordersResp)
    '''
    qsBooking = Booking.objects.all().values('order_number', 'item_name', 'order_date', 'order_date')
    # qsOrder = Booking.objects.filter( item_name__in=[servitor.job1, servitor.job2,servitor.job3])

    ordersRelevant = []
    if len(qsBooking):
        for ith in qsBooking:
            order = {}
            if str(ith['item_name']).upper().lower() in [servitor.job1.upper().lower(), servitor.job2.upper().lower(),
                                                         servitor.job3.upper().lower()]:
                order = {'bid': ith['order_number'], 'job': ith['item_name'],
                         'date': str(ith['order_date'])[:10], 'time': str(ith['order_date'])[11:-9], 'earn': 500}

                ordersRelevant.append(order)
    return HttpJSONResponse({"booking": ordersRelevant,
                             })


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
        log('Servitor not found with phone : %s' % (dct['pn']))

        servitor = Servitor()
        servitor.an = sAn
        servitor.auth = getClientAuth(sAn, sPhone)[:5]  # shortened the auth key of proper decoding base 62 into base 10
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
        log('Servitor registered with phone : %s' % (dct['pn']))

        dpFileName = 'dp_' + servitor.auth + '_.jpg'
        os.rename(sPhotoFileName, os.path.join(settings.PROFILE_PHOTO_DIR, dpFileName))
        log('New photo saved: %s' % dpFileName)

        ret = {'auth': servitor.auth, 'name': servitor.name, 'an': servitor.an, 'pn': servitor.pn}
    else:
        log('Auth exists for: %s, %s' % (dct['pn'], qsServitor[0].name))

        qsServitor[0].job1 = sJob1
        qsServitor[0].job2 = sJob2
        qsServitor[0].job3 = sJob3
        qsServitor[0].job4 = sJob4
        qsServitor[0].job5 = sJob5
        qsServitor[0].ps = dct['ps']

        qsServitor[0].save()

        dpFileName = 'dp_' + qsServitor[0].auth + '_.jpg'
        os.rename(sPhotoFileName, os.path.join(settings.PROFILE_PHOTO_DIR, dpFileName))
        log('New photo saved: %s' % dpFileName)

        ret = {'auth': qsServitor[0].auth, 'an': qsServitor[0].an, 'pn': qsServitor[0].pn,
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
        key : base 62 encoded auth of Servitor

    Response :
        status : true or false depending on whether the login was successful

        if true, then

        auth: auth key of the servitor
        an: an of the servitor
        pn: phone number of the servitor
        name: name of the servitor

    '''

    sPhone = str(dct['pn'])
    # from codecs import encode
    # sAuth = encode(str(dct['key']), 'rot13')  # rot13 of the auth  #NOT safe enough

    sAuth = encode(str(dct['key']), settings.BASE62)
    # print("serv auth Key : ", sAuth, "phone :", sPhone)
    qsServitor = Servitor.objects.filter(auth=sAuth, pn=sPhone)
    bServitorExists = len(qsServitor) != 0
    if not bServitorExists:
        log('Servitor not registered with phone : %s' % (dct['pn']))
        return HttpJSONResponse({'status': 'false'})
    else:
        log('Auth exists for: %s' % (dct['pn']))
        ret = {'status': True, 'auth': qsServitor[0].auth, 'an': qsServitor[0].an, 'pn': qsServitor[0].pn,
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


@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@checkAuth()
def servitorOrderGet(_dct, servitor):
    '''

    HTTP params:
        auth

    '''

    # getcontext().prec = 1000
    qsBooking = Booking.objects.all().values()
    return HttpJSONResponse({'orders': list(qsBooking)})


@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@checkAuth()
def servitorBookingData(dct, servitor):
    '''

    HTTP params:
        auth:
        bid

    '''
    '''
    # getcontext().prec = 1000
    wcapi = API(url="https://zippe.in", consumer_key=settings.WP_CONSUMER_KEY,
                consumer_secret=settings.WP_CONSUMER_SECRET_KEY, version="wc/v3")
    ret = wcapi.get("orders/" + dct['bid'])
    # print(ret.status_code)

    i = ret.json()
    orig = "'" + str(i['line_items']).replace('\'', '\"')[1:-1] + "'"

    rep = orig.replace("None", "\"None\"").replace("False", "\"False\"").replace("True", "\"True\"")

    from ast import literal_eval
    a = literal_eval(rep)
    y = json.loads(a)

    zz = str(y['meta_data']).replace('\'', '\"')[1:-1]

    arrOrder = json.loads("[" + zz + "]")
    z = arrOrder[0]

    x = i
    resp={}
    if 'start' in z['value']:
        orders = {
            # "servitor": arrOrder,
            "bid": x['id'],
            "job": x['line_items'][0]['name'],

            "start": str(datetime.strptime(z['value']['start']['date'][:-7], '%Y-%m-%d %H:%M:%S')),
            "end": str(datetime.strptime(z['value']['end']['date'][:-7], '%Y-%m-%d %H:%M:%S')),

            "date": str(datetime.strptime(z['value']['start']['date'][:-7], '%Y-%m-%d %H:%M:%S'))[:10],
            "time": str(datetime.strptime(z['value']['end']['date'][:-7], '%Y-%m-%d %H:%M:%S'))[11:-3],
            "hours": str(datetime.strptime(z['value']['end']['date'][:-7], '%Y-%m-%d %H:%M:%S') -
                         datetime.strptime(z['value']['start']['date'][:-7], '%Y-%m-%d %H:%M:%S')).split(":")[0],
            "area": i['billing']['address_2'] + " , " +  i['billing']['city'],

            "earn": "500",
            # "billing": i['billing'],
            "customer_name": i['billing']['first_name'] + " " + i['billing']['last_name'],
            "customer_phone": i['billing']['phone'],
            "customer_note": i['customer_note'],
            "customer_address": i['billing']['address_1'] + " , " + i['billing']['address_2'] + " , " +
                                i['billing']['city'] + " , " + i['billing']['state'] + " , " +
                                i['billing']['postcode']

        }
        #print(orders)
        resp = orders
    print(resp)
    '''
    booking = Booking.objects.filter(order_number=dct['bid'])[0]

    resp = {}

    if booking.item_name.upper().lower() in [servitor.job1.upper().lower(), servitor.job2.upper().lower(),
                                             servitor.job3.upper().lower()]:
        resp = {
                'bid': booking.order_number,
                'job': booking.item_name,
                'date': str(booking.order_date)[:10],
                'time': str(booking.order_date)[11:-9],
                'hours': '2',
                'area': str(booking.address_1_2_billing),
                'earn': 500,
                'customer_note': str(booking.customer_note),
                'customer_address': str(booking.address_1_2_billing) + " " + str(booking.city_billing) + " " +
                                    str(booking.postcode_billing) + " " + str(booking.state_code_billing),
                'customer_name': str(booking.first_name_billing) + " " + str(booking.last_name_billing),
                'customer_phone': str(booking.phone_billing)
        }

    return HttpJSONResponse(resp)


@makeView()
@csrf_exempt
@handleException(IndexError, 'Booking not found', 404)
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@transaction.atomic
@checkAuth()
def servitorBookingStart(dct, _serv):
    '''
    Servitor calls this to start the Booking providing the OTP that the user shared
    HTTP Args:
        OTP,
        bid
    '''
    qsBooking = Booking.objects.filter(order_number=dct['bid'])
    booking = qsBooking[0]

    if str(dct['otp']) == '1243':
        print(dct['otp'], str(getOTP(booking.uan, booking.servan, booking.rtime)))
        booking.order_status = 'START'
        booking.status = 'START'
        booking.stime = datetime.now(timezone.utc)
        booking.save()
    else:
        raise ZPException(403, 'Invalid OTP')

    return HttpJSONResponse({})


@makeView()
@csrf_exempt
@handleException(IndexError, 'Booking not found', 404)
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@transaction.atomic
@checkAuth()
def servitorBookingCancel(dct, _serv):
    '''
    Servitor calls this to cancel the Booking
    HTTP Args:
        auth
        bid
    '''
    qsBooking = Booking.objects.filter(order_number=dct['bid'])
    booking = qsBooking[0]

    booking.status = 'CANC'
    booking.order_status = 'CANC'
    booking.etime = datetime.now(timezone.utc)
    booking.save()

    return HttpJSONResponse({})


@makeView()
@csrf_exempt
@handleException(IndexError, 'Booking not found', 404)
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@transaction.atomic
@checkAuth()
def servitorBookingEnd(dct, serv):
    '''
    Servitor calls this to end the Booking in a normal manner
    HTTP Args:
        auth
        bid
    '''
    qsBooking = Booking.objects.filter(order_number=dct['bid'])
    booking = qsBooking[0]

    booking.status = 'DONE'
    booking.order_status = 'DONE'
    booking.etime = datetime.now(timezone.utc)
    booking.save()

    user = User.objects.filter(an=booking.uan)[0]

    params = {"to": str(user.fcm), "notification": {
        "title": "ZIPPE kar lo...",
        "body": "Your Service has been marked as completed by the service provider. Please give a rating.",
        "imageUrl": "https://cdn1.iconfinder.com/data/icons/christmas-and-new-year-23/64/Christmas_cap_of_santa-512.png",
        "gameUrl": "https://i1.wp.com/zippe.in/wp-content/uploads/2020/10/seasonal-surprises.png"
    }
              }
    dctHdrs = {'Content-Type': 'application/json',
               'Authorization': 'key=AAAA9ac2AKM:APA91bH7N4ocS711aAjNHEYvKo6TZxQ702CWSMqrBBILgAb2hPnZzo2byOb_IHUgHaFCG3xZyKUHH6p8VsUBsXwpfsXKwhxiqtgUSjWGkweKvAcb5p_08ud-U7e3PUIdaC6Sz-TGhHZB'}
    jsonData = json.dumps(params).encode()
    sUrl = 'https://fcm.googleapis.com/fcm/send'
    req = urllib.request.Request(sUrl, headers=dctHdrs, data=jsonData)
    jsonResp = urllib.request.urlopen(req, timeout=30).read()
    ret = json.loads(jsonResp)

    return HttpJSONResponse({})


@makeView()
@csrf_exempt
@handleException(IndexError, 'Booking not found', 404)
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@checkAuth()
def servitorJobsAccepted(_dct, serv):
    '''
    Servitor calls this to get the Bookings which are ongoing or yet to come
    HTTP Args:
        auth
        bid
    '''
    qsBooking = Booking.objects.filter(servan=serv.an)

    currBookings = Booking.objects.filter(order_number=serv.coid)

    status = False

    if len(currBookings):
        currBooking = currBookings[0]
        currentBooking = {
                          'bid': currBooking.order_number,
                          'job': currBooking.item_name,
                          'date': str(currBooking.order_date)[:10],
                          'time': str(currBooking.order_date)[11:-9],
                          'hours': '2',
                          'area': str(currBooking.address_1_2_billing),
                          'earn': 500
        }
        status = True

    else:
        currentBooking = {}

    upcomingBookings = []
    today = datetime.now(timezone.utc)

    for i in qsBooking:

        if i.order_date > today:

            data = {
                'bid': i.order_number,
                'job': i.item_name,
                'date': str(i.order_date)[:10],
                'time': str(i.order_date)[11:-9],
                'hours': '2',
                'area': str(i.address_1_2_billing),
                'earn': 500
            }
            upcomingBookings.append(data)

    if not status:
        return HttpJSONResponse({'status': status,  'upcoming': upcomingBookings})
    else:
        return HttpJSONResponse({
            'status': status,

            'bid': currentBooking['bid'],
            'job': currentBooking['job'],
            'date': currentBooking['date'],
            'time': currentBooking['time'],
            'hours': currentBooking['hours'],
            'area': currentBooking['area'],
            'earn': currentBooking['earn'],

            'upcoming': upcomingBookings
        })


@makeView()
@csrf_exempt
@handleException(IndexError, 'Booking not found', 404)
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@checkAuth()
def servitorJobsCompleted(_dct, serv):
    '''
    Servitor calls this to get the Bookings which are completed
    HTTP Args:
        auth
    '''
    qsBooking = Booking.objects.filter(servan=serv.an)

    pastBookings = []

    if len(qsBooking):
        today = datetime.now(timezone.utc)

        for i in qsBooking:
            data = {
                          'bid': i.order_number,
                          'job': i.item_name,
                          'date': str(i.order_date)[:10],
                          'time': str(i.order_date)[11:-9],
                          'hours': '2',
                          'earn': 500
            }
            if i.order_date < today:
                pastBookings.append(data)

    return HttpJSONResponse({'past': pastBookings})


# ============================================================================
# User views
# ============================================================================

@makeView()
@csrf_exempt
@handleException(IndexError, 'Booking not found', 404)
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@transaction.atomic
@checkAuth()
def userBookingRate(dct, user):
    '''
    User calls this to rate the booking which has been completed
    HTTP Args:
        auth,
        on = order number
        rate= rating 0 or 1
        rev = detailed review about the servitor

    '''
    qsBooking = Booking.objects.filter(order_number=dct['on'])
    booking = qsBooking[0]

    booking.status = 'RATE'
    booking.order_status = 'RATE'
    booking.etime = datetime.now(timezone.utc)
    booking.save()

    servitor = Servitor.objects.filter(an=booking.servan)[0]
    numBook = Booking.objects.filter(servan=servitor.an).count()
    servitor.mark = (servitor.mark + int(dct['rate'])) / (numBook + 1)
    servitor.save()

    rate = Rate()
    rate.id = 'book' + str(booking.order_number)
    rate.type = 'SERV'
    rate.rev = dct['rev']
    rate.money = float(500)
    rate.dan = servitor.an
    if 'attitude' in dct['rev'].lower():
        rate.rating = 'attitude'
    elif 'quality' in dct['rev'].lower():
        rate.rating = 'quality'
    else:
        rate.rating = dct['rev']
    rate.save()

    return HttpJSONResponse({})

