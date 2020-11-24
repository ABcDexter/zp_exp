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
from ..models import Product, Location
from ..models import User, Product, Rate, Purchaser
from ..utils import ZPException, HttpJSONResponse, saveTmpImgFile, doOCR, log, aadhaarNumVerify, renameTmpImgFiles, \
    googleDistAndTime
from ..utils import getOTP
from ..utils import handleException, extractParams, checkAuth, retireDelEntity, getClientAuth

import googlemaps
from ..utils import extract_name_from_pin
from django.forms.models import model_to_dict
import json
import urllib.request
from woocommerce import API


###########################################
# Types
Filename = str


###########################################
# Constants

makeView.APP_NAME = 'zp'


# ============================================================================
# Shop auth views
# ============================================================================


@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@checkAuth()
def authProductUpdate(dct, entity):
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
    sSKU = dct['sku']
    qsProduct = Product.objects.filter(sku=sSKU)
    rec =  Product() if len(qsProduct) == 0 else qsProduct[0]
        
    for key, val in dct.items():
        setattr(rec, key, val)
    rec.save()
    print(rec)
    
    #WooCommerce update
    wcapi = API(
        url="https://zippe.in",
        consumer_key=settings.WP_CONSUMER_KEY,
        consumer_secret=settings.WP_CONSUMER_SECRET_KEY,
        version="wc/v3"
    )
    ret = wcapi.get("products")
    print(ret.status_code)
    
    products = {}
    
    for i in ret.json():
        print(i['id'], i['sku'])
        products[str(i['sku'])] = str(i['id'])
        

    print(products)
    data = dct
    URI = 'products/'+ str(products[dct['sku']])
    print(wcapi.put(URI, data).json())


    return HttpJSONResponse({})



# ============================================================================
# Purchaser views
# ============================================================================


@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@transaction.atomic
@extractParams
def loginPurchaser(_, dct):
    '''
    Purchaser login
    makes the purchaser login with phone number
    
    HTTP Args:
        pn: phone number of the purchaser without the ISD code
        key: auth of purchaser
        
    '''

    sPhone = str(dct['pn'])
    sAuth = str(dct['key'])
    
    qsPurchaser = Purchaser.objects.filter(auth=sAuth, pn=sPhone)
    bPurchaserExists = len(qsPurchaser) != 0
    if not bPurchaserExists:
        log('Purchaser not registered with phone : %s' % (dct['pn']))
        return HttpJSONResponse({'status':'false'})
    else:
        log('Auth exists for: %s' % (dct['pn']))
        ret = {'status': True, 'auth':qsPurchaser[0].auth, 'an':qsPurchaser[0].an, 'pn' : qsPurchaser[0].pn, 'name': qsPurchaser[0].name}    
        return HttpJSONResponse(ret)



@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@checkAuth()
def purchaserProductGet(dct, entity):
    '''
    Get the list of all products from zippe.in website
        
    HTTP params:
        auth

    '''
    
    #WooCommerce get
    wcapi = API(
        url="https://zippe.in",
        consumer_key=settings.WP_CONSUMER_KEY,
        consumer_secret=settings.WP_CONSUMER_SECRET_KEY,
        version="wc/v3"
    )
    ret = wcapi.get("products")
    print(ret.status_code)
    
    products = {}
    
    for i in ret.json():
        print(i['id'], i['sku'])
        products[str(i['sku'])] = str(i['id'])
        

    return HttpJSONResponse(products)



