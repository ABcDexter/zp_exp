# imports
import datetime
from datetime import datetime, timedelta

from django.db import transaction
from django.utils import timezone
from django.views.decorators.csrf import csrf_exempt

from url_magic import makeView
from ..models import Place, Trip, Progress, Driver
from ..models import User, Vehicle
from ..utils import ZPException, HttpJSONResponse
from ..utils import getOTP
from ..utils import getRoutePrice, getTripPrice
from ..utils import handleException, extractParams, checkAuth, checkTripStatus, retireEntity

###########################################
# Types
Filename = str


###########################################
# Constants

makeView.APP_NAME = 'zp'


# ============================================================================
# Rental views
# ============================================================================


@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@transaction.atomic
@checkAuth()
@checkTripStatus(['RQ', 'AS', 'ST'])
def userRentalUpdate(dct, user, trip):
    '''
    Update the rental time for a user if requested, assigned or started
    this update the etime for the user
    '''
    # take time in minutes
    # oldTime = (trip.etime - trip.rtime).total_seconds() / 60
    iHrs = int(dct['hrs'])
    #startTime = trip.stime #datetime.now(timezone.utc)
    #tdISTdelta = timedelta(hours=iHrs, minutes=0)
    #endTime = startTime + tdISTdelta
    #trip.etime = endTime
    #dstId = trip.
    #if trip.st == 'RQ':
    #    trip.etime = trip.rtime + dct['hrs']
    #else :
    #    trip.etime = trip.rtime + dct['hrs']
    
    #trip.dstid = dct['dstid']
    trip.hrs = iHrs
    trip.save()

    return HttpJSONResponse({})


@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@checkAuth()
@checkTripStatus('AS')
def userRentGetVehicle(_dct, entity, trip):
    '''
    Returns aadhaar, name and phone of current assigned vehicle,
    '''
    vehicle = Vehicle.objects.filter(an=trip.van)[0]
    ret = {'vno': vehicle.regn, 'otp': 1243} #TODO complete this
    return HttpJSONResponse(ret)


@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@checkAuth()
@checkTripStatus('AS')
def userRentGetSup(_dct, entity, trip):
    '''
    Returns aadhaar, name and phone of hub supervisor
    '''
    driver = Driver.objects.filter(an=trip.dan)[0]
    ret = {'pn': driver.pn, 'name': driver.name}
    return HttpJSONResponse(ret)


@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@checkAuth()
@checkTripStatus('ST')
def authTimeRemaining(_dct, entity, trip):
    '''
    Returns aadhaar, name and phone of current assigned supervisor
    '''
    # currTimeUTC = datetime.now(timezone.utc)
    # tdISTdelta = timedelta(hours=iHrs, minutes=0)
    # endTime = currTimeUTC + tdISTdelta
    # trip.etime = endTime
    ret = {}
    return HttpJSONResponse(ret)
