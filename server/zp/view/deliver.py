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
from ..utils import getDeliveryPrice
from ..utils import handleException, extractParams, checkAuth, retireEntity, getClientAuth
from ..utils import checkDeliveryStatus
import googlemaps

###########################################
# Types
Filename = str


###########################################
# Constants

makeView.APP_NAME = 'zp'


# ============================================================================
# Delivery user views
# ============================================================================


@makeView()
@csrf_exempt
@handleException(IndexError, 'Agent not found', 404)
@extractParams
def isAgentVerified(_, dct):
    '''
    Returns the Agents registration status and valid auth once the mode
    is changed from 'RG'
    The mode is changed by the call center after human verification of agent bona fides

    HTTP args:
        token : which was sent in response to  the agent registration request
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
@handleException(googlemaps.exceptions.TransportError, 'Internet Connectivity Problem', 503)
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@checkAuth()
@checkDeliveryStatus(['INACTIVE'])
def userDeliveryEstimate(dct, user, _trip):
    '''
    Returns the estimated price for the delivery

    HTTP args:
        auth, srcpin, dstpin,
        srclat, srclng,
        dstlat, dstlng
        self or not
        pmode
    '''
    print("Delivery Estimate param : ", dct)
    ret = getDeliveryPrice(dct['srclat'], dct['srclng'], dct['dstlat'], dct['dstlng'], 1, dct['pmode'])
    return HttpJSONResponse(ret)



@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@checkAuth()
@checkDeliveryStatus(['RQ'])
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


# ============================================================================
# Agent views
# ============================================================================


@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@checkAuth()
def agentGetMode(dct, agent):
    '''
    Agent calls this to get his status
    Returns:
        status(str): The current status

    Note:
        Status changes externally due to trip failure or admin intervention
    '''
    ret = {'st': agent.mode}
    return HttpJSONResponse(ret)


@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@checkAuth()
def agentSetMode(dct, agent):
    '''
    Agent calls this to set his status - AV, OF
    Returns:
        status(str): The current status

    Note:
        Status can be changed only from
            AV to OF and vice versa
            BK to AV/OF iff agent tid is -1

        Otherwise the state is not changed
    '''
    if (agent.mode in ['OF', 'AV'] or agent.tid == -1) and dct['st'] in ['OF', 'AV']:
        agent.mode = dct['st']
        agent.save()

    ret = {'st': agent.mode}
    return HttpJSONResponse(ret)


@makeView()
@csrf_exempt
@extractParams
@checkAuth(['AV', 'BK'])
def agentDeliveryGetStatus(_dct, agent):
    '''
    Agent calls this to get the status of the current active trip if any
    It must be polled continuously to detect state changes
    Returns:
        active: boolean - means trip is in AS, ST, FN
        status(str): Delivery status
        For each of the following statuses, additional data is returned:
            AS: uan, van, id
            ST: progress (percent)
            TR, FN: price, time (seconds), dist (meters), speed (m/s average)
    '''

    # Get the last trip with this agent if any

    ret = {'active': False}

    qsDelivery = Delivery.objects.filter(id=agent.tid)
    if len(qsDelivery):
        trip = qsDelivery[0]

        # For assigned trip return user and vehicle an
        if trip.st == 'AS':
            ret = {'uan': trip.uan, 'van': trip.van}

        # For started trip send progress
        if trip.st == 'ST':
            pct = Progress.objects.filter(tid=trip.id)[0].pct
            ret = {'pct': pct}

        # For ended trips that need payment send the price data
        if trip.st in Delivery.PAYABLE:
            ret = getDeliveryPrice(trip)

        ret['active'] = trip.st in Delivery.DRIVER_ACTIVE
        ret['st'] = trip.st
        ret['tid'] = trip.id

    return HttpJSONResponse(ret)


@makeView()
@csrf_exempt
@handleException()
@extractParams
@checkAuth(['AV'])
def agentDeliveryCheck(_dct, agent):
    '''
    Returns a list of requested trips
    Only trips which start from this agents PID are returned
    No trips are returned if there are no vehicles there
    '''
    # Get available vehicles at this hub, if none return empty
    # qsVehicles = Vehicle.objects.filter(pid=agent.pid, tid=-1)
    #if len(qsVehicles) == 0:
    #    return HttpJSONResponse({}) # making it easy for Volley to handle JSONArray and JSONObject

    # Get the first requested delivery from agents place id
    qsDelivery = Delivery.objects.filter(srcid=agent.pid, st='RQ').order_by('-rtime') #TODO how to do this :?
    ret = {} if len(qsDelivery) == 0 else {'tid': qsDelivery[0].id}
    return HttpJSONResponse(ret)



@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@transaction.atomic
@checkAuth(['AV'])
def agentRideAccept(dct, agent):
    '''
    Accept requested ride by agent
    HTTP args:
        tid : Delivery id
        van : an of the Vehicle chosen by agent
    '''
    # Ensure that this agent is not in another active trip (for safety)
    qsActiveDelivery = Delivery.objects.filter(dan=agent.an, st__in=Delivery.DRIVER_ACTIVE)
    if len(qsActiveDelivery):
        raise ZPException(400, 'Agent already in trip')

    ret = {}
    # Assign agent to trip and create a trip progress entry
    trip = Delivery.objects.filter(id=dct['tid'])[0]
    if trip.st == 'RQ':
        # Ensure that the chosen vehicle is here and not assigned to a trip
        vehicle = Vehicle.objects.filter(an=dct['van'], pid=trip.srcid)[0] #????? how
        if vehicle.tid != -1:
            raise ZPException(400, 'Vehicle already in trip')

        # Make the trip
        trip.st = 'AS'
        trip.dan = agent.an
        trip.van = vehicle.an
        trip.atime = datetime.now(timezone.utc)
        trip.save()

        # Make the progress
        progress = Progress()
        progress.tid = trip.id
        progress.pct = 0
        progress.save()

        # Set the agent to booked, set tid
        agent.mode = 'BK'
        agent.tid = trip.id
        agent.save()

        # set the vehicles tid
        vehicle.tid = trip.id
        vehicle.save()

        ret.update({'dstid': trip.dstid})

        user = User.objects.filter(an=trip.uan)[0]
        ret.update({'name': user.name, 'phone': user.pn})
        src = Place.objects.filter(id=trip.srcid)[0]
        dst = Place.objects.filter(id=trip.dstid)[0]
        ret.update({'srcname': src.pn, 'dstname': dst.pn})
        print("Accepting trip : ", ret)
    else:
        raise ZPException(400, 'Delivery already assigned')

    return HttpJSONResponse(ret)


@makeView()
@csrf_exempt
@handleException()
@extractParams
@transaction.atomic
@checkAuth(['BK'])
@checkDeliveryStatus(['AS'])
def agentRideCancel(_dct, agent, trip):
    '''
    Called by agent to deny a trip that was assigned (AS)
    '''
    # Change trip status from assigned to  denied
    # Set the state for the trip and agent - agent is set to OF on failure
    if trip.st == 'AS':
        agent.mode = 'AV'
        trip.st = 'DN'

    # Reset agent tid, but not users since they need to see the DN state
    retireEntity(agent)

    # Note the time of trip cancel/fail and save
    trip.etime = datetime.now(timezone.utc)
    trip.save()

    # Reset the vehicle tid
    vehicle = Vehicle.objects.filter(tid=trip.id)[0]
    retireEntity(vehicle)

    return HttpJSONResponse({})


@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@transaction.atomic
@checkAuth(['BK'])
@checkDeliveryStatus(['AS'])
def agentRideStart(dct, _agent, trip):
    '''
    Agent calls this to start the trip providing the OTP that the user shared
    HTTP Args:
        OTP
    '''
    if str(dct['otp']) == str(getOTP(trip.uan, trip.dan, trip.atime)):
        trip.st = 'ST'
        trip.stime = datetime.now(timezone.utc)
        trip.save()
    else:
        raise ZPException(403, 'Invalid OTP')

    return HttpJSONResponse({})


@makeView()
@csrf_exempt
@extractParams
@transaction.atomic
@checkAuth(['BK'])
@checkDeliveryStatus(['ST'])
def agentRideEnd(_dct, agent, trip):
    '''
    Agent calls this to end ride
    TODO: Verify via vehicle/agent/user location that the trip actually happened
    '''
    trip.st = 'FN'
    trip.etime = datetime.now(timezone.utc)
    trip.save()

    # Get the vehicle
    recVehicle = Vehicle.objects.filter(an=trip.van)[0]

    # Calculate price
    dctPrice = getRoutePrice(trip.srcid, trip.dstid, recVehicle.vtype, trip.pmode, (trip.etime - trip.stime).seconds)
    return HttpJSONResponse(dctPrice)


@makeView()
@csrf_exempt
@extractParams
@transaction.atomic
@checkAuth(['BK'])
@checkDeliveryStatus(['FN', 'TR'])
def agentPaymentConfirm(_dct, agent, trip):
    '''
    Agent calls this to confirm money received

    Note:
        Since state goes to PD, the trip retiring is done here
    '''
    trip.st = 'PD'
    trip.save()

    agent.mode = 'AV'
    retireEntity(agent)

    # user = User.objects.filter(an=trip.uan)[0]
    # user.tid = -1
    # user.save()
    # User retires via userRideRetire

    # Get the vehicle
    vehicle = Vehicle.objects.filter(an=trip.van)[0]
    retireEntity(vehicle)

    return HttpJSONResponse({})



@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@transaction.atomic
@checkAuth(['BK'])
@checkDeliveryStatus(['CN', 'TO'])
def agentRideRetire(dct, agent, trip):
    '''
    Resets agent's and vehicles active trip
    This is called when the agent has seen the message pertaining to trip end states:
    'TO', 'CN'

    These states occur by admin refresh or user cancel

    Following states when reached, have already retired the agent and vehicle
    DN : agent already retired from agentRideCancel()
    PD : agent already retired from agentPaymentConfirm
    FL : admin already retired from adminHandleFailedDelivery()

    TODO: move this common code to a function
    '''
    # made the agent AV and reset the tid to -1
    agent.mode = 'AV'
    retireEntity(agent)

    # Reset the vehicle tid to available
    vehicle = Vehicle.objects.filter(tid=trip.id)[0]
    vehicle.tid = Vehicle.AVAILABLE
    vehicle.save()
    return HttpJSONResponse({})

