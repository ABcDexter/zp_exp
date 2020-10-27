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
from ..utils import ZPException, HttpJSONResponse, saveTmpImgFile, doOCR, log, aadhaarNumVerify, renameTmpImgFiles, \
    googleDistAndTime
from ..utils import getOTP
from ..utils import getDeliveryPrice, getDelPrice
from ..utils import handleException, extractParams, checkAuth, retireDelEntity, getClientAuth
from ..utils import checkDeliveryStatus
import googlemaps
from ..utils import extract_name_from_pin
from django.forms.models import model_to_dict
import json
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
    if 'did' in dct:
        deli = Delivery.objects.filter(id=dct['did'])[0]
    elif 'scid':
        deli = Delivery.objects.filter(scid=dct['scid'])[0]

    if not (deli.uan == entity.an or deli.dan == entity.an):
        raise ZPException('Invalid deli ID', 400)

    # get the deli and append pricing info if complete
    ret = {'st': deli.st}
    hs = User.objects.filter(an=deli.uan)[0].hs
    if deli.st in ['SC']:
        ret.update(getDelPrice(deli, hs))
    elif deli.st in ['PD', 'FN']:
        ret.update({'tip': deli.tip,
                    'price': getDelPrice(deli, hs)['price'],
                    'earn':float(getDelPrice(deli, hs)['price'])/10})  # TODO return earning from delivery
    elif deli.st in ['AS', 'RC']:
        ret['otp'] = getOTP(deli.uan, deli.dan, deli.atime)

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
    if len(user.did) > 0:

        qsDeli = Delivery.objects.filter(scid=user.did)
        deli = qsDeli[0]
        ret = {'st': deli.st, 'scid': deli.scid, 'active': deli.st in Delivery.USER_ACTIVE}

        if ret['active']:
            if deli.st == 'SC':  # Delivery.PAYABLE:
                price = getDelPrice(deli, user.hs)
                ret.update(price)
        '''
        else:
            if deli.st == 'RQ':  # Delivery.PAYABLE:
                price = getDelPrice(deli, user.hs)
                ret.update(price)
            elif deli.st == 'AS':
                # price = #round(float("{:.2s}".format(getDelPrice(deli, user.hs)['price'])), 2) #+ (1.00*deli.tip)
                price = "%0.2f" % (float(getDelPrice(deli, user.hs)['price']) + (1.00*deli.tip) )
                ret.update({'price': price})
        '''
    else:

        deli = Delivery.objects.filter(scid=dct['scid'])[0]
        ret = {'active': deli.st in Delivery.USER_ACTIVE, 'st': deli.st}
        if deli.st == 'SC':
            ret.update(getDelPrice(deli, user.hs))
        # For paid Delivery request send OTP, and 'an' of vehicle and Agent
        #if deli.st == 'PD':
            #ret['otp'] = getOTP(deli.uan, deli.dan, deli.atime)
            # For started delis send deli progress percent
        if deli.st in ['AS', 'RC']:
            ret['otp'] = getOTP(deli.uan, deli.dan, deli.atime)

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
        gMapsRet = googleDistAndTime(srcCoOrds, dstCoOrds)
        nDist, nTime = gMapsRet['dist'], gMapsRet['time']

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
def userDeliveryEstimate(dct, user):
    '''
    Returns the estimated price for the delivery

    HTTP args:
        auth,
        srclat, srclng,
        dstlat, dstlng
        itype,
        idim = L, XL, XXL
        #fr, br, li, kc, kw, pe, (Fragile, Breakable, Liquid, Keep cold, Keep Warm, Perishable),
        express = 0 or 1
        pmode
    '''
    print("Delivery Estimate param : ", dct)
    # NOPE add price of the closest agent
    tip = float(dct['tip']) if 'tip' in dct else 0.00
    ret = getDeliveryPrice(dct['srclat'], dct['srclng'], dct['dstlat'], dct['dstlng'],
                           dct['idim'], dct['pmode'], dct['express'], user.hs, 0)

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
    Returns the did for the requested

    HTTP args:
        1. auth,
        2. srclat,
        3. srclng,
        4. dstlat,
        5. dstlng,
        6. srcphone,
        7. dstphone,
        8. srcper,
        9. dstper,
        10. srdadd,
        11. srcpin
        12. srcland,
        13. dstadd,
        14. dstpin
        15. dstland,
        16. itype,
        17. idim
        18. det,
        19. srcdet,
        20. dstdet,
        21. fr,
        22. br,
        23. li,
        24. kc,
        25. kw,
        26. pe,
        # 27. no (Fragile, Breakable, Liquid, Keep cold, Keep Warm, Perishable)
        28. tip
        29. express 0 or 1
        30. picktime:
                    date, month, year,
                    hour, minute.
        31. droptime
                    date, month, year,
                    hour, minute.

    Delivery has total 39 attributes as on now : 16:26 29/7/2020
    '''
    print("#######  ", len(dct), "Delivery request param : ",  dct)

    delivery = Delivery()
    delivery.st = 'RQ' #1
    delivery.uan = user.an #2

    delivery.srclat, delivery.srclng, delivery.dstlat, delivery.dstlng = dct['srclat'], dct['srclng'], \
                                                                         dct['dstlat'], dct['dstlng']
    # 3,4,5,6
    delivery.srcpin, delivery.dstpin = dct['srcpin'],  dct['dstpin']
    # 7,8,9,10
    delivery.idim = dct['idim']
    delivery.itype = dct['itype']
    delivery.pmode = dct['pmode']
    delivery.rtime = datetime.now(timezone.utc)
    # 11,12,13,14
    delivery.srcper, delivery.srcadd, delivery.srcland, delivery.srcphone = dct['srcper'], dct['srcadd'], \
                                                                            dct['srcland'], dct['srcphone']
    # 15,16,17,18
    delivery.dstper, delivery.dstadd, delivery.dstland, delivery.dstphone = dct['dstper'], dct['dstadd'], \
                                                                            dct['dstland'], dct['dstphone']

    delivery.br = dct['br'] if 'br' in dct else 0
    delivery.fr = dct['fr'] if 'fr' in dct else 0
    delivery.kc = dct['kc'] if 'kc' in dct else 0
    delivery.kw = dct['kw'] if 'kw' in dct else 0
    delivery.li = dct['li'] if 'li' in dct else 0
    delivery.pe = dct['pe'] if 'pe' in dct else 0
    # 19,20,21,22,23,24
    delivery.det = dct['det'] if 'det' in dct else ''
    delivery.srcdet = dct['srcdet'] if 'srcdet' in dct else ''
    delivery.dstdet = dct['dstdet'] if 'dstdet' in dct else ''
    # 25, 26, 27
    delivery.tip = int(float(dct['tip'])) if 'tip' in dct else 0
    # 28

    delivery.picktime = datetime.strptime(str(dct['picktime']), '%Y-%m-%d %H:%M:%S')  # .%f')
    delivery.droptime = datetime.strptime(str(dct['droptime']), '%Y-%m-%d %H:%M:%S')  # .%f')
    # 29, 30

    delivery.save()

    user.did = delivery.scid  # .id
    user.save()
    return HttpJSONResponse({'did': delivery.id})


@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@transaction.atomic
@checkAuth()
@checkDeliveryStatus(['SC'])
def userDeliveryPay(dct, user, delivery):
    '''
        Pay for the Delivery for a user after scheduled
        Https args:
            auth,
            scid
    '''
    print(delivery.scid, delivery.id)
    delivery = Delivery.objects.filter(scid=dct['scid'])[0]
    user.did = ''  # retire the user, #TODO move this logic to userDeliveryRetire() and comment this out
    user.save()

    delivery.st = 'PD'  # paid now
    delivery.save()

    return HttpJSONResponse({})


@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@transaction.atomic
@checkAuth()
@checkDeliveryStatus(['SC', 'PD', 'RQ', 'AS'])  # After PD, refund will happen.
def userDeliveryCancel(_dct, _user, delivery):
    '''
        Cancel the Delivery for a user if requested, assigned or started
        Should PD delivery also be allowed to Cancel? What about refund?
    '''
    delivery.st = 'CN'
    delivery.etime = datetime.now(timezone.utc)
    delivery.save()

    return HttpJSONResponse({})


@makeView()
@csrf_exempt
@handleException(IndexError, 'Delivery not found', 404)
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@transaction.atomic
@checkAuth()
#@checkDeliveryStatus(['ST'])
def userDeliveryTrack(dct, user):
    '''
        Track the Delivery for a user
    '''
    deli = Delivery.objects.filter(scid=dct['scid'])[0]  # get that delivery
    # agent = Agent.objects.filter(an=deli.dan)[0]  # get that agent
    if deli.st in ['ST', 'AS']:
        loc = Location.objects.filter(an=deli.dan)[0]  # get the location
        ret = {'lat': loc.lat, 'lng': loc.lng}
    else:
        raise ZPException('Trip NOT in right state to track agent', 501)

    return HttpJSONResponse(ret)


@makeView()
@csrf_exempt
@handleException(IndexError, 'Delivery not found', 404)
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@transaction.atomic
@checkAuth()
#@checkDeliveryStatus(['ST'])
def userDeliveryRQ(dct, user):
    '''
        Put the delivery into the RQ queue

        Http args:
            auth, scid
    '''
    
    deli = Delivery.objects.filter(scid=dct['scid'])[0]  # get that delivery
    deli.st = 'RQ' # now the delivery is in RQ
    deli.save()
    return HttpJSONResponse({})


@makeView()
@csrf_exempt
@handleException(IndexError, 'Delivery not found', 404)
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@transaction.atomic
@checkAuth()
@checkDeliveryStatus(['PD'])
def userDeliveryRetire(dct, user, _deli):
    '''
        retires the delivery for the use

        Http args:
            auth, scid
    '''
    user.did = ''
    user.save()
    return HttpJSONResponse({})


@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@checkAuth()
def authDeliveryDATA(dct, entity):
    '''
    Returns athe data of a delivery
        Https:
            auth, scid
    '''

    deli = Delivery.objects.filter(scid=dct['scid']).values('scid','fr', 'li', 'pe', 'kw', 'kc', 'express', 'st', 'rtime',
        'atime', 'stime', 'etime', 'picktime', 'droptime',#: datetime.datetime(2020, 7, 29, 15, 10, tzinfo=<UTC>),
        'srcpin', 'srclat', 'srclng', 'dstpin', 'dstlat', 'dstlng', 'itype', 'idim', 'srcper', 'dstper',
        'srcadd', 'dstadd', 'srcland','dstland', 'srcphone', 'dstphone','pmode', 'det', 'srcdet', 'dstdet', 'tip')

    lstDeli = list(deli)
    print("VAL : ", lstDeli[0], type(lstDeli[0]))#, lstDeli[1]) 1 mein empty hai()
    dctDeli = lstDeli[0]
    dctRet = {}
    for key, val in dctDeli.items():
        # print(key, val)
        if 'picktime' in str(key):
            #print(val.hour, val.minute)
            dctRet.update({str("pickdate"): str(val.day) + "/" + str(val.month) + "/" + str(val.year)})
            dctRet.update({str("picktime"): str(val.hour+6) + ":" + str(val.minute)})
        else:
            dctRet.update({str(key): str(val)})

    return HttpJSONResponse(dctRet)  # {'deli': dctRet})


# ============================================================================
# Admin views
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


@makeView()
@csrf_exempt
@handleException(IndexError, 'Agent not found', 404)
@handleException(KeyError, 'Invalid parameters', 501)
@handleException(IntegrityError, 'Transaction error', 500)
@extractParams
@checkAuth()
def adminAgentAssign(dct):
    '''
    Checks for deliveries in RQ state and assigns the nearest AV agent.

    HTTP args:
        *: Any other fields that need to be updated/corrected (except state)

    Note:
        this uses Google distance, assigns closest as per time, so might not be accurate
    '''
    # Get the deliveries and look for RQ ones
    qsDeli = Delivery.objects.filter(st__in=['RQ', 'PD'])

    qsAgent = Agent.objects.filter(mode='AV', did='-1')

    # for deli in qsDeli:
    # do one delivery at a time
    deli = qsDeli[0]
    srcCoOrds = ['%s, %s' % (deli.srclat, deli.srclng)]
    iterAn = 0
    minDist, minTime = 10_000, 60  # 10 kms and 60 minutes
    for agent in qsAgent:
        locAgent = Location.objects.filter(an=agent.an)[0]

        dstCoOrds = ['%s,%s' % (locAgent.lat, locAgent.lng)]
        print(srcCoOrds, dstCoOrds)

        gMapsRet = googleDistAndTime(srcCoOrds, dstCoOrds)
        nDist, nTime = gMapsRet['dist'], gMapsRet['time']
        print(" dist : ", nDist, " time : ", nTime)
        if nTime and (nTime < minTime):
            minTime = nTime
            minDist = nDist
            iterAn = agent.an

    print("closest agent is : ", minDist, " metres away and ", minTime, " minutes away")

    choosenAgent = Agent.objects.filter(an=iterAn)[0]

    if choosenAgent.mode != 'AV':
        raise ZPException('Agent is already registered', 501)
    delId = deli.id
    auth = choosenAgent.auth
    return HttpJSONResponse({'babua': auth, 'did': delId})


# ============================================================================
# Agent views
# ============================================================================


@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@transaction.atomic
@extractParams
def loginAgent(_, dct):
    '''
    Agent login
    makes the agent login with phone number
    
    HTTP Args:
        pno: phone number of the agent without the ISD code
        key: auth rot 13 of agent
        

    Notes:
        Rot 13 is important
    '''

    sPhone = str(dct['pn'])
    from codecs import encode
    sAuth = encode(str(dct['key']), 'rot13')
    
    qsAgent = Agent.objects.filter(auth=sAuth, pn=sPhone)
    bAgentExists = len(qsAgent) != 0
    if not bAgentExists:
        log('Agent not registered with phone : %s' % (dct['pn']))
        return HttpJSONResponse({'status':'false'})
    else:
        log('Auth exists for: %s' % (dct['pn']))
        ret = {'status': True, 'auth':qsAgent[0].auth, 'an':qsAgent[0].an}    
        return HttpJSONResponse(ret)



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
            hs = User.objects.filter(an=deli.uan)[0].hs
            ret.update(getDelPrice(deli, hs))

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

        gMapsRet = googleDistAndTime(srcCoOrds, dstCoOrds)
        nDist, nTime = gMapsRet['dist'], gMapsRet['time']

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
    if deli.st in ['RQ', 'PD']:
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
@checkDeliveryStatus(['RC', 'AS'])  # should we remove the 'AS' state from here
def agentDeliveryCancel(_dct, agent, deli):
    '''
    Called by agent to deny a delivery that was assigned (AS)
    '''
    # Change deli status from assigned to  denied
    # Set the state for the deli and agent - agent is set to OF on failure
    #if deli.st == 'RC':
    agent.mode = 'LK'  # agent is locked after this unless, he sends the reason for cancellation
    deli.st = 'DN'

    # Reset agent did, but not users since they need to see the DN state
    # retireDelEntity(agent)  moved to agentDeliveryRetire()

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
@checkDeliveryStatus(['AS'])
def agentDeliveryReached(dct, _agent, deli):
    '''
    Agent calls this to tell (s)he reached
    TODO: Verify via agent location that the agent actually reached
    HTTP Args:
        auth
    '''
    deli.st = 'RC'
    deli.save()
    return HttpJSONResponse({})


@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@transaction.atomic
@checkAuth(['BK'])
@checkDeliveryStatus(['RC'])
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
@checkDeliveryStatus(['CN', 'TO', 'FN', 'DN'])
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
@checkDeliveryStatus(['SC', 'RQ', 'AS', 'ST', 'FN', 'TR', 'TO', 'CN', 'DN', 'FL', 'PD'])
def authDeliveryHistory(dct, entity, deli):
    '''
    returns the history of all Deliveries for a entity
    '''

    qsDeli = Delivery.objects.filter(uan=entity.an).values() if type(entity) is User else Delivery.objects.filter(
        dan=entity.an).values()
    ret = {}
    # print(qsDeli)
    # print("REEEEEEEEEEEE ",len(qsDeli))
    if len(qsDeli):
        states = []
        for i in qsDeli:
            # print(str(i['stime'])[:19])
            strSTime = str(i['stime'])[:19]
            # print(i['etime'])
            if i['st'] in ['ST', 'FL', 'FN']:
                sTime = datetime.strptime(strSTime, '%Y-%m-%d %H:%M:%S').date()
            else:
                sTime = 'NOTSTARTED'
            if i['st'] in ['FN', 'CN']:
                strETime = str(i['etime'])[:19]
                eTime = datetime.strptime(strETime, '%Y-%m-%d %H:%M:%S').date()
            else:
                eTime = 'ONGOING'

            hs = User.objects.filter(an=deli.uan)[0].hs
            thisOneBro = {'scid': i['scid'], 'st': i['st'],
                          'price': float(getDelPrice(Delivery.objects.filter(id=i['id'])[0], hs)['price']) ,
                          'earn': float(getDelPrice(Delivery.objects.filter(id=i['id'])[0], hs)['price'])/10, #earns 10%
                          'tip': i['tip'],
                          'sdate': str(sTime),
                          'edate': str(eTime)
                          }

            states.append(thisOneBro)
        print(states)
        ret.update({'delis': states})

    return HttpJSONResponse(ret)


@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@transaction.atomic
@checkAuth()
@checkDeliveryStatus(['FN'])
def authDeliveryRate(dct, entity, deli):
    '''
    rating system...
    HTTP args:
        auth,
        rate
    '''
    print(dct, entity, deli)
    bIsUser = True if type(entity) is User else False  # user or agent
    if bIsUser :
        agent = Agent.objects.filter(an=deli.dan)[0]
        numDeliverys = Delivery.objects.filter(dan=agent.an).count()
        agent.mark = (agent.mark+int(dct['rate']))/(numDeliverys+1)
        agent.save()
    else :
        user = User.objects.filter(an=deli.uan)[0]
        numDeliverys = Delivery.objects.filter(uan=user.an).count()
        user.mark = (user.mark+int(dct['rate']))/(numDeliverys+1)
        user.save()
    # think maybe mark the deli as RATED, do we need an extra state ...?
    return HttpJSONResponse({})


@makeView()
@csrf_exempt
@handleException(IndexError, 'This PIN code is not serviceable yet.', 404)
@handleException(googlemaps.exceptions.TransportError, 'Internet Connectivity Problem', 503)
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@checkAuth()
def authLocationNameFromPin(dct, entity):
    '''
    Returns the estimated price for the trip

    HTTP args:
        srcpin,
        dstpin

    returns the name of place or none
    '''
    print("Google Ride Estimate param : ", dct)
    ret = extract_name_from_pin(str(dct['pin']))
    return HttpJSONResponse(ret)
