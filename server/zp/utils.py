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
from .models import Purchaser
from .models import Servitor

from django.conf import settings
import requests
from urllib.parse import urlencode

import googlemaps
#import pandas as pd
from math import ceil
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


def googleDistAndTime(srcCoOrds, dstCoOrds):
    '''

    Args:
        srcCoOrds: list of lat,lng of the source
        dstCoOrds: list of lat, lng of the destination

    Returns:
        dictionary of dist, time
    '''

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
    ret = {'dist': nDist, 'time': nTime}

    return ret


#################################
#OCR v2

STATES = [
    ('Andhra Pradesh', 'andhrapradesh',  	'AP'),
    ('Arunachal Pradesh', 'arunachalpradesh', 	'AR'),
    ('Assam', 'assam',	'AS' ),
    ('Bihar', 'bihar',	'BR'),
    ('Chhattisgarh', 'chhattisgarh', 	'CG'),
    ('Goa', 'goa', 	'GA'),
    ('Gujarat', 'gujarat', 	'GJ'),
    ('Haryana', 'haryana', 	'HR'),
    ('Himachal Pradesh', 'himachalrpradesh', 	'HP'),
    ('Jharkhand', 'jharkhand', 	'JH'),
    ('Karnataka', 'karnataka', 	'KA'),
    ('Kerala', 'kerala', 	'KL'),
    ('Madhya Pradesh', 'madhyapradesh', 	'MP'),
    ('Maharashtra', 'maharashtra', 	'MH'),
    ('Manipur', 'manipur', 	'MN'),
    ('Meghalaya', 'meghalaya', 	'ML'),
    ('Mizoram', 'mizoram', 	'MZ'),
    ('Nagaland', 'nagaland', 	'NL'),
    ('Odisha', 'orissa', 	'OD'), # name was changed
    ('Punjab', 'punjab', 	'PB'),
    ('Rajasthan', 'rajasthan', 	'RJ'),
    ('Sikkim', 'sikkim', 	'SK'),
    ('Tamil Nadu', 	'tamilnadu', 'TN'),
    ('Telangana', 'telangana' , 	'TS'),
    ('Tripura', 'tripura', 	'TR'),
    ('Uttar Pradesh', 'uttarpradesh', 	'UP'),
    ('Uttarakhand', 'uttaranchal', 	'UK'),
    ('West Bengal', 'westbengal', 	'WB'),

    ('Andaman and Nicobar Islands' , 'andamanandnicobarislands', 	'AN'),
    ('Chandigarh', 'chandigarh',	'CH'),
    ('Dadra and Nagar Haveli', 'dadradandnagarhaveli', 	'DD'), #two UTs clubbed together, but we are using different
    ('Daman and Diu', 'damananddiu', 'DD'),
    ('Delhi' 'delhi', 	'DL'),
    ('Jammu and Kashmir', 'jammuandkashmir', 	'JK'),
    ('Ladakh', 'ladakh', 	'LA'),
    ('Lakshadweep', 'lakshadweep', 	'LD'),
    ('Puducherry', 'puducherry', 	'PY')
 ]

pins_to_name = {
'248121' : ' Ajabpur, Dehradun ',
'248125' : ' Ambari, Dehradun ',
'248007' : ' Ambiwala, Dehradun ',
'248003' : ' Anarwala, Dehradun ',
'248199' : ' Tuini, Dehradun ',
'248001' : ' Araghar, Dehradun ',
'249201' : ' Ashutosh Nagar, Dehradun ',
'248252' : ' Thaina, Dehradun ',
'248143' : ' Kaluwala, Dehradun ',
'248140' : ' Resham Majri, Dehradun ',
'248005' : ' Tunwala, Dehradun ',
'248196' : ' Samalta, Dehradun ',
'248161' : ' Bhaniawala, Dehradun ',
'248008' : ' Tapovan, Dehradun ',
'248122' : ' Barlowganj, Dehradun ',
'248124' : ' Nada, Dehradun ',
'248197' : ' Tilwari, Dehradun ',
'248009' : ' Shahanshahi Ashram, Dehradun ',
'248123' : ' Tungra, Dehradun ',
'248141' : ' Ghangora, Dehradun ',
'248164' : ' Chhibroo, Dehradun ',
'249204' : ' S.N. temple, Dehradun ',
'248002' : ' Turner Road, Dehradun ',
'248145' : ' Rani Pokhari, Dehradun ',
'248165' : ' Ubhreau, Dehradun ',
'248167' : ' Dhakrani, Dehradun ',
'248142' : ' Vikasnagar, Dehradun ',
'248159' : ' Khadar, Dehradun ',
'249205' : ' Raiwala, Dehradun ',
'248160' : ' Harrawala, Dehradun ',
'248179' : ' Savoy Hotel, Dehradun ',
'248006' : ' Newforest, Dehradun ',
'248195' : ' Vijepur Hathibarka, Dehradun ',
'248152' : ' Jharipani, Dehradun ',
'248202' : ' K.P.kshetra, Dehradun ',
'248119' : ' Korwa, Dehradun ',
'248148' : ' Kulhal, Dehradun ',
'248171' : ' Mehuwala, Dehradun ',
'248110' : ' Mohbewala, Dehradun ',
'248115' : ' Mothrowala, Dehradun ',
'247670' : ' Narsan Kalan, Dehradun ',
'248010' : ' Nehrugram, Dehradun ',
'248198' : ' Vikasnagar, Dehradun ',
'249203' : ' Pashulok, Dehradun ',
'248126' : ' Ranjhawala, Dehradun ',
'248102' : ' Sahstradhara, Dehradun ',
'248146' : ' Seemadwar, Dehradun ',
'249202' : ' Virbhadra, Dehradun ',
'313601' : ' Vallabhnagar, Udaipur ',
'313611' : ' Tothada, Udaipur ',
'313804' : ' Sundra, Udaipur ',
'313702' : ' Vass, Udaipur ',
'313703' : ' Wali, Udaipur ',
'313604' : ' Sarangpura, Udaipur ',
'313031' : ' Sisarma, Udaipur ',
'313701' : ' Upreta, Udaipur ',
'313038' : ' Sheshpur, Udaipur ',
'313602' : ' Vana, Udaipur ',
'313026' : ' Tokar, Udaipur ',
'313905' : ' Veerpura, Udaipur ',
'313803' : ' Suveri, Udaipur ',
'313705' : ' Surajgarh, Udaipur ',
'307025' : ' Sandraf, Udaipur ',
'313203' : ' Vadiyar, Udaipur ',
'313802' : ' Ugamna Kotra, Udaipur ',
'313027' : ' Toda, Udaipur ',
'313902' : ' Thana, Udaipur ',
'313603' : ' Saleda, Udaipur ',
'313801' : ' Tidi, Udaipur ',
'313011' : ' Thoor, Udaipur ',
'313025' : ' Bari Tb (s), Udaipur ',
'313708' : ' Teja Ka was, Udaipur ',
'313024' : ' Zinc Smelter, Udaipur ',
'313204' : ' Wanri, Udaipur ',
'313003' : ' Udaipur Industrial area, Udaipur ',
'313605' : ' Sihar, Udaipur ',
'313001' : ' Udaipur University campus, Udaipur ',
'313201' : ' Veerdholia, Udaipur ',
'313903' : ' Surkhand Ka khera, Udaipur ',
'313205' : ' Vasni Kalan, Udaipur ',
'313015' : ' Umaramines, Udaipur ',
'313904' : ' Chawand, Udaipur ',
'313022' : ' Nandwel, Udaipur ',
'313901' : ' Zawar Mines, Udaipur ',
'313206' : ' Sanwar, Udaipur ',
'313704' : ' Sayra, Udaipur ',
'313002' : ' Udaipur H magri, Udaipur ',
'263157' : ' Tushrar, Nainital ',
'263159' : ' Syat, Nainital ',
'263126' : ' Rnibagh, Nainital ',
'263139' : ' Painth Parao, Nainital ',
'263143' : ' Arjunpur, Nainital ',
'263136' : ' Bhimtal, Nainital ',
'263163' : ' Bail Parao, Nainital ',
'263140' : ' Pawalgarh, Nainital ',
'263128' : ' Patwadanger, Nainital ',
'263134' : ' Unchakote, Nainital ',
'263132' : ' Supi, Nainital ',
'263141' : ' Kunwarpur, Nainital ',
'263131' : ' Bhumiadhar, Nainital ',
'263127' : ' Jeolikote, Nainital ',
'244715' : ' Tukura, Nainital ',
'263164' : ' Chhoi, Nainital ',
'263158' : ' Nathuwakhan, Nainital ',
'263135' : ' Talla Bardho, Nainital ',
'262580' : ' Dholigaon, Nainital ',
'263138' : ' Unura, Nainital ',
'263156' : ' Ghorakhal, Nainital ',
'263408' : ' Gularbhoj, Nainital ',
'262404' : ' Haldu Chaur, Nainital ',
'263002' : ' Tallital, Nainital ',
'263162' : ' Kamola, Nainital ',
'263001' : ' Pangoot, Nainital ',
'262402' : ' Lalkua, Nainital ',
'263129' : ' Manorapeak, Nainital ',
'263144' : ' Mota Haldu, Nainital ',
'263137' : ' Ramgarh, Nainital ',
'263155' : ' Sattal, Nainital '

}
count = 0
#################################

from fuzzywuzzy import fuzz as fuzzY

def doOCRback(path):
    '''
    does the OCR for backside,
    give the Home State and AN(aadhaar number again)
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
        frat = fuzzY.ratio(s1.lower(), s2.lower())
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
        print('Error!!!')
        #raise ZPException(501, str(response.error.message))

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
    # log(arrWords)

    ret = {}
    ret['count'] = 0
    # get AN again to see whether the front and the back are same or not
    getREMatch(ret, 'an', r'\d{12}', arrWords)

    # print(ret)
    sArr = [ word.split()[0] for word in arrWords[1:]]
    bFound = False
    for sA in reversed(sArr):
        if len(sA) > 2 : #smallest state isf Goa with len=3
            # Look for "Home State" fuzzily
            # len = range(STATES)
            for tState in STATES:
                ret['count'] = ret['count'] + 1
                fMatch = max( fuzzMatch(noSpace(sA.lower()), tState[0]), fuzzMatch(noSpace(sA.lower()), tState[1] ))
                print(sA, tState, fMatch, )

                if fMatch > 0.90 :
                    # print('Confidence %d%%' % (fMatch * 100))
                    ret['hs'] = tState[2]
                    bFound = True
                    break
        if bFound:
            break

    return ret


def getSCID(an: int, admin: int, rtime: datetime) -> int:
    '''
    Generates a deterministic SCID
    '''
    sText = 'zippee-otp-%s-%s-%s' % (str(an), str(admin), str(rtime))
    shaText = sText.encode('utf-8')
    m = hashlib.new('ripemd160')
    m.update(shaText)
    scid = m.hexdigest()[:10]
    return str(scid)


def getOTP(an: int, dan: int, rtime: datetime) -> int:
    '''
    Generates a deterministic 4 digit OTP
    '''
    print((str(an), str(dan), str(rtime)))
    sText = 'zippee-otp-%s-%s-%s' % (str(an), str(dan), str(rtime))
    shaText = sText.encode('utf-8')
    m = hashlib.new('ripemd160')
    m.update(shaText)
    otp = int(m.hexdigest(), 16) % 9999
    sOtp = str(otp)
    if len(sOtp) ==1:
        sOtp += "123"
    elif len(sOtp) ==2:
        sOtp += "42"
    elif   len(sOtp) == 3:
        sOtp += "0"
    otp = int(sOtp)
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
    TODO: add google Places and Geocoding api to do this
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
    #recRoute = Route.getRoute(idSrc, idDst)
    fDist = 6000 #iDist  #recRoute.dist
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
        'price': str(round(float('%.2f' % price),0))+'0', #make this round off to nearest decimal
        'time' : float('%.0f' % ((fDist/fAvgSpeed)/60)), #converted seconds to minutes
        'dist': float('%.0f' % (fDist / 1000)),
        'speed': float('%.0f' % (fAvgSpeed * 3.6 ))
    }


def getRentPrice(iTimeHrs=1, iTimeActualMins=0):
    '''
    #old was depending of the time as well, but other factors
    #def getRentPrice(idSrc, idDst, iVType, iPayMode, iTimeHrs=0):

    Determines the price given the rent details and time taken
    TIME is the only factor :

    iTimeHrs is the trip.hrs, min is 1 hour
    iTimeActualMins is etime - stime

    1:00:00 am	₹ 60.00
    2:00:00 am	₹ 54.00	total	₹ 114.00
    3:00:00 am	₹ 48.00	total	₹ 162.00
    4:00:00 am	₹ 42.00	total	₹ 204.00
    5:00:00 am	₹ 36.00	total	₹ 240.00
    6:00:00 am	₹ 30.00	total	₹ 270.00
    7:00:00 am	₹ 30.00	total	₹ 300.00
    8:00:00 am	₹ 30.00	total	₹ 330.00
    9:00:00 am	₹ 30.00	total	₹ 360.00
    10:00:00 am	₹ 30.00	total	₹ 390.00
    11:00:00 am	₹ 30.00	total	₹ 420.00
    12:00:00 pm	₹ 30.00 total	₹ 450.00

    1	    0-60 	                     ₹60.00
    0.95	0-120	₹114.00	  ₹ 0.00     ₹54.00
    0.9  	0-180	₹162.00	  ₹ 0.00     ₹48.00
    0.85	0-240	₹204.00	  ₹ 0.00     ₹42.00
    0.8	    0-300	₹240.00	  ₹ 0.00     ₹36.00
    0.75	0-360	₹270.00	  ₹ 0.00     ₹30.00
    0.7	    0-420	₹294.00	  ₹ 6.00     ₹24.00
    0.65	0-480	₹312.00	  ₹ 18.00    ₹18.00
    0.6	    0-540	₹324.00	  ₹ 36.00    ₹12.00
    0.55	0-600	₹330.00	  ₹ 60.00    ₹6.00
    0.5	    0-660	₹330.00	  ₹ 90.00    ₹0.00
    0.5	    0-720	₹360.00	  ₹ 90.00    ₹30.00

    1.00	 	₹ 60.00     ₹ 60.00
    0.90		₹ 108.00    ₹ 48.00
    0.80		₹ 144.00    ₹ 36.00
    0.75   	    ₹ 180.00    ₹ 36.00
    0.70		₹ 210.00    ₹ 30.00
    0.65		₹ 234.00    ₹ 24.00
    0.60   	    ₹ 252.00    ₹ 18.00
    0.55		₹ 264.00    ₹ 12.00
    0.50		₹ 270.00    ₹ 6.00
    0.50        ₹ 300.00    ₹ 30.00
    0.50        ₹ 330.00    ₹ 30.00
    0.50        ₹ 360.00    ₹ 30.00
    '''
    iMaxSpeed = 25  # capped to 25 kmph
    iTimeActualMins = int(iTimeHrs) * 60 if iTimeActualMins == 0 else int(iTimeActualMins)
    #hrs converted to mins
    #lstPrice = [ 0, 100, 90, 80, 70, 60, 50, 50, 50, 50, 50, 50, 50, 50]  # paise per minute for every 1 hour
    #idx      = [ 0 , 1 , 2 , 3 , 4 , 5 , 6 , 7 , 8 , 9 , 10, 11, 12, 13 ] # for every 1 hour, after 12th hour maybe charge extra
    #idxNext  = [ 0, 70, 130, 190, 250, 310, 370, 430, 490, 550, 610, 670, 730 ] # for what should be the limit for next hours charges
    #lstActualPrice = [0, 60.00, 54.00, 48.00, 42.00, 36.00, 30.00, 30.00, 30.00, 30.00, 30.00, 30.00, 30.00 ] #finalPrice

    #idxNext = [ 0, 60, 120, 180, 240, 300, 300, 420, 480, 540, 600, 660, 720,
    #             780, 840, 900, 960, 1020, 1080, 1140, 1200, 1260, 1320, 1380, 1440]  # for next hours charges
    #lstUpdatedPrice = [0, 60.00, 54.00, 48.00, 42.00, 36.00, 30.00, 24.00, 18.00, 12.00, 6.00, 0.00, 30.00
    #                  ]  # finalPrice

    idxNext = [0, 60, 120, 180, 240, 300, 300, 420, 480, 540, 600, 660, 720]
    #             780, 840, 900, 960, 1020, 1080, 1140, 1200, 1260, 1320, 1380, 1440]  # for next hours charges
    lstUpdatedPrice = [0, 60.00, 48.00, 36.00, 36.00, 30.00, 24.00, 18.00, 12.00, 6.00, 30.00, 30.00, 30.00, 30.00]  # finalPrice

    try:
        idxMul = next(x[0] for x in enumerate(idxNext) if x[1] >= iTimeActualMins) #Find the correct value from the
    except StopIteration:
        idxMul = 12
    price = sum(lstUpdatedPrice[1:idxMul+1]) # sum(lstActualPrice[1:idxMul+1])
    # price = lstPrice[iTimeHrs] * iTimeSec
    return {
        'price': str(round(float('%.2f' % price),0))+'0',
        'speed': round(iMaxSpeed,0) #(fAvgSpeed * 3.6))
    }


def getTripPrice(trip):
    '''
    Gets price info for non active trips
    '''
    vehicle = Vehicle.objects.filter(an=trip.van)[0]
    if trip.rtype == '0':
        return getRidePrice(trip.srclat, trip.srclng, trip.dstlat, trip.dstlng, vehicle.vtype, trip.pmode, (trip.etime - trip.stime).seconds)
    else :
        hrs = datetime.now(timezone.utc) - trip.stime if trip.st == 'ST' else trip.etime- trip.stime
        return getRentPrice(trip.hrs, hrs.seconds//60) #convert seconds to minutes


def getDelPrice(deli, hs):
    '''
    Gets price info for non active deliveries
    '''
    # vehicle = Vehicle.objects.filter(an=deli.van)[0]
    # home state of user
    # print(" TIP IS : ", deli.tip)
    exp = '1' if deli.express is True else '0'
    return getDeliveryPrice(deli.srclat, deli.srclng, deli.dstlat, deli.dstlng, deli.idim, 1, exp, hs, deli.tip)

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
            isPurchaser = sFnName.startswith('purchase')
            isServitor = sFnName.startswith('servi')
            
            if isAdmin:
                if dct.get('auth', '') != settings.ADMIN_AUTH:
                    return HttpJSONError('Forbidden', 403)
                else:
                    return func(dct)

            # If auth key checks out, then return JSON, else Unauthorized
            auth = dct.get('auth', '')
            print("auth is :", auth, "...")
            if isUser or isAuth:
                qsUser = User.objects.filter(auth=auth)
                if (qsUser is not None) and len(qsUser) > 0:
                    return func(dct, qsUser[0])

            if isDriver or isAuth:
                qsDriver = Driver.objects.filter(auth=auth)
                print("driver : ", qsDriver)
                # Ensure we have a confirmed driver
                if (qsDriver is not None) and (len(qsDriver) > 0) and qsDriver[0].mode != 'RG':
                    # Ensure the driver is in the desired state if any
                    if self.driverMode is None or qsDriver[0].mode in self.driverMode:
                        return func(dct, qsDriver[0])

            if isAgent or isAuth:
                qsAgent = Agent.objects.filter(auth=auth)
                print("agents : ", qsAgent)
                # Ensure we have a confirmed Agent
                if (qsAgent is not None) and (len(qsAgent) > 0) and qsAgent[0].mode != 'RG':
                    # Ensure the agent is in the desired state if any
                    if self.driverMode is None or qsAgent[0].mode in self.driverMode: #agent is basically driver
                        return func(dct, qsAgent[0])

            if isSuper or isAuth:
                qsSuper = Supervisor.objects.filter(auth=auth)
                if (qsSuper is not None) and (len(qsSuper) > 0):
                    return func(dct, qsSuper[0])

            if isPurchaser or isAuth:
                qsPur = Purchaser.objects.filter(auth=auth)
                if (qsPur is not None) and (len(qsPur) > 0):
                    return func(dct, qsPur[0])
                    
            if isServitor or isAuth:
                qsSer = Servitor.objects.filter(auth=auth)
                if (qsSer is not None) and (len(qsSer) > 0):
                    return func(dct, qsSer[0])


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


def getDeliveryPrice(srclat, srclng, dstlat, dstlng, size, pmode, express, hs, tip):
    '''
    Determines the price given the rent details and time taken
    time is etime - stime
    '''
    # Get this route distance

    qsPlaces = Place.objects.all().values()
    arrLocs = [recPlace for recPlace in qsPlaces]
    srcCoOrds = ['%s,%s' % (srclat,srclng)]
    dstCoOrds = ['%s,%s' % (dstlat,dstlng)]

    # print(srcCoOrds,dstCoOrds)

    gMapsRet = googleDistAndTime(srcCoOrds, dstCoOrds)
    nDist, nTime = gMapsRet['dist'], gMapsRet['time']

    fDist = nDist
    iVType, iPayMode, iTimeSec = 2, 1, nTime*60

    # Calculate the speed if time is known or else use average speed for estimates
    fAvgSpeed = Vehicle.AVG_SPEED_M_PER_S[iVType] if iTimeSec == 0 else fDist / iTimeSec

    # Get base fare for vehicle
    if str(hs).lower() == 'UK'.lower():
        fBaseFare = 20.00
    else:
        fBaseFare = 50.00  # Vehicle.BASE_FARE[iVType]

    # Calculate price
    price = fBaseFare  # + (fDist / 1000) * vehiclePricePerKM * avgWt
    if fDist > 5000:
        price += ceil((fDist - 5000) / 1000) * 10.00
        #print(fDist, ceil((fDist - 5000) / 1000), price )
    #if iPayMode == Trip.UPI:
    #    price *= 0.9
    # print("EXPRESS : ", express)
    if express == '1':
        price += 20.00  # 20 Rs extra for express
        # print('Expresss okay############')
    '''
    # L = 10
    # XL = 20
    # XXL = 30
    '''
    if size == 'L':
        price += 10.00
    elif size == 'XL':
        price += 20.00
    elif size == 'XXL':
        price += 30.00

    price += tip
    # print( "PRICE : ", price)
    return {
        'price': str(round(float('%.2f' % price),0))+'0',
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
            qsDel = Delivery.objects.filter(uan=an, st__in=self.arrValid) if type(entity) is User else Delivery.objects.filter(dan=an, st__in=self.arrValid)

            # If there is a delivery, ensure its status is within allowed
            if len(qsDel) > 0:
                # arrValid == ['INACTIVE'] means "No delivery    should be active for this entity"
                if self.arrValid and len(self.arrValid) > 0 and self.arrValid[0] == 'INACTIVE':
                    return HttpJSONError('Delivery already active', 400)
                # print(qsDel)
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
    #Get this route distance using google's APIs
    
    Determines the price given 
        srclat : latitude of the source 
        srclng : longitude of the source 
        dstlat : latitude of the destination
        dstlng : longitude of the destination
        iVType : type of the vehicle (0 for cycle, 1 for scooty, 2 for bike, 3 for ZBee)
        iPayMode : payment type (0 for cash, 1 for UPI)
        iTime : time taken for the ride in seconds, this is required to calculate actual price (for eg drierRideEnd )
    
    returns 
        'price': Price for t,
        'time': time taken in minutes  # converted seconds to minutes
        'dist': Distance in Kilometers,
        'speed': Average speed 
    '''
    # Get this route distance

    #qsPlaces = Place.objects.all().values()
    #arrLocs = [recPlace for recPlace in qsPlaces]
    srcCoOrds = ['%s,%s' % (srclat,srclng)]
    dstCoOrds = ['%s,%s' % (dstlat,dstlng)]

    #print(srcCoOrds, dstCoOrds)

    gMapsRet = googleDistAndTime(srcCoOrds, dstCoOrds)
    nDist, nTime = gMapsRet['dist'], gMapsRet['time']
    #print(nDist, nTime)
    fDist = nDist
    iVType, iPayMode = int(iVType), int(iPayMode)  # need explicit type conversion to int
    iTimeSec = nTime*60 if iTime == 0 else iTime
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
    if iPayMode == Trip.UPI:  # UPI has 10% off
        price *= 0.9

    #if str(hs) == 'UK': # 10% off for natives
    #   price *= 0.9

    #if str(gdr) == 'F': # 25% off for females
    #   price *= 0.75

 
    return {
        'price': str(round(float('%.2f' % price),0))+'0',
        'time': int('%.0f' % ((fDist / fAvgSpeed) / 60)),  # converted seconds to minutes
        'dist': float('%.2f' % (fDist / 1000)),
        'speed': float('%.2f' % (fAvgSpeed * 3.6))
    }


def getRiPrice(trip):
    '''
    Gets price info for non active trips
    '''
    vehicle = Vehicle.objects.filter(an=trip.van)
    vType = vehicle[0].vtype if len(vehicle)>0 else 1
    #print(vType)
    return getRidePrice(trip.srclat, trip.srclng, trip.dstlat, trip.dstlng, vType, trip.pmode)#, (trip.etime - trip.stime).seconds)

###############
# GOOGLE

def extract_lat_lng(address_or_postalcode, data_type = 'json'):
    """

    Args:
        address_or_postalcode:
        data_type:

    Returns:
        lat,lng of the place
    """
    endpoint = f"https://maps.googleapis.com/maps/api/geocode/{data_type}"
    params = {"address": address_or_postalcode, "key": settings.GOOGLE_PLACES_KEY}
    url_params = urlencode(params)
    url = f"{endpoint}?{url_params}"
    r = requests.get(url)
    if r.status_code not in range(200, 299):
        return {}
    latlng = {}
    try:
        print(r.json()['results'][0]['address_components'][1]['long_name'])
        latlng = r.json()['results'][0]['geometry']['location']
    except:
        pass
    return latlng.get("lat"), latlng.get("lng")


def extract_name_from_pin(address_or_postalcode, data_type='json'):
    """

    Args:
        address_or_postalcode:
        data_type:

    Returns:
        lat,lng of the place
    """
    '''
    endpoint = f"https://maps.googleapis.com/maps/api/geocode/{data_type}"
    params = {"address": address_or_postalcode, "key": settings.GOOGLE_PLACES_KEY}
    url_params = urlencode(params)
    url = f"{endpoint}?{url_params}"
    r = requests.get(url)
    townName = {}
    if r.status_code not in range(200, 299):
        return townName

    townName['name'] = ''
    # try:
    print(r.json())
    print(r.json()['results'][0]['address_components'][1]['long_name'])
    townName['name'] = r.json()['results'][0]['address_components'][1]['long_name']
    # r.json()['results'][0]['geometry']['location']
    # except:
    '''
    townName = {}
    if address_or_postalcode in pins_to_name:
        townName['name'] = pins_to_name[address_or_postalcode]
    else:
        townName['name'] = "This PIN code is not serviceable yet."
    return townName



'''
data = pd.read_excel('./pin_codes.xlsx')
>>> data
           State  District      Location  Pincode
0    Uttarakhand  Dehradun       Ajabpur   248121
1    Uttarakhand  Dehradun        Ambari   248125
2    Uttarakhand  Dehradun      Ambiwala   248007
3    Uttarakhand  Dehradun      Anarwala   248003
4    Uttarakhand  Dehradun           Anu   248199
..           ...       ...           ...      ...
857  Uttarakhand  Nainital  Thala Manral   244715
858  Uttarakhand  Nainital        Tukura   244715
859  Uttarakhand  Nainital       Tushrar   263157
860  Uttarakhand  Nainital     Unchakote   263134
861  Uttarakhand  Nainital         Unura   263138

[862 rows x 4 columns]
>>> rows = [ row for ix, row in data.iterrows()]
>>> rows
len(rows)
862
>>> 
>>> 
>>> 
>>> 
>>> 
>>> nainital
set()
>>> for i in range(862):
...     if rows[i]['District'] == 'Nainital':
...             nainital.add(rows[i]['Pincode'])
... 
>>> #data = pd.read_excel('./pin_codes.xlsx')
>>> len(nainital)

'''
def sendInvoiceMail(userEmail, userName, tripId, tripDate, tripTime, tripPrice, tripCGST, tripSGST, tripTotal ):
    '''
    sends mail to the user with
    Args:
        userEmail
        userName
        tripPrice
        tripTime
    #TODO take the message as per what happens to the ride, say Ride was TOed then send apt email
    
    '''
    import smtplib, ssl
    from email.mime.text import MIMEText
    from email.mime.multipart import MIMEMultipart

    SENDER_SERVER = "localhost"
    FROM = "zippe@villageapps.in"
    TO = str(userEmail)

    message = MIMEMultipart("alternative")
    message["Subject"] = "Zipp-e Trip Invoice # %s" % (str(tripId))
    message["From"] = FROM
    message["To"] = TO

    context = ssl.create_default_context()

    # Prepare textual message
    body = """\
    Hi %s, """ % (str(userName))#+"""\"""
    #\n
    #Thanks for riding with Zippe!\n"""
    print(body)

    html = ("""<!doctype html> <html>  <head>  <meta charset="utf-8">  <title>Zipp-e Trip Invoice</title>  <style>  .invoice-box {  max-width: 800px""" + str(';') + """  margin: auto""" + str(';') + """  padding: 30px""" + str(';') + """  border: 1px solid #eee""" + str(';') + """  box-shadow: 0 0 10px rgba(0, 0, 0, .15)""" + str(';') + """  font-size: 16px""" + str(';') + """  line-height: 24px"""  
    + str(';') + """  font-family: 'Helvetica Neue', 'Helvetica', Helvetica, Arial, sans-serif""" + str(';') + """  color: #555""" + str(';') + """  }    .invoice-box table {  width: 100%""" + str(';') + """  line-height: inherit""" + str(';') + """  text-align: left""" + str(';') + """  }    .invoice-box table td {  padding: 5px""" + str(';') + """  vertical-align: top""" + str(';') + """  }    .invoice-box table tr td:nth-child(2) {  text-align: right""" 
    + str(';') + """  }    .invoice-box table tr.top table td {  padding-bottom: 20px""" + str(';') + """  }    .invoice-box table tr.top table td.title {  font-size: 45px""" + str(';') + """  line-height: 45px""" + str(';') + """  color: #333""" + str(';') + """  }    .invoice-box table tr.information table td {  padding-bottom: 40px""" + str(';') + """  }    .invoice-box table tr.heading td {  background: #eee""" + str(';') + """  border-bottom: 1px solid #ddd""" 
    + str(';') + """  font-weight: bold""" + str(';') + """  }    .invoice-box table tr.details td {  padding-bottom: 20px""" + str(';') + """  }    .invoice-box table tr.item td{  border-bottom: 1px solid #eee""" + str(';') + """  }    .invoice-box table tr.item.last td {  border-bottom: none""" + str(';') + """  }    .invoice-box table tr.total td:nth-child(2) {  border-top: 2px solid #eee""" + str(';') + """  font-weight: bold""" 
    + str(';') + """  }    @media only screen and (max-width: 600px) {  .invoice-box table tr.top table td {  width: 100%""" + str(';') + """  display: block""" + str(';') + """  text-align: center""" + str(';') + """  }    .invoice-box table tr.information table td {  width: 100%""" + str(';') + """  display: block""" + str(';') + """  text-align: center""" + str(';') + """  }  }    /** RTL **/  .rtl {  direction: rtl""" 
    + str(';') + """  font-family: Tahoma, 'Helvetica Neue', 'Helvetica', Helvetica, Arial, sans-serif""" + str(';') + """  }    .rtl table {  text-align: right""" + str(';') + """  }    .rtl table tr td:nth-child(2) {  text-align: left""" + str(';') + """  }  </style>  </head>  <body>  <div class="invoice-box">  <table cellpadding="0" cellspacing="0">  <tr class="top">  <td colspan="2">  <table>  <tr>  <td class="title">  <img src="https://i.imgur.com/1WEczdk.png" style="width:100.0%""" 
    + str(';') + """ max-width:100px""" + str(';') + """">  </td>    </tr>  </table>  </td>  </tr>    <tr class="information">  <td colspan="2">  <table>  <tr>  <td>  Zippe &#169""" + str(';') + """ <br>  Village Connect Pvt. Ltd. <br>  H No. 33, Naukuchiatal,<br>  Village Chanoti, Nainital,<br>  Uttarakhand, 263136  </td>   """)
    parsed = """ <td>  Invoice # : %s<br>  Dated : %s<br>  </td>  </tr>  </table>  </td>  </tr>      <tr class="heading">  <td>  Item  </td>    <td>  Details  </td>  </tr>  <tr class="item">  <td>  Trip time  </td>  <td>  %s minutes  </td>  </tr>    <tr class="item">  <td>  Bill amount  </td>  <td>  %s  </td>  </tr>  <tr class="item">  <td>  CGST ( 2.5 percent )  </td>    <td>  %s   </td>  </tr>    <tr class="item last">  <td>  SGST ( 2.5 percent)  </td>    <td>  %s  </td>  </tr>    <tr class="total">  <td></td>    <td>  Total: %s  </td>  </tr>  </table>  </div>    <br>  Note : Fares are inclusive of GST.  </body>  </html>""" % ( str(0)+str(tripId), str(tripDate), str(tripTime), str(u"\u20B9")+" "+ str(tripPrice), str(u"\u20B9")+" "+str(tripCGST), str(u"\u20B9") + " "+str(tripSGST),str(u"\u20B9")+" "+str(tripTotal))
    
    msg = body + html + parsed
    print(html)

    #set the correct MIMETexts
    part1 = MIMEText(body, "plain")
    #part2 = MIMEText(html, "html")
    part3 = MIMEText(msg, "html")

    #attach the parts to actual message
    message.attach(part1)
    #message.attach(part2)
    message.attach(part3)
    # Send the mail
    print(message.as_string())
    server = smtplib.SMTP(SENDER_SERVER)
    server.sendmail(FROM, TO, message.as_string())
    server.quit()
    
    
