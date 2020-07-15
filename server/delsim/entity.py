#!/bin/python3

import os
import json
import urllib.request
import time
import random
import datetime # DO NOT remove this import

'''
Simulation entity common code for
'''


def dump(x):
    print(json.dumps(x))


def prob(fProb):
    r = random.random()
    return r < fProb

FINISHED_STATUS_MSGS = {
    'PD': 'Delivery paid for',
    'CN': 'Delivery was cancelled by user',
    'DN': 'Delivery was denied by agent',
    'FL': 'Delivery failed',
    'TO': 'Delivery timed out',
}


class Entity:
    def __init__(self):
        self.sLastMsg = ''
        self.sAuth = ''
        self.sDID = -1
        self.iPID = -1
        self.iDstPID = -1
        self.delay = 3
        self.bPollVehicle = False
        self.bPollRide = False
        self.sAadhaar = ''


    ADMIN_AUTH = '437468756c68752066687461676e'
    SERVER_URL = os.environ.get('ZP_URL', 'http://127.0.0.1:9999/')           # localhost
    #SERVER_URL = os.environ.get('ZP_URL', 'https://api.villageapps.in:8090/')  # server

    def callAPI(self, sAPI, dct={}, auth=None):
        # print(sAPI, dct)
        if sAPI.startswith('admin'):
            dct['auth'] = Entity.ADMIN_AUTH
        else:
            dct['auth'] = auth if auth else self.sAuth
            #print(sAPI.startswith('auth-admin'))
            if sAPI.startswith('auth-admin'): # needed for authAdminEntityUpdate
                dct['adminAuth'] = Entity.ADMIN_AUTH
        #print("### new  :::", sAPI, dct)
        sUrl = Entity.SERVER_URL + sAPI
        dctHdrs = {'Content-Type': 'application/json'}
        jsonData = json.dumps(dct).encode()
        req = urllib.request.Request(sUrl, headers=dctHdrs, data=jsonData)
        jsonResp = urllib.request.urlopen(req, timeout=30).read()
        ret = json.loads(jsonResp)
        return ret


    def logIfErr(self, dctRet):
        if 'error' in dctRet:
            self.log('Error: %s' % dctRet['error'])
            return True
        return False


    # Given a trip and a percentage, gives lat and long linearly interpolated based on pctProgress
    def getProgressLocation(self, dctPlaceSrc, dctPlaceDst, pctProgress):
        x1 = dctPlaceSrc['lng']
        x2 = dctPlaceDst['lng']
        y1 = dctPlaceSrc['lat']
        y2 = dctPlaceDst['lat']

        f = pctProgress / 100
        x = x1 + (x2 - x1) * f
        y = y1 + (y2 - y1) * f

        return y, x

    def showDeliveryProgress(self):
        ret = self.callAPI('auth-progress-percent')
        if not self.logIfErr(ret):
            self.log('Delivery progress %s%%' % ret['pct'])
            self.iPID = ret['pid']
            return ret['pct']

    # Only on ST
    def maybeFailDelivery(self, typ, fProb=0.0001):  # p is the probability to cancel, 1% by default
        # once in 10000 fail the delivery
        if prob(fProb):
            self.log(str(typ) + ' Failing delivery!')
            ret = self.callAPI('auth-delivery-fail')
            self.logIfErr(ret)
            return True
        return False

    # Only AS for driver, RQ, AS, ST for user
    def maybeCancelDelivery(self, typ, fProb=0.1):  # p is the probability to cancel, 10% by default
        # once in 10 cancel the trip
        if prob(fProb):
            self.log('Canceling trip!')
            ret = self.callAPI(typ + '-delivery-cancel')
            self.logIfErr(ret)
            return True
        return False

    def rideRetire(self, entity=None, state=None):
        '''
        Retire the trip for good (FL already retire by adminSimul)
        '''
        self.log('retiring %s trip for %s' % (state, entity))
        ret = {}
        if entity == 'driver' and state in ['TO', 'CN']:
            ret = self.callAPI('driver-ride-retire')

        elif entity == 'user' and state in ['TO', 'DN', 'PD']:
            ret = self.callAPI('user-ride-retire')

        if not self.logIfErr(ret):
            self.log('Retired trip')
            self.sTID = -1


    def handleFinishedDelivery(self, entity=None):
        ret = self.callAPI('auth-delivery-get-info', {'tid': self.sTID})
        if not self.logIfErr(ret):
            st = ret['st']
            if st != 'FL':
                sMsg = FINISHED_STATUS_MSGS.get(st, 'Delivery in unexpected state: %s , for : %s' .format(st, entity))
                self.log(sMsg)
                if st in FINISHED_STATUS_MSGS.keys(): # TO, CN, DN, PD # FL is retired by adminHandleFailedDelivery()
                    self.rideRetire(entity, st)
            else:
                self.log('Please wait for admin to run!!!')
                time.sleep(self.delay)

    def waitForVehicles(self):
        vehicles =  [{'an': 2000, 'tid': -1, 'regn': 'reg00', 'dist': 0, 'hrs': 0.0, 'pid': 2, 'vtype': 0}, {'an': 2002, 'tid': -1, 'regn': 'reg02', 'dist': 0, 'hrs': 0.0, 'pid': 2, 'vtype': 1}, {'an': 2003, 'tid': -1, 'regn': 'reg03', 'dist': 0, 'hrs': 0.0, 'pid': 2, 'vtype': 2}, {'an': 2004, 'tid': -1, 'regn': 'reg04', 'dist': 0, 'hrs': 0.0, 'pid': 2, 'vtype': 3}, {'an': 2005, 'tid': -1, 'regn': 'reg05', 'dist': 0, 'hrs': 0.0, 'pid': 2, 'vtype': 0}, {'an': 2006, 'tid': -1, 'regn': 'reg06', 'dist': 0, 'hrs': 0.0, 'pid': 2, 'vtype': 1}, {'an': 2007, 'tid': -1, 'regn': 'reg07', 'dist': 0, 'hrs': 0.0, 'pid': 2, 'vtype': 2}, {'an': 2008, 'tid': -1, 'regn': 'reg08', 'dist': 0, 'hrs': 0.0, 'pid': 2, 'vtype': 3}, {'an': 2010, 'tid': -1, 'regn': 'reg10', 'dist': 0, 'hrs': 0.0, 'pid': 2, 'vtype': 1}, {'an': 2011, 'tid': -1, 'regn': 'reg11', 'dist': 0, 'hrs': 0.0, 'pid': 2, 'vtype': 2}, {'an': 2012, 'tid': -1, 'regn': 'reg12', 'dist': 0, 'hrs': 0.0, 'pid': 2, 'vtype': 3}, {'an': 2013, 'tid': -1, 'regn': 'reg13', 'dist': 0, 'hrs': 0.0, 'pid': 2, 'vtype': 0}, {'an': 2014, 'tid': -1, 'regn': 'reg14', 'dist': 0, 'hrs': 0.0, 'pid': 2, 'vtype': 1}, {'an': 2015, 'tid': -1, 'regn': 'reg15', 'dist': 0, 'hrs': 0.0, 'pid': 2, 'vtype': 2}, {'an': 2018, 'tid': -1, 'regn': 'reg18', 'dist': 0, 'hrs': 0.0, 'pid': 2, 'vtype': 1}, {'an': 2019, 'tid': -1, 'regn': 'reg19', 'dist': 0, 'hrs': 0.0, 'pid': 2, 'vtype': 2}, {'an': 2020, 'tid': -1, 'regn': 'reg20', 'dist': 0, 'hrs': 0.0, 'pid': 2, 'vtype': 3}]

        # {'vehicles': [{'an': 2000, 'tid': -1, 'regn': 'reg00', 'dist': 0, 'hrs': 0.0, 'pid': 2, 'vtype': 0},
        # {'an': 2002, 'tid': -1, 'regn': 'reg02', 'dist': 0, 'hrs': 0.0, 'pid': 2, 'vtype': 1},
        # {'an': 2003, 'tid': -1, 'regn': 'reg03', 'dist': 0, 'hrs': 0.0, 'pid': 2, 'vtype': 2},
        # {'an': 2004, 'tid': -1, 'regn': 'reg04', 'dist': 0, 'hrs': 0.0, 'pid': 2, 'vtype': 3},
        # {'an': 2005, 'tid': -1, 'regn': 'reg05', 'dist': 0, 'hrs': 0.0, 'pid': 2, 'vtype': 0},
        # {'an': 2006, 'tid': -1, 'regn': 'reg06', 'dist': 0, 'hrs': 0.0, 'pid': 2, 'vtype': 1},
        # {'an': 2007, 'tid': -1, 'regn': 'reg07', 'dist': 0, 'hrs': 0.0, 'pid': 2, 'vtype': 2},
        # {'an': 2008, 'tid': -1, 'regn': 'reg08', 'dist': 0, 'hrs': 0.0, 'pid': 2, 'vtype': 3},
        # {'an': 2010, 'tid': -1, 'regn': 'reg10', 'dist': 0, 'hrs': 0.0, 'pid': 2, 'vtype': 1},
        # {'an': 2011, 'tid': -1, 'regn': 'reg11', 'dist': 0, 'hrs': 0.0, 'pid': 2, 'vtype': 2},
        # {'an': 2012, 'tid': -1, 'regn': 'reg12', 'dist': 0, 'hrs': 0.0, 'pid': 2, 'vtype': 3},
        # {'an': 2013, 'tid': -1, 'regn': 'reg13', 'dist': 0, 'hrs': 0.0, 'pid': 2, 'vtype': 0},
        # {'an': 2014, 'tid': -1, 'regn': 'reg14', 'dist': 0, 'hrs': 0.0, 'pid': 2, 'vtype': 1},
        # {'an': 2015, 'tid': -1, 'regn': 'reg15', 'dist': 0, 'hrs': 0.0, 'pid': 2, 'vtype': 2},
        # {'an': 2018, 'tid': -1, 'regn': 'reg18', 'dist': 0, 'hrs': 0.0, 'pid': 2, 'vtype': 1},
        # {'an': 2019, 'tid': -1, 'regn': 'reg19', 'dist': 0, 'hrs': 0.0, 'pid': 2, 'vtype': 2},
        # {'an': 2020, 'tid': -1, 'regn': 'reg20', 'dist': 0, 'hrs': 0.0, 'pid': 2, 'vtype': 3}]}

        '''
        while True:
            ret = self.callAPI('auth-vehicle-get-avail')
            print(ret)
            if not self.logIfErr(ret):
                nVehicles = len(ret['vehicles'])
                if nVehicles > 0:
                    self.log('Vehicles found at hub: %d' % nVehicles)
                    self.bPollVehicle = False
                    vehicles = ret['vehicles']
                else:

                    self.log('No vehicles at hub - waiting...')
                    self.bPollVehicle = True
            time.sleep(self.delay)
            if not self.bPollVehicle:
                break
        '''
        return vehicles


    def getData(self, **kwargs):
        return self.callAPI('admin-data-get', {**kwargs})[0]

