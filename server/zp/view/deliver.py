# imports
import datetime
from datetime import datetime, timedelta
from decimal import getcontext

from django.db import transaction
from django.utils import timezone
from django.views.decorators.csrf import csrf_exempt
from django.conf import settings

from url_magic import makeView
from ..models import Place, Delivery, Progress, Location
from ..models import User, Vehicle, Agent
from ..utils import ZPException, HttpJSONResponse
from ..utils import getOTP
from ..utils import getDeliveryPrice, getDelPrice
from ..utils import handleException, extractParams, checkAuth, retireDelEntity, getClientAuth
from ..utils import checkDeliveryStatus
import googlemaps

###########################################
# Types
Filename = str


###########################################
# Constants

makeView.APP_NAME = 'zp'


# ============================================================================
# Delivery auth views
# ============================================================================


@makeView()
@csrf_exempt
@extractParams
@checkAuth()
def authDeliveryGetInfo(dct, entity):
    '''
    https args :
        did : delivery id
    Returns deli info for this agent or user for any past or current deli
    '''
    # get the delivery and ensure entity was in it
    deli = Delivery.objects.filter(id=dct['did'])[0]
    if not (deli.uan == entity.an or deli.dan == entity.an):
        raise ZPException('Invalid deli ID', 400)

    # get the deli and append pricing info if complete
    ret = {'st': deli.st}
    if deli.st in ['AS']:
        ret.update(getDelPrice(deli))
    return HttpJSONResponse(ret)


# ============================================================================
# Delivery user views
# ============================================================================

@makeView()
@csrf_exempt
@handleException()
@extractParams
@transaction.atomic
@checkAuth()
def userDeliveryGetStatus(_dct, user):
    '''
    Gets the current delis detail for a user
    This must be polled continuously by the user app to detect any state change
    after a ride request is madega

    Returns:
        active(bool): Whether a deli is in progress
        status(str): deli status
        tid(str): deli ID

        For each of the following statuses, additional data is returned:
            AS: otp, dan, van
            ST: progress (percent)
            TR, FN: price, time (seconds), dist (meters), speed (m/s average)

        Note: If active is false, no other data is returned
    '''
    print(_dct)
    # Get the users current deli if any
    if user.did != -1:
        print("here A")
        qsDeli = Delivery.objects.filter(id=user.did)
        deli = qsDeli[0]
        ret = {'st': deli.st, 'did': deli.id, 'active': deli.st in Delivery.USER_ACTIVE}

        # For paid Delivery request send OTP, and 'an' of vehicle and driver
        if deli.st == 'PD':
            ret['otp'] = getOTP(deli.uan, deli.dan, deli.atime)
            #vehicle = Vehicle.objects.filter(an=deli.van)[0]
            #ret['vno'] = vehicle.regn

        # For started delis send deli progress percent
        # this is redundant, this functionality is provided by authProgressPercent()
        if deli.st == 'ST':
            progress = Progress.objects.filter(tid=deli.id)[0]
            ret['pct'] = progress.pct
            if deli.rtype == 1:
                currTime = datetime.now(timezone.utc)
                diffTime = (currTime - deli.stime).total_seconds() / 60  # minutes
                remHrs = diffTime - deli.hrs
                ret['time'] = remHrs

        # For ended delis that need payment send the price data
        if deli.st in Delivery.PAYABLE:
            price = getDelPrice(deli)

            ret.update(price)
    else:

        print("here B")
        ret = {'active': False}

    return HttpJSONResponse(ret)



@makeView()
@csrf_exempt
@handleException(googlemaps.exceptions.TransportError, 'Internet Connectivity Problem', 503)
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@checkAuth()
def userIsAgentAv(dct, user):
    '''
    Returns the Agents count around 5 km radius of a particular lat and lng

    HTTP args:
        auth : auth of user
        srclat : latitude
        srclng : longitude
    '''

    srcCoOrds = ['%s,%s' % (dct['srclat'], dct['srclng'])]

    getcontext().prec = 50
    qsAgents = Agent.objects.filter(mode='AV').values()#Agent.objects.raw('''SELECT * FROM  location INNER JOIN agent WHERE location.an = agent.an AND agent.mode='AV';''');

    ret = []
    # print("$$$$$$$$$$$$$$$$: " ,qsAgents, qsAgents[0]['an'])
    for agent in qsAgents:

        qsLocs = Location.objects.filter(an=agent['an']).values()
        # print('##############',qsLocs)
        arrLocs = [recPlace for recPlace in qsAgents]
        dstCoOrds = ['%s,%s' % (recPlace['lat'], recPlace['lng']) for recPlace in qsLocs]
        # print('################',dstCoOrds)

        print(srcCoOrds, dstCoOrds)

        import googlemaps
        gmaps = googlemaps.Client(key=settings.GOOGLE_MAPS_KEY)
        dctDist = gmaps.distance_matrix(srcCoOrds, dstCoOrds)
        # log(dctDist)
        # print('############# DST : ', dctDist)
        if dctDist['status'] != 'OK':
            raise ZPException(501, 'Error fetching distance matrix')

        dctElem = dctDist['rows'][0]['elements'][0]
        nDist = 0
        nTime = 0
        if dctElem['status'] == 'OK':
            nDist = dctElem['distance']['value']
            nTime = int(dctElem['duration']['value'])//60
        elif dctElem['status'] == 'NOT_FOUND':
            nDist , nTime = 2048, 60  # dummy distance, time in mins
        elif dctElem['status'] == 'ZERO_RESULTS':
            nDist , nTime = 4096, 120

        print('distance: ', nDist)
        print('time: ', nTime)

        ret.append({'an': agent['an'], 'name': agent['name'], 'dist': nDist, 'time': nTime})

    return HttpJSONResponse(ret)


@makeView()
@csrf_exempt
@handleException(googlemaps.exceptions.TransportError, 'Internet Connectivity Problem', 503)
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@checkAuth()
def userDeliveryEstimate(dct, _user):
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
    ret = getDeliveryPrice(dct['srclat'], dct['srclng'], dct['dstlat'], dct['dstlng'], 1, 1)
    return HttpJSONResponse(ret)


@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@transaction.atomic
@checkAuth()
@checkDeliveryStatus(['INACTIVE'])
def userDeliveryRequest(dct, user, _delivery):
    '''
    Returns the estimated price for the delivery

    HTTP args:
        rtype
        vtype
        npas
        dstpin
        pmode

        hrs
    '''
    print("Delivery request param : ", dct)


    delivery = Delivery()
    delivery.st = 'RQ'
    delivery.srclat, delivery.srclng, delivery.dstlat, delivery.dstlng = dct['srclat'], dct['srclng'], dct['dstlat'], dct['dstlng']
    delivery.uan = user.an
    delivery.srcpin = dct['srcpin']
    delivery.dstpin = dct['dstpin']
    delivery.idim = dct['idim']
    delivery.itype = dct['itype']
    delivery.pmode = 1 # online for now , later one can be edited to dct['pmode']
    delivery.rtime = datetime.now(timezone.utc)
    delivery.save()

    user.did = delivery.id
    user.save()
    return HttpJSONResponse({'did': delivery.id})


@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@transaction.atomic
@checkAuth()
@checkDeliveryStatus(['AS'])
def userDeliveryPay(_dct, _user, delivery):
    '''
        Cancel the Delivery for a user if requested, assigned or started
        Should PD delivery also be allowed to Cancel? What about refund?
    '''
    delivery.st = 'PD'
    delivery.save()

    return HttpJSONResponse({})


@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@transaction.atomic
@checkAuth()
@checkDeliveryStatus(['RQ', 'AS']) #TODO should this have PD or not?
def userDeliveryCancel(_dct, _user, delivery):
    '''
        Cancel the Delivery for a user if requested, assigned or started
        Should PD delivery also be allowed to Cancel? What about refund?
    '''
    delivery.st = 'CN'
    delivery.etime = datetime.now(timezone.utc)
    delivery.save()

    return HttpJSONResponse({})


# ============================================================================
# Agent views
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
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@checkAuth()
def agentGetMode(dct, agent):
    '''
    Agent calls this to get his status
    Returns:
        status(str): The current status

    Note:
        Status changes externally due to deli failure or admin intervention
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
            BK to AV/OF iff agent did is -1

        Otherwise the state is not changed
    '''
    if (agent.mode in ['OF', 'AV'] or agent.did == -1) and dct['st'] in ['OF', 'AV']:
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
    Agent calls this to get the status of the current active deli if any
    It must be polled continuously to detect state changes
    Returns:
        active: boolean - means deli is in AS, ST, FN
        status(str): Delivery status
        For each of the following statuses, additional data is returned:
            AS: uan, van, id
            ST: progress (percent)
            TR, FN: price, time (seconds), dist (meters), speed (m/s average)
    '''

    # Get the last deli with this agent if any

    ret = {'active': False}

    qsDelivery = Delivery.objects.filter(id=agent.did)
    if len(qsDelivery):
        deli = qsDelivery[0]

        # For assigned deli return user and vehicle an
        if deli.st == 'AS':
            ret = {'uan': deli.uan, 'van': deli.van}

        # For started deli send progress
        if deli.st == 'ST':
            pct = Progress.objects.filter(tid=deli.id)[0].pct
            ret = {'pct': pct}

        # For ended delis that need payment send the price data
        if deli.st in Delivery.PAYABLE:
            ret = getDelPrice(deli)

        ret['active'] = deli.st in Delivery.AGENT_ACTIVE
        ret['st'] = deli.st
        ret['did'] = deli.id

    return HttpJSONResponse(ret)


@makeView()
@csrf_exempt
@handleException()
@extractParams
@checkAuth(['AV'])
def agentDeliveryCheck(_dct, agent):
    '''
    Returns a list of requested delis
    Only delis which start from this agents PID are returned
    No delis are returned if there are no vehicles there
    '''
    # Get available vehicles at this hub, if none return empty
    # qsVehicles = Vehicle.objects.filter(pid=agent.pid, tid=-1)
    #if len(qsVehicles) == 0:
    #    return HttpJSONResponse({}) # making it easy for Volley to handle JSONArray and JSONObject

    # Get the first requested delivery from agents place id
    qsDelivery = Delivery.objects.filter(srcpin=agent.pid, st='RQ').order_by('-rtime') #TODO how to do this :?
    ret = {} if len(qsDelivery) == 0 else {'did': qsDelivery[0].id}
    return HttpJSONResponse(ret)



@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@transaction.atomic
@checkAuth(['AV'])
def agentDeliveryAccept(dct, agent):
    '''
    Accept requested delivery
    HTTP args:
        did : Delivery id
        van : an of the Vehicle chosen by agent
    '''
    # Ensure that this agent is not in another active deli (for safety)
    qsActiveDelivery = Delivery.objects.filter(dan=agent.an, st__in=Delivery.AGENT_ACTIVE)
    if len(qsActiveDelivery):
        raise ZPException(400, 'Agent already in deli')

    ret = {}
    # Assign agent to deli and create a deli progress entry
    deli = Delivery.objects.filter(id=dct['did'])[0]
    if deli.st == 'RQ':
        # Ensure that the chosen vehicle is here and not assigned to a deli
        #vehicle = Vehicle.objects.filter(an=dct['van'], pid=deli.srcpin)[0] #????? how
        #if vehicle.tid != -1:
        #    raise ZPException(400, 'Vehicle already in deli')

        # Make the deli
        deli.st = 'AS'
        deli.dan = agent.an
        deli.van = vehicle.an
        deli.atime = datetime.now(timezone.utc)
        deli.save()

        # Make the progress
        progress = Progress()
        progress.tid = deli.id
        progress.pct = 0
        progress.save()

        # Set the agent to booked, set tid
        agent.mode = 'BK'
        agent.did = deli.id
        agent.save()

        # set the vehicles tid
        vehicle.tid = deli.id
        vehicle.save()

        ret.update({'dstpin': deli.dstpin})

        user = User.objects.filter(an=deli.uan)[0]
        ret.update({'name': user.name, 'phone': user.pn})
        src = Place.objects.filter(id=deli.srcpin)[0]
        dst = Place.objects.filter(id=deli.dstpin)[0]
        ret.update({'srcname': src.pn, 'dstname': dst.pn})
        print("Accepting deli : ", ret)
    else:
        raise ZPException(400, 'Delivery already assigned')

    return HttpJSONResponse(ret)


@makeView()
@csrf_exempt
@handleException()
@extractParams
@transaction.atomic
@checkAuth(['BK'])
@checkDeliveryStatus(['PD'])
def agentDeliveryCancel(_dct, agent, deli):
    '''
    Called by agent to deny a delivery that was assigned (AS)
    '''
    # Change deli status from assigned to  denied
    # Set the state for the deli and agent - agent is set to OF on failure
    if deli.st == 'AS':
        agent.mode = 'AV'
        deli.st = 'DN'

    # Reset agent did, but not users since they need to see the DN state
    retireDelEntity(agent)

    # Note the time of deli cancel/fail and save
    deli.etime = datetime.now(timezone.utc)
    deli.save()

    # Reset the vehicle tid
    #vehicle = Vehicle.objects.filter(tid=deli.id)[0]
    #retireDelEntity(vehicle)

    return HttpJSONResponse({})


@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@transaction.atomic
@checkAuth(['BK'])
@checkDeliveryStatus(['PD'])
def agentDeliveryStart(dct, _agent, deli):
    '''
    Agent calls this to start the deli providing the OTP that the user shared
    HTTP Args:
        OTP
    '''
    if str(dct['otp']) == str(getOTP(deli.uan, deli.dan, deli.atime)):
        deli.st = 'ST'
        deli.stime = datetime.now(timezone.utc)
        deli.save()
    else:
        raise ZPException(403, 'Invalid OTP')

    return HttpJSONResponse({})


@makeView()
@csrf_exempt
@extractParams
@transaction.atomic
@checkAuth(['BK'])
@checkDeliveryStatus(['ST'])
def agentDeliveryDone(_dct, agent, deli):
    '''
    Agent calls this to complete the Delivery
    TODO: Verify via vehicle/agent/user location that the deli actually happened
    '''
    deli.st = 'FN'
    deli.etime = datetime.now(timezone.utc)
    deli.save()

    # Get the vehicle
    # recVehicle = Vehicle.objects.filter(an=deli.van)[0]

    # Calculate price
    #dctPrice = getDeliveryPrice(deli.srclat, deli.srclng, deli.dstlat, deli.dstlng, deli.size, 1)
    # TODO share how much money agent earned on this delivery
    return HttpJSONResponse({})


@makeView()
@csrf_exempt
@extractParams
@transaction.atomic
@checkAuth(['BK'])
@checkDeliveryStatus(['FN'])
def agentUserRate(_dct, agent, deli):
    '''
    Agent calls this to confirm money received

    Note:
        Since state goes to PD, the deli retiring is done here
    '''

    agent.mode = 'AV'
    retireDelEntity(agent)

    # Get the vehicle
    # vehicle = Vehicle.objects.filter(an=deli.van)[0]
    # retireDelEntity(vehicle)

    return HttpJSONResponse({})



@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@transaction.atomic
@checkAuth(['BK'])
@checkDeliveryStatus(['CN', 'TO'])
def agentDeliveryRetire(dct, agent, deli):
    '''
    Resets agent's and vehicles active deli
    This is called when the agent has seen the message pertaining to deli end states:
    'TO', 'CN'

    These states occur by admin refresh or user cancel

    Following states when reached, have already retired the agent and vehicle
    DN : agent already retired from agentDeliveryCancel()
    PD : agent already retired from agentPaymentConfirm
    FL : admin already retired from adminHandleFailedDelivery()

    TODO: move this common code to a function
    '''
    # made the agent AV and reset the tid to -1
    agent.mode = 'AV'
    retireDelEntity(agent)

    # Reset the vehicle tid to available
    # vehicle = Vehicle.objects.filter(tid=deli.id)[0]
    # vehicle.tid = Vehicle.AVAILABLE
    # vehicle.save()
    return HttpJSONResponse({})



@makeView()
@csrf_exempt
@handleException()
@extractParams
@transaction.atomic
@checkAuth()
@checkDeliveryStatus(['ST'])
def authDeliveryFail(dct, agent, deli):
    '''
    Called by driverin an emergency which causes the delivery to end
    '''
    # Note the time of delivery cancel/fail and set state to failed
    deli.st = 'FL'
    deli.etime = datetime.now(timezone.utc)
    deli.save()

    # Reset the vehicle tid to failed so it wont be able to be selected
    # vehicle = Vehicle.objects.filter(tid=deli.id)[0]
    # vehicle.tid = Vehicle.FAILED
    # vehicle.save()

    agent.mode = 'LK'
    agent.save()

    return HttpJSONResponse({})
