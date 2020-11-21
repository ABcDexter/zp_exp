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
from ..models import User, Product, Rate
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
    
    
    wcapi = API(
        url="https://zippe.in",
        consumer_key=settings.WP_CONSUMER_KEY,
        consumer_secret=settings.WP_CONSUMER_SECRET_KEY,
        version="wc/v3"
    )
    ret = wcapi.get("products")
    print(ret.status_code)
    
    product = {}
    
    for i in ret.json():
        print(i['id'], i['sku'])
        product[str(i['sku'])] = str(i['id'])

    print(products)

    return HttpJSONResponse({})


