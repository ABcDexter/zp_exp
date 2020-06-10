#!/bin/python3


import base64
import hashlib
import json
import re
import os
import random
import sys
from datetime import datetime, timedelta
from json import JSONDecodeError

import csv

GOOGLE_APPLICATION_CREDENTIALS="/home/rep/projects/zp/server/google-cloud/MyFirstProject.json"


def doOCR(path):
    '''
    does the OCR for a given image
    '''

    def isEnglish(s):
        try:
            s.encode(encoding='utf-8').decode('ascii')
        except UnicodeDecodeError:
            return False
        else:
            return True

    OCRed = []
    import io
    from google.cloud import vision

    client = vision.ImageAnnotatorClient()

    with io.open(path, 'rb') as image_file:
        content = image_file.read()

    image = vision.types.Image(content=content)

    response = client.text_detection(image=image)
    texts = response.text_annotations

    arrText = []
    for text in texts:
        dct = {
            'desc': text.description,
            'bounds': [ (vertex.x, vertex.y) for vertex in text.bounding_poly.vertices ]
        }
        arrText.append(dct)

    print(json.dumps(arrText))


def fuzzMatch(s1, s2):
    mn = min(len(s1), len(s2))
    mx = max(len(s1), len(s2))
    nMatch = 0
    for i in range(mn):
        if s1[i] == s2[i]:
            nMatch += 1

    return nMatch / mx;


def onlyAscii(s):
    return ''.join([c for c in s if c.isascii()])

def noSpace(s):
    return ''.join([c for c in s if not c.isspace()])

def getREMatch(ret, key, regex, arrWords, bIgnoreCase=True, bNoSpace=True):
    for word in arrWords:
        w = noSpace(word) if bNoSpace else word
        m = re.search(regex, w, re.IGNORECASE) if bIgnoreCase else re.search(regex, w)
        if m:
            span = m.span()
            ret[key] = w[span[0]:span[1]]
            return;


def extractDetails(sFile):
    with open(sFile) as f:
        arrTexts = json.load(f)


        # Get the words and eliminate non ascii
        arrWords = arrTexts[0]['desc'].split('\n')

        arrWords = [onlyAscii(word).strip() for word in arrWords]
        arrWords = [word for word in arrWords if len(word) > 0]
        print(arrWords)

        # Look for "Government of India" fuzzily
        fMatch = fuzzMatch(noSpace(arrWords[0]).lower(), 'governmentofindia')
        if fMatch > 0.85:
            print('Confidence %d%%' % (fMatch * 100))
            arrWords.pop()

            ret = {}
            getREMatch(ret, 'name', r'[A-Z][a-z]*\s+[A-Z][a-z]+', arrWords, False, False)
            getREMatch(ret, 'dob', r'\d\d\/\d\d\/\d\d\d\d', arrWords)
            getREMatch(ret, 'an', r'\d{12}', arrWords)
            getREMatch(ret, 'gdr', r'male|female|transgender', arrWords)

            if 'dob' in ret:
                dob = datetime.strptime(ret.pop('dob'), '%d/%m/%Y').date()
                ret['age'] = datetime.today().year - dob.year

            print(ret)


if sys.argv[1] == 'parse':
    extractDetails(sys.argv[2])
else:
    doOCR(sys.argv[2])
