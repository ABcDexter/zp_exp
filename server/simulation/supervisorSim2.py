#!/bin/python3

import os
import sys

from entity import FINISHED_STATUS_MSGS

sys.path.insert(0, os.path.dirname(__file__))
from entity import *

'''
Script simulates a drivers behaviour
Run multiple instances of this to simulate multiple
'''

class Driver(Entity):

    def __init__(self):
        super().__init__()
        self.sState = ''
        self.sOTP = ''
        self.sTripState = None
        self.bPollVehicle = False
        self.bChangeStatus = True
        self.fDelay = 1

    # log a message if its different from the last logged message
    def log(self, sMsg):
        if sMsg != self.sLastMsg:
            t = datetime.datetime.now()
            print('[%s] [PID: %s] [TID: %s] [TS: %s] [ST: %s]  %s' %
                  (datetime.datetime.strftime(t, '%x %X'), self.iPID, self.sTID, self.sTripState, self.sState or '--', sMsg))
            self.sLastMsg = sMsg


    def tryReadFileData(self, sFileName, sKey):
        if os.path.exists(sFileName):
            try:
                with open(sFileName) as f:
                    dct = json.load(f)
                    return int(float(dct[sKey]))  #just to ensure it reads the int value
            except Exception as e:
                print(repr(e))
        return None


    def handleActive(self, ret):
        self.sTID = ret['tid']
        self.sTripState = ret['st']
        st = self.sTripState

        if st in ['FN', 'TR']:
            t = int(ret['time']) / 60
            d = int(ret['dist']) / 1000
            self.log('Waiting for payment: Cost: %.2f, Dist %.2f km' % (ret['price'], d))

            pay = self.tryReadFileData('money.%d' % self.sTID, 'payment')
            if pay is not None:
                self.log('Received payment: %.2f, rating user' % pay)
                ret = self.callAPI('driver-payment-confirm')
                if not self.logIfErr(ret):
                    os.remove('money.%d' % self.sTID)

        if st == 'AS':
            self.log('Waiting for user to wear helmet')

            if prob(0.0000001): # only 1 % chance of cancelling a trip
                self.log('Canceling trip!')
                ret = self.callAPI('driver-ride-cancel')
                self.logIfErr(ret)
            else:
                otp = self.tryReadFileData('otp.%d' % self.sTID, 'otp')
                if otp is not None:
                    self.log('Received OTP: %d, starting trip' % otp)
                    ret = self.callAPI('driver-ride-start', {'otp': otp})
                    if not self.logIfErr(ret):
                        os.remove('otp.%d' % self.sTID)

        if st == 'ST':
            self.sTID = ret['tid']

            pct = self.showTripProgress()
            self.callAPI('admin-progress-advance', {'tid': self.sTID, 'pct' : 10})

            if pct == 100:
                self.log('Trip completed - ending')
                ret = self.callAPI('driver-ride-end')
                self.logIfErr(ret)

            if prob(0.01):
                self.log('Failing trip!')
                ret = self.callAPI('auth-trip-fail')
                self.logIfErr(ret)




    def handleInactive(self):
        if self.sTID != -1: # TO, CN, DN, FL, PD
            self.handleFinishedTrip('driver')
            self.bChangeStatus = True
        else: #RQ state
            ret = self.callAPI('driver-ride-check')
            if not self.logIfErr(ret):
                vehicles = self.waitForVehicles()
                if 'tid' in ret:
                    bChoose = prob(0.9)
                    self.log('Trip available - %s' % ('accepting...' if bChoose else 'rejecting...') )
                    if bChoose:
                        vehicle = random.choice(vehicles)
                        params = {'tid': ret['tid'], 'van': vehicle['an'] }
                        ret = self.callAPI('driver-ride-accept', params)
                        if not self.logIfErr(ret):
                            self.sTID = params['tid']
                            self.iDstPID = ret['dstid']
                            self.log('Accepted trip %s to PID %d' % (json.dumps(params), self.iDstPID))
                            self.bChangeStatus = False


    def handleTripStatus(self):
        ret = self.callAPI('driver-ride-get-status')
        if 'active' in ret and ret['active']:
            self.handleActive(ret)
        else:
            # TO, CN, DN, RQ, FL, PD
            self.handleInactive()


    def handleDriverStatus(self):
        ret = self.callAPI('driver-get-mode')
        self.sState = ret['st']

        newState = None
        if prob(0.99):
            if self.sState == 'OF':
                newState = 'AV'
        else:
            if self.sState == 'AV':
                newState = 'OF'

        if newState is not None:
            ret = self.callAPI('driver-set-mode', {'st': newState})
            sSuccess = 'succeeded' if ret['st'] == newState else 'failed'
            self.sState = ret['st']
            self.log('Switching to state %s - %s' % (newState, sSuccess))

        return self.sState != 'OF'


    def run(self, idxDriver, fDelay):

        # Get list of drivers and choose the idxDriver'th one
        arrDrivers = self.callAPI('admin-data-get', {'table': 'Driver'})
        dctDriver = arrDrivers[idxDriver]

        self.sAuth = dctDriver['auth']
        self.sState = dctDriver['mode']
        self.log('Driver an: %d' % dctDriver['an'])

        # Assume driver is in some valid place get it
        pid = dctDriver['pid']
        dctPlace = self.callAPI('admin-data-get', {'table': 'Place', 'pk': pid})[0]
        self.log('Driver located at pid %d: %s' % (pid, dctPlace['pn']))
        self.iPID = pid

        bOnline = False

        # code follows the state machine in the flowchart
        while True:

            # set of/av
            if self.bChangeStatus:
                bOnline = self.handleDriverStatus()

            if bOnline:
                self.handleTripStatus()

            time.sleep(fDelay)


USAGE = \
'''
Usage driverSim.py <n> [sleep] 
    n : index of the driver to simulate (nth record in DB)
    sleep: optional number of seconds (float) to sleep between actions (default 3.0)
'''

def main():
    if len(sys.argv) < 2:
        print(USAGE)
        sys.exit(-1)

    idxDriver = int(sys.argv[1])
    fDelay = 3 if len(sys.argv) < 3 else float(sys.argv[2])

    entity = Driver()
    entity.run(idxDriver, fDelay)

if __name__ == "__main__":
    main()
