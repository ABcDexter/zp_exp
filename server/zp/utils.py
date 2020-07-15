# imports
import base64
import datetime
import hashlib
import json
import os
import random
import sys
import re
from datetime import datetime, timedelta
from json import JSONDecodeError
from functools import wraps

import logging
from django.conf import settings
from django.utils import timezone
import csv

from django.core.serializers.json import DjangoJSONEncoder
from django.http import HttpResponse

from .models import Place, Trip, Progress, Route
from .models import Vehicle, User, Driver, Supervisor
from .models import Delivery, Agent

from django.conf import settings
#from fuzzywuzzy import fuzz as accurate

os.environ['GOOGLE_APPLICATION_CREDENTIALS'] = settings.GOOGLE_APPLICATION_CREDENTIALS
logger = logging.getLogger('zp')

def log(x: str):
    logger.debug(x)


###########################################
# Utility functions and classes


def errMsg(err, status=501):
    return{'error': err, 'status': status}


def HttpJSONResponse(data, status:int=None):
    return HttpResponse(json.dumps(data), content_type='application/json', status=status)


def HttpRecordsResponse(data, status:int=None):
    ret = json.dumps(list(data), cls=DjangoJSONEncoder)
    return HttpResponse(ret, content_type='application/json', status=status)


def HttpJSONError(err, status=501):
    return HttpResponse(json.dumps(errMsg(err, status)), content_type='application/json')


class ZPException(Exception):
    def __init__(self, status, message):
        self.status = status
        self.message = message

    # return HTTPJsonResponse with code and message
    def response(self):
        return HttpJSONResponse(errMsg(self.message, self.status))

class DummyException(Exception):
    pass

##########################################
# Helpers/Algorithms

def saveTmpImgFile(sPath, sImage, sSuffix):
    '''
    Saves the given base64 image data to a file: <sPath>/<random>-<sSuffix>.jpg
    '''
    # Get a temp random filename
    random.seed(datetime.now())
    sRand = str(random.randrange(0, 999999))
    sFilePath = os.path.join(sPath, sRand + '-' + sSuffix + '.jpg')

    # This may throw some filesystem error, also key not found...
    b64Img = base64.b64decode(sImage)
    with open(sFilePath, 'wb') as f:
        f.write(b64Img)

    return sFilePath


def renameTmpImgFiles(sPath, sFrontFilePath, sBackFilePath, sPrefix):
    '''
    Moves/Renames files sFrontFilePath and sBackFilePath to:
    <sPath>/<sPrefix>_front.jpg and <sPath>/<sPrefix>_back.jpg
    '''
    os.rename(sFrontFilePath, os.path.join(sPath, sPrefix + '_front.jpg'))
    os.rename(sBackFilePath, os.path.join(sPath, sPrefix + '_back.jpg'))


def doOCR(path):
    '''
    does the OCR for a given image
    '''

    def fuzzMatch(s1, s2):
        mn = min(len(s1), len(s2))
        mx = max(len(s1), len(s2))
        nMatch = 0
        for i in range(mn):
            if s1[i] == s2[i]:
                nMatch += 1
        fuzz = nMatch / mx
        # print('values : ', nMatch, mx)
        frat = 90
        # frat = accurate.ratio(s1.lower(), s2.lower())
        # print('fuzz : ', fuzz, 'frat : ', frat / 100)
        return max(fuzz, frat / 100)

    def is_ascii(s):
        return all(ord(c) < 128 for c in s)

    def onlyAscii(s):
        return ''.join([c for c in s if is_ascii(c)])

    def noSpace(s):
        return ''.join([c for c in s if not c.isspace()])

    def getREMatch(dct, key, regex, words, bIgnoreCase=True, bNoSpace=True):
        for word in words:
            w = noSpace(word) if bNoSpace else word
            m = re.search(regex, w, re.IGNORECASE) if bIgnoreCase else re.search(regex, w)
            if m:
                span = m.span()
                dct[key] = w[span[0]:span[1]]
                return

    import io
    from google.cloud import vision
    client = vision.ImageAnnotatorClient()

    with io.open(path, 'rb') as image_file:
        content = image_file.read()

    image = vision.types.Image(content=content)
    response = client.text_detection(image=image)
    if response.error.message:
        raise ZPException(501, str(response.error.message))

    texts = response.text_annotations

    # Take only the first item
    sAllText = ''
    for text in texts:
        sAllText = text.description
        break

    # Get each line of text and eliminate non ascii chars
    arrWords = [onlyAscii(word).strip() for word in sAllText.split('\n')]

    # Remove empty words
    arrWords = [word for word in arrWords if len(word) > 0]
    log(arrWords)

    ret = {}

    # Look for "Government of India" fuzzily
    fMatch = fuzzMatch(noSpace(arrWords[0]).lower(), 'governmentofindia')
    if fMatch > 0.85:
        print('Confidence %d%%' % (fMatch * 100))
        # arrWords.pop(0)
        # this is bugg-E, pops the last element, what if 12 digit aadhaar is the last string OCRed, GovtOfIndia should be popped
        # print("WORDS : ", arrWords)
        getREMatch(ret, 'an', r'\d{12}', arrWords)
        getREMatch(ret, 'name', r'[A-Z][a-z]*\s+[A-Z][a-z]+', arrWords, False, False)
        getREMatch(ret, 'dob', r'\d\d\/\d\d\/\d\d\d\d|\:\d{4}', arrWords)
        getREMatch(ret, 'gender', r'male|female|transgender', arrWords)

        if 'dob' in ret:
            if len(ret['dob']) > 7:
                dob = datetime.strptime(ret.pop('dob'), '%d/%m/%Y').date()
                ret['age'] = datetime.today().year - dob.year
            else:
                ret['age'] = datetime.today().year - int(ret.pop('dob')[-4:])

        # To be removed later on
        # ret['age'] = '25' if 'age' not in ret.keys() else ret['age']
        # ret['name'] = 'Pahaadi babua' if 'name' not in ret.keys() else ret['name']
        # ret['gender'] = 'Male' if 'gender' not in ret.keys() else ret['gender']
        # Delete! Exterminate!
        print(ret)

    return ret


def getOTP(an: int, dan: int, rtime: datetime) -> int:
    '''
    Generates a deterministic 4 digit OTP
    '''
    sText = 'zippee-otp-%s-%s-%s' % (str(an), str(dan), str(rtime))
    shaText = sText.encode('utf-8')
    m = hashlib.new('ripemd160')
    m.update(shaText)
    otp = int(m.hexdigest(), 16) % 9999
    return otp


def gvOCR(path) : #sPath: Filename, nChunks:int = 3, nChunkSize: int = 4) -> str:
    '''
        does the OCR for a given image, returns JSON
    '''
    import io, json
    from google.cloud import vision

    client = vision.ImageAnnotatorClient()

    with io.open(path, 'rb') as image_file:
        content = image_file.read()

    image = vision.types.Image(content=content)

    response = client.text_detection(image=image)
    texts = response.text_annotations
    #print('Texts:')
    reply = {}

    for i, text in enumerate(texts):
        #print('\n"{}"'.format(text.description))

        vertices = (['({},{})'.format(vertex.x, vertex.y)
                     for vertex in text.bounding_poly.vertices])
        reply[str(i)] = str(text.description)
        #print('bounds: {}'.format(','.join(vertices)))

    if response.error.message:
        raise ZPException(501, str(response.error.message))
    try :
        reply = str(reply).replace("\'", "\"")
        json = json.loads(reply)
    except (JSONDecodeError,RuntimeError, TypeError, NameError):
        raise ZPException(501, 'Error in OCRing')

    return json


def aadhaarNumVerify(sNum: str) -> bool:
    '''
    Verifies an aadhaar number using Verhoeff's algorithm
    '''
    verhoeff_table_d = (
        (0, 1, 2, 3, 4, 5, 6, 7, 8, 9),
        (1, 2, 3, 4, 0, 6, 7, 8, 9, 5),
        (2, 3, 4, 0, 1, 7, 8, 9, 5, 6),
        (3, 4, 0, 1, 2, 8, 9, 5, 6, 7),
        (4, 0, 1, 2, 3, 9, 5, 6, 7, 8),
        (5, 9, 8, 7, 6, 0, 4, 3, 2, 1),
        (6, 5, 9, 8, 7, 1, 0, 4, 3, 2),
        (7, 6, 5, 9, 8, 2, 1, 0, 4, 3),
        (8, 7, 6, 5, 9, 3, 2, 1, 0, 4),
        (9, 8, 7, 6, 5, 4, 3, 2, 1, 0))

    verhoeff_table_p = (
        (0, 1, 2, 3, 4, 5, 6, 7, 8, 9),
        (1, 5, 7, 6, 2, 8, 3, 0, 9, 4),
        (5, 8, 0, 3, 7, 9, 6, 1, 4, 2),
        (8, 9, 1, 6, 0, 4, 3, 5, 2, 7),
        (9, 4, 5, 3, 1, 2, 6, 8, 7, 0),
        (4, 2, 8, 6, 5, 7, 3, 9, 0, 1),
        (2, 7, 9, 3, 8, 0, 6, 4, 1, 5),
        (7, 0, 4, 6, 9, 1, 3, 2, 5, 8))

    # verhoeff_table_inv = (0, 4, 3, 2, 1, 5, 6, 7, 8, 9)

    def checksum(s: str) -> int:
        '''For a given number generates a Verhoeff digit and returns number + digit'''
        c = 0
        for i, item in enumerate(reversed(s)):
            c = verhoeff_table_d[c][verhoeff_table_p[i % 8][int(item)]]
        return c

    # Validate Verhoeff checksum
    return checksum(sNum) == 0 and len(sNum) == 12


def updateTrip(_loc, trip: Trip):
    '''
    This method calculates the trip completion %age and updates the progress table
    TODO: add google api to do this
    '''
    # For now just pretend progress is being made, Later set progress based on vehicle/driver location
    prog = Progress.objects.get(tid=trip.id)
    currPct = prog.pct
    if currPct < 100:
        x=random.randint(0, 2)
        if x % 3 == 0:
            currPct = currPct + 1
            prog.pct = currPct
            prog.save()


# Helper to generate auth
def getClientAuth(an: str, pn: str) -> str:
    sText = 'zippee-salt-' + an + '-' + pn
    shaText = sText.encode('utf-8')  # setting the encoding
    m = hashlib.sha256()
    m.update(shaText)
    return base64.b64encode(m.digest()).decode()[:8]



def getRoutePrice(idSrc, idDst, iVType, iPayMode, iTimeSec=0):
    '''
    Determines the price given the trip details and time taken
    if time taken is not provided, its estimated from vehicle type
    '''
    # Get this route distance
    recRoute = Route.getRoute(idSrc, idDst)
    fDist = recRoute.dist
    iVType, iPayMode, iTimeSec = int(iVType), int(iPayMode), int(iTimeSec) #need explicit type conversion to int
    
    # Calculate the speed if time is known or else use average speed for estimates
    fAvgSpeed = Vehicle.AVG_SPEED_M_PER_S[iVType] if iTimeSec == 0 else fDist / iTimeSec

    # Get base fare for vehicle
    fBaseFare = Vehicle.BASE_FARE[iVType]

    # Get average economic weight
    idSrcWt = Place.objects.filter(id=idSrc)[0].wt
    idDstWt = Place.objects.filter(id=idDst)[0].wt
    avgWt = (idSrcWt + idDstWt) / 200

    # get per km price for vehicle
    maxPricePerKM = 15
    vehiclePricePerKM = (iVType / 4) * maxPricePerKM

    # Calculate price
    price = fBaseFare + (fDist / 1000) * vehiclePricePerKM * avgWt
    if iPayMode == Trip.UPI:
        price *= 0.9

    return {
        'price': float('%.0f' % price),
        'time' : float('%.0f' % ((fDist/fAvgSpeed)/60)), #converted seconds to minutes
        'dist': float('%.0f' % (fDist / 1000)),
        'speed': float('%.0f' % (fAvgSpeed * 3.6 ))
    }


def getRentPrice(idSrc, idDst, iVType, iPayMode, iTimeHrs=0):
    '''
    Determines the price given the rent details and time taken
    time is etime - stime
    '''
    # Get this route distance
    recRoute = Route.getRoute(idSrc, idDst)
    fDist = recRoute.dist
    iVType, iPayMode, iTimeHrs = int(iVType), int(iPayMode), int(iTimeHrs)  # need explicit type conversion to int

    iTimeSec = iTimeHrs * 3600
    fAvgSpeed = Vehicle.AVG_SPEED_M_PER_S[iVType] if iTimeSec == 0 else fDist / iTimeSec

    lstPrice = [90, 80, 70, 60, 50, 50, 50] #for every 2 hours

    price = lstPrice[0]
    if iTimeHrs == 4 :
        price += lstPrice[1]
    elif iTimeHrs == 8:
        price += sum(lstPrice[1:3])
    elif iTimeHrs == 12:
        price += sum(lstPrice[1:4])
    else : #24c hours
        price += sum(lstPrice[1:6])

    return {
        'price': float('%.0f' % price),
        'time': float('%.0f' % ((fDist / fAvgSpeed) / 60)),  # converted seconds to minutes
        'dist': float('%.0f' % (fDist / 1000)),
        'speed': float('%.0f' % (fAvgSpeed * 3.6))
    }

def getTripPrice(trip):
    '''
    Gets price info for non active trips
    '''
    vehicle = Vehicle.objects.filter(an=trip.van)[0]
    if trip.rtype == '0':
        return getRoutePrice(1, 2, vehicle.vtype, trip.pmode, (trip.etime - trip.stime).seconds)
    else :
        return getRentPrice(trip.srcid, trip.dstid, vehicle.vtype, trip.pmode, trip.hrs)

def getDelPrice(deli):
    '''
    Gets price info for non active deliveries
    '''
    # vehicle = Vehicle.objects.filter(an=deli.van)[0]
    return getDeliveryPrice(deli.srclat, deli.srclng, deli.dstlat, deli.dstlng, 1, 1) #ToDO fix this for the dimensions

###########################################


sys.path.insert(0, os.path.dirname(__file__))

############################################
logging.warning('Enter the Matrix')
#
# action (api name)
# actor(entity)
# time (time stamp, in UTC and IST)
# response
# ACTOR called ACTION at [TIME]
############################################
# Constants

sLogDIRName = settings.LOG_DIR
sLogFileName = sLogDIRName + "logFile.csv"
field_names = ['api', 'timestampUTC', 'timestampIST', 'response']

############################################
# Functions

def logEvent(funcName, status ): #entityName, funcName, status ):
    '''
    Logs the event to the file

    funName is the API being hit, status is the JSON reponse
    '''
    currTimeUTC = timezone.now()
    tdISTdelta  = timedelta(hours=5, minutes=30)
    currTimeIST = currTimeUTC + tdISTdelta

    # Writing to a txt file
    #with open(sLogFileName, 'a+') as logFile:
    #    # logFile.write("<" + entityName + ">" +" hit the API : " + funcName + " at TIME : [" + str(
    #    logFile.write("hit the API : " + funcName + " at TIME : [" + str(
    #            currTimeUTC) + "(in UTC)] and in IST : [" + str(currTimeIST) + "! Status : " + str(status) + ",\n")

    # Writing to a CSV
    with open(sLogDIRName, 'a+', encoding='utf-8') as file:
        csvwriter = csv.DictWriter(file, field_names)
        #csvwriter.writeheader()
        csvwriter.writerow({'api': funcName, 'timestampUTC': currTimeUTC, 'timestampIST': currTimeIST, 'response': status})



###########################################
# Decorators

def extractParams(func):
    '''
    Decorator applied to a view function to retrieve GET/POST data as a dict
    POST is treated as JSON
    '''
    def _decorator(request):
        log('extractParams:' + func.__name__)
        if request.method == 'GET':
            dct = dict(request.GET.items())
        else:
            #print("REEEEEEEEEEEEEEEEEE : ", request.body)
            #log(request.body.decode('utf-8'))
            dct = dict(json.loads(request.body.decode('utf-8')))
        return func(request, dct)

    return wraps(func)(_decorator)


class checkAuth(object):
    '''
    Decorator class that performs authentication with auth=dct[auth] and returns the object
    Decides which table to check based on whether the function name starts with user, driver, admin or auth (which means either of user or driver)
    '''
    def __init__(self, driverMode=None):
        self.driverMode = driverMode
        pass

    def __call__(self, func):
        @wraps(func)
        def decorated_func(request, dct):
            # log('checkAuth:' + func.__name__)
            sFnName = func.__name__
            isDriver = sFnName.startswith('driver')
            isUser = sFnName.startswith('user')
            isAdmin = sFnName.startswith('admin')
            isAuth = sFnName.startswith('auth')
            isSuper = sFnName.startswith('sup')
            isAgent = sFnName.startswith('agent')

            if isAdmin:
                if dct.get('auth', '') != settings.ADMIN_AUTH:
                    return HttpJSONError('Forbidden', 403)
                else:
                    return func(dct)

            # If auth key checks out, then return JSON, else Unauthorized
            auth = dct.get('auth', '')
            if isUser or isAuth:
                qsUser = User.objects.filter(auth=auth)
                if (qsUser is not None) and len(qsUser) > 0:
                    return func(dct, qsUser[0])

            if isDriver or isAuth:
                qsDriver = Driver.objects.filter(auth=auth)

                # Ensure we have a confirmed driver
                if (qsDriver is not None) and (len(qsDriver) > 0) and qsDriver[0].mode != 'RG':
                    # Ensure the driver is in the desired state if any
                    if self.driverMode is None or qsDriver[0].mode in self.driverMode:
                        return func(dct, qsDriver[0])

            if isAgent or isAuth:
                qsAgent = Agent.objects.filter(auth=auth)

                # Ensure we have a confirmed Agent
                if (qsAgent is not None) and (len(qsAgent) > 0) and qsAgent[0].mode != 'RG':
                    # Ensure the agent is in the desired state if any
                    if self.driverMode is None or qsAgent[0].mode in self.driverMode: #agent is basically driver
                        return func(dct, qsAgent[0])

            if isSuper or isAuth:
                qsSuper = Supervisor.objects.filter(auth=auth)
                if (qsSuper is not None) and (len(qsSuper) > 0):
                    return func(dct, qsSuper[0])

            return HttpJSONError('Unauthorized', 403)

        return decorated_func


class handleException(object):
    '''
    handles exceptions and returns it as a HTTP response
    '''

    def __init__(self, class_name=None, message=None, status=None):
        self.class_name = class_name or DummyException
        self.status = status
        self.message = message

    def __call__(self, func):
        @wraps(func)
        def decorated_func(*args, **kwargs):
            try:
                return func(*args, **kwargs)
            except ZPException as ex:
                log(repr(ex))
                return ex.response()
            # in case of unspecified message/code, dump the exception text itself
            except self.class_name as ex:
                log(repr(ex))
                return HttpJSONError(self.message or str(ex), self.status or 501)
            except Exception:
                # print out exceptions which we didn't handle and also throw it to get stack trace
                #log(e)
                raise

        return decorated_func


class checkTripStatus(object):
    '''
    Decorator which ensures that the given entity has a trip and the latest trip status is
    one of the values specified in the decorators parameter (as an array)
    if arrValid == None all statuses are valid
    This MUST be used only after the checkAuth decorator
    '''
    def __init__(self, arrValid=None):
        self.arrValid = arrValid

    def __call__(self, func):
        @wraps(func)
        def decorated_func(dct, entity):
            #log('checkTripStatus:' + func.__name__)

            # Get any trip assigned to this entity
            qsTrip = Trip.objects.filter(id=entity.tid)

            # If there is a trip, ensure its status is within allowed
            if len(qsTrip) > 0:
                # arrValid == ['INACTIVE'] means "No trip should be active for this entity"
                if self.arrValid and len(self.arrValid) > 0 and self.arrValid[0] == 'INACTIVE':
                    return HttpJSONError('Trip already active', 400)

                # Ensure the trip has an allowed status
                bAllowAll = self.arrValid is None
                if bAllowAll or qsTrip[0].st in self.arrValid:
                    return func(dct, entity, qsTrip[0])
                else:
                    return HttpJSONError('Invalid trip or trip status', 404)

            return func(dct, entity, None)

        return decorated_func

###########################################
# simple Helpers

def retireEntity(entity: [User, Driver, Vehicle]) -> None :
    '''
    retired this entity by setting tid = -1
    '''
    entity.tid = -1
    entity.save()

def retireDelEntity(entity: [User, Agent, Vehicle]) -> None :
    '''
    retired this entity by setting tid = -1
    '''
    entity.did = -1
    entity.save()

###########################################

# Delivery module

def getDeliveryPrice(srclat, srclng, dstlat, dstlng, size, pmode):
    '''
    Determines the price given the rent details and time taken
    time is etime - stime
    #TODO add the variable of the size in this
    '''
    # Get this route distance

    qsPlaces = Place.objects.all().values()
    arrLocs = [recPlace for recPlace in qsPlaces]
    srcCoOrds = ['%s,%s' % (srclat,srclng)]
    dstCoOrds = ['%s,%s' % (dstlat,dstlng)]

    print(srcCoOrds,dstCoOrds)

    import googlemaps
    gmaps = googlemaps.Client(key=settings.GOOGLE_MAPS_KEY)
    dctDist = gmaps.distance_matrix(srcCoOrds, dstCoOrds)
    #log(dctDist)
    print( '############# DST : ', dctDist)
    if dctDist['status'] != 'OK':
        raise ZPException(501, 'Error fetching distance matrix')

    dctElem = dctDist['rows'][0]['elements'][0]
    nDist = 0
    nTime = 0
    if dctElem['status'] == 'OK':
        nDist = dctElem['distance']['value']
        nTime = dctElem['duration']['value']
    print('distance: ', nDist)
    print('time: ', nTime)

    fDist = nDist
    iVType, iPayMode, iTimeSec = 2, 1, nTime #int(iVType), int(iPayMode), int(iTimeSec)  # need explicit type conversion to int

    # Calculate the speed if time is known or else use average speed for estimates
    fAvgSpeed = Vehicle.AVG_SPEED_M_PER_S[iVType] if iTimeSec == 0 else fDist / iTimeSec

    # Get base fare for vehicle
    fBaseFare = Vehicle.BASE_FARE[iVType]

    # Get average economic weight
    idSrcWt = 100 # Place.objects.filter(id=idSrc)[0].wt
    idDstWt = 100 # Place.objects.filter(id=idDst)[0].wt
    avgWt = (idSrcWt + idDstWt) / 200

    # get per km price for vehicle
    maxPricePerKM = 15
    vehiclePricePerKM = (iVType / 4) * maxPricePerKM

    # Calculate price
    price = fBaseFare + (fDist / 1000) * vehiclePricePerKM * avgWt
    if iPayMode == Trip.UPI:
        price *= 0.9

    return {
        'price': float('%.0f' % price),
        'time': float('%.0f' % ((fDist / fAvgSpeed) / 60)),  # converted seconds to minutes
        'dist': float('%.0f' % (fDist / 1000)),
        'speed': float('%.0f' % (fAvgSpeed * 3.6))
    }


class checkDeliveryStatus(object):
    '''
    Decorator which ensures that the given entity has a Delivery and the latest Delivery status is
    one of the values specified in the decorators parameter (as an array)
    if arrValid == None all statuses are valid
    This MUST be used only after the checkAuth decorator
    '''
    def __init__(self, arrValid = None):
        self.arrValid = arrValid

    def __call__(self, func):
        @wraps(func)
        def decorated_func(dct, entity):
            log('checkTripStatus:' + func.__name__)
            print( "arr : " , self.arrValid)

            # Get any trip assigned to this entity
            an = entity.an
            qsDel = Delivery.objects.filter(uan=an, st__in=self.arrValid) if type(entity) is User else Delivery.objects.filter(dan=an)

            # If there is a delivery, ensure its status is within allowed
            if len(qsDel) > 0:
                # arrValid == ['INACTIVE'] means "No delivery    should be active for this entity"
                if self.arrValid and len(self.arrValid) > 0 and self.arrValid[0] == 'INACTIVE':
                    return HttpJSONError('Delivery already active', 400)
                print(qsDel)
                # Ensure the delivery has an allowed status
                bAllowAll = self.arrValid is None
                if bAllowAll or qsDel[len(qsDel)-1].st in self.arrValid:
                    return func(dct, entity, qsDel[0])
                else:
                    return HttpJSONError('Invalid delivery or delivery status', 404)

            return func(dct, entity, None)

        return decorated_func


def headers(h):
    """Decorator adding arbitrary HTTP headers to the response.

    This decorator adds HTTP headers specified in the argument (map), to the
    HTTPResponse returned by the function being decorated.

    Example:

    @headers({'Refresh': '10', 'X-Bender': 'Bite my shiny, metal ass!'})
    def index(request):
        ....
    """
    def headers_wrapper(fun):
        def wrapped_function(*args, **kwargs):
            response = fun(*args, **kwargs)
            for k,v in h.iteritems():
                response[k] = v
            return response
        return wrapped_function
    return headers_wrapper


###################################

# Ride with google maps


def getRidePrice(srclat, srclng, dstlat, dstlng, iVType, iPayMode, iTime=0):
    '''
    Determines the price given the rent details and time taken
    time is etime - stime
    '''
    # Get this route distance

    #qsPlaces = Place.objects.all().values()
    #arrLocs = [recPlace for recPlace in qsPlaces]
    srcCoOrds = ['%s,%s' % (srclat,srclng)]
    dstCoOrds = ['%s,%s' % (dstlat,dstlng)]

    print(srcCoOrds, dstCoOrds)

    import googlemaps
    gmaps = googlemaps.Client(key=settings.GOOGLE_MAPS_KEY)
    dctDist = gmaps.distance_matrix(srcCoOrds, dstCoOrds)
    #log(dctDist)
    print( '############# DST : ', dctDist)
    if dctDist['status'] != 'OK':
        raise ZPException(501, 'Error fetching distance matrix')

    dctElem = dctDist['rows'][0]['elements'][0]
    nDist = 0
    nTime = 0
    if dctElem['status'] == 'OK':
        print(dctElem)
        nDist = dctElem['distance']['value']
        nTime = dctElem['duration']['value']
    print('distance: ', nDist)
    print('time: ', nTime)

    fDist = nDist
    iVType, iPayMode = int(iVType), int(iPayMode)  # need explicit type conversion to int
    iTimeSec = nTime if iTime == 0 else iTime
    # Calculate the speed if time is known or else use average speed for estimates
    fAvgSpeed = Vehicle.AVG_SPEED_M_PER_S[iVType] if iTimeSec == 0 else fDist / iTimeSec

    # Get base fare for vehicle
    fBaseFare = Vehicle.BASE_FARE[iVType]

    # Get average economic weight
    # TODO how do I decide which area is hot ?
    idSrcWt = 100 # Place.objects.filter(id=idSrc)[0].wt
    idDstWt = 100 # Place.objects.filter(id=idDst)[0].wt
    avgWt = (idSrcWt + idDstWt) / 200

    # get per km price for vehicle
    maxPricePerKM = 15
    vehiclePricePerKM = (iVType / 4) * maxPricePerKM

    # Calculate price
    price = fBaseFare + (fDist / 1000) * vehiclePricePerKM * avgWt
    if iPayMode == Trip.UPI:
        price *= 0.9

    return {
        'price': float('%.0f' % price),
        'time': float('%.0f' % ((fDist / fAvgSpeed) / 60)),  # converted seconds to minutes
        'dist': float('%.0f' % (fDist / 1000)),
        'speed': float('%.0f' % (fAvgSpeed * 3.6))
    }


def getRiPrice(trip):
    '''
    Gets price info for non active trips
    '''
    vehicle = Vehicle.objects.filter(an=trip.van)
    vType = vehicle[0].vtype if len(vehicle)>0 else 1
    print(vType)
    return getRidePrice(trip.srclat, trip.srclng, trip.dstlat, trip.dstlng, vType, trip.pmode)#, (trip.etime - trip.stime).seconds)

