#!/bin/python3

import os
import sys

from entity import FINISHED_STATUS_MSGS

sys.path.insert(0, os.path.dirname(__file__))
from entity import *

'''
Script simulates a agents behaviour
Run multiple instances of this to simulate multiple
'''

class Agent(Entity):

    def __init__(self):
        super().__init__()
        self.sState = ''
        self.sOTP = ''
        self.sDeliveryState = None
        self.bPollVehicle = False
        self.bChangeStatus = True
        self.fDelay = 1

    # log a message if its different from the last logged message
    def log(self, sMsg):
        if sMsg != self.sLastMsg:
            t = datetime.datetime.now()
            print('[%s] [PID: %s] [DID: %s] [DS: %s] [ST: %s]  %s' %
                  (datetime.datetime.strftime(t, '%x %X'), self.iPID, self.sDID, self.sDeliveryState, self.sState or '--', sMsg))
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
        self.sDID = ret['did']
        self.sDeliveryState = ret['st']
        st = self.sDeliveryState

        if st in ['FN', 'TR']:
            t = int(ret['time']) / 60
            d = int(ret['dist']) / 1000
            self.log('Waiting for payment: Cost: %.2f, Dist %.2f km' % (ret['price'], d))

            pay = self.tryReadFileData('money.%d' % self.sDID, 'payment')
            if pay is not None:
                self.log('Received payment: %.2f, rating user' % pay)
                ret = self.callAPI('agent-payment-confirm')
                if not self.logIfErr(ret):
                    os.remove('money.%d' % self.sDID)

        if st == 'AS':
            self.log('Waiting for user to arrive')

            if prob(0.01): # only 1 % chance of cancelling a delivery
                self.log('Canceling delivery!')
                ret = self.callAPI('agent-delivery-cancel')
                self.logIfErr(ret)
            else:
                otp = self.tryReadFileData('otp.%d' % self.sDID, 'otp')
                if otp is not None:
                    self.log('Received OTP: %d, starting delivery' % otp)
                    ret = self.callAPI('agent-delivery-start', {'otp': otp})
                    if not self.logIfErr(ret):
                        os.remove('otp.%d' % self.sDID)

        if st == 'ST':
            self.sDID = ret['did']

            pct = self.showTripProgress()
            self.callAPI('admin-progress-advance', {'did': self.sDID, 'pct' : 10})

            if pct == 100:
                self.log('Trip completed - ending')
                ret = self.callAPI('agent-delivery-end')
                self.logIfErr(ret)

            if prob(0.01):
                self.log('Failing delivery!')
                ret = self.callAPI('auth-delivery-fail')
                self.logIfErr(ret)


    def handleInactive(self):
        if self.sDID != -1: # TO, CN, DN, FL, PD
            self.handleFinishedDelivery('agent')
            self.bChangeStatus = True
        else: #RQ state
            print("here for a request... ")
            ret = self.callAPI('agent-delivery-check')
            print(ret)
            if not self.logIfErr(ret):
                vehicles = self.waitForVehicles()
                if 'did' in ret:
                    bChoose = prob(0.9)
                    self.log('Available available - %s' % ('accepting...' if bChoose else 'rejecting...') )
                    if bChoose:
                        vehicle = random.choice(vehicles)
                        params = {'did': ret['did'], 'van': vehicle['an'] }
                        ret = self.callAPI('agent-delivery-accept', params)
                        if not self.logIfErr(ret):
                            self.sDID = params['did']
                            self.iDstPID = ret['dstpin']
                            self.log('Accepted delivery %s to PID %d' % (json.dumps(params), self.iDstPID))
                            self.bChangeStatus = False


    def handleTripStatus(self):
        ret = self.callAPI('agent-delivery-get-status')
        print(ret)
        if 'active' in ret and ret['active']:
            self.handleActive(ret)
        else:
            # TO, CN, DN, RQ, FL, PD
            self.handleInactive()


    def handleAgentStatus(self):
        ret = self.callAPI('agent-get-mode')
        self.sState = ret['st']

        newState = None
        if prob(0.99):
            if self.sState == 'OF':
                newState = 'AV'
        else:
            if self.sState == 'AV':
                newState = 'OF'

        if newState is not None:
            ret = self.callAPI('agent-set-mode', {'st': newState})
            sSuccess = 'succeeded' if ret['st'] == newState else 'failed'
            self.sState = ret['st']
            self.log('Switching to state %s - %s' % (newState, sSuccess))

        return self.sState != 'OF'


    def run(self, idxAgent, fDelay):

        # Get list of agents and choose the idxAgent'th one
        arrAgents = self.callAPI('admin-data-get', {'table': 'Agent'})
        dctAgent = arrAgents[idxAgent]

        self.sAuth = dctAgent['auth']
        self.sState = dctAgent['mode']
        self.log('Agent an: %d' % dctAgent['an'])

        # Assume agent is in some valid place get it
        pid = dctAgent['pid']
        dctPlace = self.callAPI('admin-data-get', {'table': 'Place', 'pk': pid})[0]
        self.log('Agent located at pid %d: %s' % (pid, dctPlace['pn']))
        self.iPID = pid

        bOnline = False

        # code follows the state machine in the flowchart
        while True:

            # set of/av
            if self.bChangeStatus:
                bOnline = self.handleAgentStatus()

            if bOnline:
                self.handleTripStatus()

            time.sleep(fDelay)


USAGE = \
'''
Usage agentSim.py <n> [sleep] 
    n : index of the agent to simulate (nth record in DB)
    sleep: optional number of seconds (float) to sleep between actions (default 3.0)
'''

def main():
    if len(sys.argv) < 2:
        print(USAGE)
        sys.exit(-1)

    idxAgent = int(sys.argv[1])
    fDelay = 3 if len(sys.argv) < 3 else float(sys.argv[2])

    entity = Agent()
    entity.run(idxAgent, fDelay)

if __name__ == "__main__":
    main()