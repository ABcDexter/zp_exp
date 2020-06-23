#!/bin/python3

import os
import sys

from entity import FINISHED_STATUS_MSGS

sys.path.insert(0, os.path.dirname(__file__))
from entity import *

'''
Script simulates a superVisor behaviour
Run multiple instances of this to simulate multiple
'''

class Supervisor(Entity):

    def __init__(self):
        super().__init__()
        self.sOTP = ''
        self.sTripState = None
        self.bPollVehicle = False
        self.bChangeStatus = True
        self.fDelay = 1

    # log a message if its different from the last logged message
    def log(self, sMsg):
        if sMsg != self.sLastMsg:
            t = datetime.datetime.now()
            print('[%s] [PID: %s] [TID: %s] [TS: %s]  %s' %
                  (datetime.datetime.strftime(t, '%x %X'), self.iPID, self.sTID, self.sTripState, sMsg))
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
            print ('values : ', ret)
            if int(ret['price']) > 0 :

                self.log('Waiting for payment: Cost: %.2f...' % (ret['price']))

                pay = self.tryReadFileData('money.%d' % self.sTID, 'payment')
                if pay is not None:
                    self.log('Received payment: %.2f, rating user' % pay)
                    ret = self.callAPI('driver-payment-confirm')
                    if not self.logIfErr(ret):
                        os.remove('money.%d' % self.sTID)

        if st == 'AS':
            self.log('Waiting for user to give advance payment with OTP')

            if prob(0.0001): # only 0.01 % chance of cancelling a trip
                self.log('Canceling trip!')
                ret = self.callAPI('sup-rent-cancel')
                self.logIfErr(ret)
            else:
                if prob(0.000000001):
                    self.log('Failing trip!')
                    ret = self.callAPI('auth-trip-fail')
                    self.logIfErr(ret)
                else:
                    otp = self.tryReadFileData('otp.%d' % self.sTID, 'otp')
                    if otp is not None:
                        self.log('Received OTP: %d, starting trip' % otp)
                        ret = self.callAPI('sup-rent-start', {'otp': otp})
                        if not self.logIfErr(ret):
                            os.remove('otp.%d' % self.sTID)

        if st == 'ST':
            self.sTID = ret['tid']

            pct = self.showTripProgress()
            self.callAPI('admin-progress-advance', {'tid': self.sTID, 'pct' : 10})

            if pct == 100:
                self.log('Trip completed - ending')
                ret = self.callAPI('sup-rent-end')
                self.logIfErr(ret)



    def handleInactive(self):
        if self.sTID != -1: # TO, CN, DN, FL, PD
            self.handleFinishedTrip('sup')
            self.bChangeStatus = True
        else: #RQ state
            ret = self.callAPI('sup-rent-check')
            if not self.logIfErr(ret):
                vehicles = self.waitForVehicles()
                if 'tid' in ret:
                    bChoose = prob(0.999999) # high probability of accepting
                    self.log('Trip requested - %s' % ('accepting...' if bChoose else 'rejecting...') )
                    if bChoose:
                        vehicle = random.choice(vehicles) #random vechile, and not the one
                        params = {'tid': ret['tid'], 'van': vehicle['an'] }
                        ret = self.callAPI('sup-rent-accept', params)
                        if not self.logIfErr(ret):
                            self.sTID = params['tid']
                            self.iDstPID = ret['dstid']
                            self.log('Accepted trip %s to PID %d for %d hrs' % (json.dumps(params), self.iDstPID, ret['hrs']))
                            self.bChangeStatus = False


    def handleTripStatus(self):
        ret = self.callAPI('sup-rent-get-status')
        if 'active' in ret and ret['active']:
            self.handleActive(ret)
        else:
            # TO, CN, DN, RQ, FL, PD
            self.handleInactive()


    def handlerSuperStatus(self):
        '''
        unlike Driver, super is 24x7 AV
        '''
        return True # Anna, chaubees ghante chaukanna


    def run(self, idxSupervisor, fDelay):

        # Get list of drivers and choose the idxDriver'th one
        arrSupes = self.callAPI('admin-data-get', {'table': 'Supervisor'})
        dctSuper = arrSupes[idxSupervisor]
        #print('######################; ', dctSuper)
        self.sAuth = dctSuper['auth']
        self.log('Supervisor an: %d' % dctSuper['an'])

        # Assume Super is in some valid place get it
        pid = dctSuper['pid']
        dctPlace = self.callAPI('admin-data-get', {'table': 'Place', 'pk': pid})[0]
        self.log('Supvervisor located at hub with pid %d: %s' % (pid, dctPlace['pn']))
        self.iPID = pid

        bOnline = True # assuming super is always online

        # code follows the state machine in the flowchart
        while True:

            # set of/av
            if self.bChangeStatus:
                bOnline = self.handlerSuperStatus()

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

    idxSupervisor = int(sys.argv[1])
    fDelay = 3 if len(sys.argv) < 3 else float(sys.argv[2])

    entity = Supervisor()
    entity.run(idxSupervisor, fDelay)

if __name__ == "__main__":
    main()
