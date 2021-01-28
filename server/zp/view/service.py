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
@extractParams
@checkAuth()
def authBookingGet(dct, entity):
    '''
    Adds or edits a product with sku,

        name(str): name of the product
        type(bool): simple 0 or grouped 1
        regular_price (float): MRP of the product 
        cost_price (float): price we are getting the product at
        sale_price (float): selling price of the product 
        
        stock_quantity(int):   quantity of the item in the stock
        categories(str): product categories( see Category table)
        weight (float): weight (in grams) of one unit of the product 
        SKU(str): PRIMARY key
        
        tax_class(float): how much tax on the product
        low_stock_amount(int): low stock alert
    '''
   
    #WooCommerce update
    wcapi = API(url="https://zippe.in", consumer_key=settings.WP_CONSUMER_KEY, consumer_secret=settings.WP_CONSUMER_SECRET_KEY, version="wc/v3")
    ret = wcapi.get("orders")
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

        if 'start' in z['value']:
            orders = {"servitor": arrOrder,
                      "start": str(datetime.strptime(z['value']['start']['date'][:-7], '%Y-%m-%d %H:%M:%S')),
                      "end" : str(datetime.strptime(z['value']['end']['date'][:-7], '%Y-%m-%d %H:%M:%S')),
                      "billing": i['billing'],
                      "customer_note": i['customer_note']
                    }
            #print(orders)
            ordersResp.append(orders)

    print(ordersResp)


    return HttpJSONResponse({"booking":ordersResp})



# ============================================================================
# Servitor views
# ============================================================================


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
        pn: phone number of the Servitor without the ISD code
        key: auth of Servitor
        job1: primary job of servitor (Compulsary)
        job2: secondary job of servitor
        job3: tertiary job of servitor

        #TODO : add the job field for the servitor, get them as comma separated values....
    '''

    sPhone = str(dct['pn'])
    sAn = '91' + sPhone
    sName = str(dct['name'])
    sJob1 = str(dct['job1'])
    sJob2 = str(dct['job2']) if 'job2' in dct else ''
    sJob3 = str(dct['job3']) if 'job3' in dct else ''


    qsServitor = Servitor.objects.filter(pn=sPhone)

    bServitorExists = len(qsServitor) != 0
    if not bServitorExists:
        log('Servitor not registered with phone : %s' % (dct['pn']))

        servitor = Servitor()
        servitor.an = sAn
        servitor.auth = getClientAuth(sAn, sPhone)
        servitor.name = sName
        servitor.pn = sPhone

        servitor.job1 = sJob1
        servitor.job2 = sJob2
        servitor.job3 = sJob3
        servitor.save()

        return HttpJSONResponse({'auth' : servitor.auth, 'name':servitor.name})
    else:
        log('Auth exists for: %s' % (dct['pn']))

        qsServitor[0].name = sName
        qsServitor[0].job1 = sJob1
        qsServitor[0].job2 = sJob2
        qsServitor[0].job3 = sJob3
        qsServitor[0].save()

        ret = {'status': True, 'auth': qsServitor[0].auth, 'an': qsServitor[0].an, 'pn': qsServitor[0].pn,
               'name': qsServitor[0].name}
        return HttpJSONResponse(ret)

    return HttpJSONResponse({})


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
        pn: phone number of the Servitor without the ISD code
        key: auth of Servitor

    '''

    sPhone = str(dct['pn'])
    sAuth = str(dct['key'])
    
    qsServitor = Servitor.objects.filter(auth=sAuth, pn=sPhone)
    bServitorExists = len(qsServitor) != 0
    if not bServitorExists:
        log('Servitor not registered with phone : %s' % (dct['pn']))
        return HttpJSONResponse({'status':'false'})
    else:
        log('Auth exists for: %s' % (dct['pn']))
        ret = {'status': True, 'auth':qsServitor[0].auth, 'an':qsServitor[0].an, 'pn' : qsServitor[0].pn, 'name': qsServitor[0].name}    
        return HttpJSONResponse(ret)
    
    return HttpJSONResponse({})
    


@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@checkAuth()
def ServitorProductGet(dct, entity):
    '''
    Get the list of all products from zippe.in website
    #TODO rename this to get all jobs
    HTTP params:
        auth

    '''
    
    #getcontext().prec = 1000
    qsProduct = Product.objects.all().values('id','name','categories')
    return HttpJSONResponse({'product': list(qsProduct)})

    return HttpJSONResponse(products)
