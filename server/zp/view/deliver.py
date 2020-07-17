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
from ..models import Place, Delivery, Progress, Location
from ..models import User, Vehicle, Agent
from ..utils import ZPException, HttpJSONResponse, saveTmpImgFile, doOCR, log, aadhaarNumVerify, renameTmpImgFiles
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
    elif deli.st in ['PD', 'FN']:
        ret.update({'tip': deli.tip}) #TODO return earning from delivery

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
def userDeliveryGetStatus(dct, user):
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
    print(dct)
    # Get the users current deli if any
    if user.did != -1:

        qsDeli = Delivery.objects.filter(id=user.did)
        deli = qsDeli[0]
        ret = {'st': deli.st, 'did': deli.id, 'active': deli.st in Delivery.USER_ACTIVE}

        if ret['active']:
            if deli.st == 'RQ':  # Delivery.PAYABLE:
                price = getDelPrice(deli)
                ret.update(price)
            elif deli.st == 'AS':
                price = int(getDelPrice(deli)['price'])+int(deli.tip)
                ret.update({'price': price})
    else:

        deli = Delivery.objects.filter(id=dct['did'])[0]
        ret = {'active': False, 'st': deli.st}

        # For paid Delivery request send OTP, and 'an' of vehicle and Agent
        if deli.st == 'PD':
            ret['otp'] = getOTP(deli.uan, deli.dan, deli.atime)
            # For started delis send deli progress percent
        '''
        if deli.st == 'ST':
            progress = Progress.objects.filter(tid=deli.id)[0]
            ret['pct'] = progress.pct
            if deli.rtype == 1:
                currTime = datetime.now(timezone.utc)
                diffTime = (currTime - deli.stime).total_seconds() / 60  # minutes
                remHrs = diffTime - deli.hrs
                ret['time'] = remHrs
        '''
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
    qsAgents = Agent.objects.filter(mode='AV').values()
    #Agent.objects.raw('''SELECT * FROM  location INNER JOIN agent WHERE location.an = agent.an AND agent.mode='AV';''');

    ret = {}
    agents = []
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
            nDist, nTime = 0,0  # 2048, 60  # dummy distance, time in mins
        elif dctElem['status'] == 'ZERO_RESULTS':
            nDist, nTime = 0,0  # 4096, 120

        print('distance: ', nDist)
        print('time: ', nTime)
        if nDist and nTime:
            agents.append({'an': agent['an'], 'name': agent['name'], 'dist': nDist, 'time': nTime})
    
    ret.update({'agents':agents})
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
        itype,
        idim,
        fr, fl, li, kd, kw, kc,
        pmode
    '''
    print("Delivery Estimate param : ", dct)
    #TODO update the algorithm as the
    # Item type
    # IDims
    # checks,
    # Okay? tonight.
    ret = getDeliveryPrice(dct['srclat'], dct['srclng'], dct['dstlat'], dct['dstlng'], 1, 1)
    return HttpJSONResponse(ret)


@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@transaction.atomic
@checkAuth()
#@checkDeliveryStatus(['INACTIVE'])
def userDeliveryRequest(dct, user): #, _delivery):
    '''
    Returns the estimated price for the delivery

    HTTP args:
        auth,
        srclat, srclng,
        dstlat, dstlng,

        srcphone, dstphone,
        srcper, dstper,

        srdadd, srcpin
        srcland,
        dstadd, dstpin
        dstland,

        itype, idim

        details,

        fr, fl, li, kd, kw, kc,

        tip

    '''
    print("#######  ", len(dct), "Delivery request param : ",  dct)

    delivery = Delivery()
    delivery.st = 'RQ'
    delivery.uan = user.an

    delivery.srclat, delivery.srclng, delivery.dstlat, delivery.dstlng = dct['srclat'], dct['srclng'], \
                                                                         dct['dstlat'], dct['dstlng']

    delivery.srcpin, delivery.dstpin = dct['srcpin'],  dct['dstpin']

    delivery.idim = dct['idim']
    delivery.itype = dct['itype']
    delivery.pmode = 1 # online for now , later one can be edited to dct['pmode']
    delivery.rtime = datetime.now(timezone.utc)

    delivery.srcper, delivery.srcadd, delivery.srcland, delivery.srcphone = dct['srcper'], dct['srcadd'], \
                                                                            dct['srcland'], dct['srcphone']

    delivery.dstper, delivery.dstadd, delivery.dstland, delivery.dstphone = dct['dstper'], dct['dstadd'], \
                                                                            dct['dstland'], dct['dstphone']

    if 'fr' in dct:
        delivery.fl = dct['fr']
    if 'fl' in dct:
        delivery.fl = dct['fl']
    if 'li' in dct:
        delivery.fl = dct['li']
    if 'kd' in dct:
        delivery.fl = dct['fl']
    if 'kw' in dct:
        delivery.fl = dct['kw']
    if 'kc' in dct:
        delivery.fl = dct['kc']

    if 'details' in dct:
        delivery.details = dct['details']
    if 'tip' in dct:
        delivery.tip = dct['tip']
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
def userDeliveryPay(_dct, user, delivery):
    '''
        Cancel the Delivery for a user if requested, assigned or started
        Should PD delivery also be allowed to Cancel? What about refund?
    '''
    user.did = -1 #retire the user
    user.save()

    delivery.st = 'PD'
    delivery.save()
    otp = getOTP(delivery.uan, delivery.dan, delivery.atime)
    return HttpJSONResponse({'otp': otp})


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
@handleException(KeyError, 'Invalid parameters', 501)
@handleException(IntegrityError, 'Transaction error', 500)
@extractParams
@checkAuth()
def adminAgentRegister(dct):
    '''
    Completes agent registration after background verification is done offline

    HTTP args:
        an: agent aadhar
        *: Any other fields that need to be updated/corrected (except state)

    Note:
        No checking is done for fields - passing an invalid field will be silently ignored by the DB
        Auth is generated and stored
        Agent state is set to 'OF'
    '''
    # Get the agent and create an auth
    recAgent = Agent.objects.filter(an=dct['an'])[0]
    if recAgent.mode != 'RG':
        raise ZPException('Agent is already registered', 501)

    dct['auth'] = getClientAuth(str(recAgent.an), str(recAgent.pn))
    dct['st'] = 'OF'

    for key, val in dct.items():
        setattr(recAgent, key, val)
        recAgent.save()

    return HttpJSONResponse({})

# ============================================================================
# Agent views
# ============================================================================

@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@transaction.atomic
@extractParams
def registerAgent(_, dct):
    '''
    Agent registration
    Creates a Agent entry in the database, pending verification (state RG)
    Once admins have verified offline, registration is completed successfully
    with adminAgentRegister

    HTTP Args:
        aadhaarFront, aadhaarBack - aadhar scans
        licenseFront, licenseBack = driving license scans

    Notes:
        A registration token is returned to the client which is to be sent to
        isAgentVerified by the client while polling for registration status
        Registration has to be atomic since we save files
    '''

    sPhone = dct['phone']
    sAadharFrontFilename = saveTmpImgFile(settings.AADHAAR_DIR, dct['aadhaarFront'], 'front')
    sAadharBackFilename = saveTmpImgFile(settings.AADHAAR_DIR, dct['aadhaarBack'], 'back')

    sLicFrontFilename = saveTmpImgFile(settings.DL_DIR, dct['licenseFront'], 'front')
    sLicBackFilename = saveTmpImgFile(settings.DL_DIR, dct['licenseBack'], 'back')

    log('Agent Registration request - Aadhar images saved at %s, %s' % (sAadharFrontFilename, sAadharBackFilename))

    # Get aadhaar as 3 groups of 4 digits at a time via google vision api
    clientDetails = doOCR(sAadharFrontFilename)
    sAadhaar = clientDetails['an']
    log('Aadhaar number read from %s - %s' % (sAadharFrontFilename, sAadhaar))

    # verify aadhaar number via Verhoeff algorithm
    if not aadhaarNumVerify(sAadhaar):
        raise ZPException(501,'Aadhaar number not valid!')
    log('Aadhaar is valid')

    # Check if this Agent exists
    qsAgent = Agent.objects.filter(an=sAadhaar)
    AgentExists = len(qsAgent) != 0
    if not AgentExists:
        agent = Agent()
        agent.an = int(sAadhaar)
        agent.pn = sPhone
        agent.name = clientDetails.get('name', '')
        agent.gdr = clientDetails.get('gender', '')
        agent.age = clientDetails.get('age', '')
        agent.mode = 'RG'

        # Dummy values set by admin team manually
        agent.dl = 'UK01-AB1234'
        agent.hs = 'UK'

        # No place set
        agent.pid = -1
        agent.did = -1

        # agent has own vehicle
        agent.veh = 1

        # Set a random auth so that this Agent wont get authed
        agent.auth = str(random.randint(0, 0xFFFFFFFF))
        agent.save()

        # licenses are also stored with the aadhar in the file name but under settings.DL_DIR
        renameTmpImgFiles(settings.AADHAAR_DIR, sAadharFrontFilename, sAadharBackFilename, sAadhaar)
        renameTmpImgFiles(settings.DL_DIR, sLicFrontFilename, sLicBackFilename, sAadhaar)
        log('New Agent registered: %s' % sAadhaar)
    else:
        # Only proceed if status is not 'RG' else throw error
        agent = qsAgent[0]
        if agent.mode != 'RG':
            # Aadhaar exists, if mobile has changed, get new auth
            if agent.pn != sPhone:
                agent.pn = sPhone
                sAuth =  getClientAuth(agent.an, agent.pn)
                log('Auth changed for Agent: %s' % sAadhaar)
            else:
                # Aadhaar exists, phone unchanged, just return existing auth
                sAuth = agent.auth
                log('Auth exists for Agent: %s' % sAadhaar)
            return HttpJSONResponse({'auth': sAuth})
        else:
            raise ZPException('Registration pending', 501)

    # Deterministic registration token will be checked by isAgentVerified
    ret = {'token': getClientAuth(sAadhaar, sPhone + '-register'), 'an': sAadhaar, 'pn': sPhone }
    return HttpJSONResponse(ret)


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
        print("STATIS : ", deli.st)
        # For assigned deli return srcadd, dstadd
        if deli.st == 'AS':
            print('here....', deli.srcadd)
            ret.update({'srcadd': deli.srcadd, 'dstadd': deli.dstadd})
        elif deli.st == 'PD':
            #ret = {'uan': deli.uan, 'van': deli.van}
            ret.update({'srcper': deli.srcper,
                    'srcadd': deli.srcadd,
                    'srcland': deli.srcland,
                    'srcphone': deli.srcphone,
                    'srclat': deli.srclat,
                    'srclng': deli.srclng})

        # For started deli send progress
        elif deli.st == 'ST':
            ret.update({'dstper': deli.dstper,
                        'dstadd': deli.dstadd,
                        'dstland': deli.dstland,
                        'dstphone': deli.dstphone,
                        'dstlat': deli.dstlat,
                        'dstlng': deli.dstlng})
        # For ended delis that need payment send the price data
        if deli.st in Delivery.PAYABLE:
            ret.update(getDelPrice(deli))

        ret['active'] = deli.st in Delivery.AGENT_ACTIVE
        ret['st'] = deli.st
        ret['did'] = deli.id
        print(ret)

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
    """
    Get available vehicles at this hub, if none return empty
    qsVehicles = Vehicle.objects.filter(pid=agent.pid, tid=-1)
    #if len(qsVehicles) == 0:
    #    return HttpJSONResponse({}) # making it easy for Volley to handle JSONArray and JSONObject
    # Get the first requested delivery from agents place id
    #qsDelivery = Delivery.objects.filter(st='RQ').order_by('-rtime')
    """
    agentLoc = Location.objects.filter(an=agent.an)[0]
    srcCoOrds = ['%s,%s' % (agentLoc.lat, agentLoc.lng)]

    getcontext().prec = 50
    qsDeli =  Delivery.objects.filter(st='RQ').values()
    #print(qsDeli)
    delis = []
    for deli in qsDeli:
        print(deli)
        dstCoOrds = ['%s,%s' % (deli['srclat'], deli['srclng'])]
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
            nTime = int(dctElem['duration']['value']) // 60
        elif dctElem['status'] == 'NOT_FOUND':
            nDist, nTime = 0, 0
        elif dctElem['status'] == 'ZERO_RESULTS':
            nDist, nTime = 0, 0

        print('distance: ', nDist)
        print('time: ', nTime)
        if nTime or nDist:
            if nDist < 10_000:  # 10 kms radius
                print({'did': deli['id'], 'srcland':deli['srcland'], 'dstland':deli['dstland']})
                delis.append({'did': deli['id'], 'srcland':deli['srcland'], 'dstland':deli['dstland']})

    ret = {} if len(delis) == 0 else {'did': delis[0]['did'], 'srcland':delis[0]['srcland'], 'dstland':delis[0]['dstland']}
    ret.update({'count': len(delis)})
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
        #deli.van = vehicle.an
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
        #todo fix this logic
        #vehicle.tid = deli.id
        #vehicle.save()

        ret.update({'dstpin': deli.dstpin})

        user = User.objects.filter(an=deli.uan)[0]
        ret.update({'name': user.name, 'phone': user.pn})
        #src = Place.objects.filter(id=deli.srcpin)[0]
        #dst = Place.objects.filter(id=deli.dstpin)[0]
        ret.update({'srcadd': deli.srcadd, 'dstadd': deli.dstadd})
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
@checkDeliveryStatus(['AS', 'PD'])
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
    Called by Agentin an emergency which causes the delivery to end
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



@makeView()
@csrf_exempt
@handleException()
@extractParams
@transaction.atomic
@checkAuth()
@checkDeliveryStatus(['RQ', 'AS', 'ST', 'FN', 'TR', 'TO', 'CN', 'DN', 'FL', 'PD'])
def authDeliveryHistory(dct, entity, deli):
    '''
    returns the history of all Deliveries for a entity
    '''
    qsDeli = Delivery.objects.filter(uan=entity.an).values() if type(entity) is User else Delivery.objects.filter(dan=entity.an).values()
    ret = {}
    #print(qsDeli)
    if len(qsDeli) :
        states = []
        for i in qsDeli:
            states.append((i['id'],i['st']))
        ret.update({'deli':states})

    return HttpJSONResponse(ret)
