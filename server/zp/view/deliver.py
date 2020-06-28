# imports
import datetime
from datetime import datetime, timedelta

from django.db import transaction
from django.utils import timezone
from django.views.decorators.csrf import csrf_exempt

from url_magic import makeView
from ..models import Place, Delivery, Progress
from ..models import User, Vehicle, Agent
from ..utils import ZPException, HttpJSONResponse
from ..utils import getOTP
from ..utils import getTripPrice, getRentPrice
from ..utils import handleException, extractParams, checkAuth, retireEntity, getClientAuth, getDeliveryPrice

###########################################
# Types
Filename = str


###########################################
# Constants

makeView.APP_NAME = 'zp'


# ============================================================================
# Delivery views
# ============================================================================


@makeView()
@csrf_exempt
@handleException(IndexError, 'Agent not found', 404)
@extractParams
def isAgentVerified(_, dct):
    '''
    Returns the Agents registration status and valid auth once the mode
    is changed from 'RG'
    The mode is changed by the call center after human verification of driver bona fides

    HTTP args:
        token : which was sent in response to  the driver registration request
        an : aadhaar number
        pn : phone number
    '''
    # Fetch this agent based on aadhaar - if confirmed, send the auth back
    agent = Agent.objects.filter(an=dct['an'])[0]
    ret = {'status': False}
    if agent.mode != 'RG' and dct['token'] == getClientAuth(dct['an'], dct['pn'] + '-register'):
        ret = {'status': True, 'auth': agent.auth}

    return HttpJSONResponse(ret)


@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@checkAuth()
@checkDeliveryStatus(['INACTIVE'])
def userDeliveryCost(dct, user, _trip):
    '''
    Returns the estimated price for the delivery

    HTTP args:
        rtype
        vtype
        npas
        dstid
        pmode

        hrs
    '''
    print("Ride Estimate param : ", dct)
    ret = getDeliveryPrice(dct['src'], dct['dst'], 1, dct['pmode'], fTimeHrs=0)
    return HttpJSONResponse(ret)
