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
        self.bChangeStatus = True
        self.fDelay = 1

    # log a message if its different from the last logged message
    def log(self, sMsg):
        if sMsg != self.sLastMsg:
            t = datetime.datetime.now()
            print('[%s] [PID: %s] [TID: %s]  %s' %
                  (datetime.datetime.strftime(t, '%x %X'), self.iPID, self.sTID,  sMsg))
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


    def handleStarting(self, sTripState, sTID):

        vehicles = self.waitForVehicles()

        if sTripState == 'RQ':

            bChoose = prob(0.999999)  # high probability of accepting
            self.log('Trip requested - %s' % ('accepting...' if bChoose else 'rejecting...'))
            if bChoose:
                vehicle = random.choice(vehicles)  # random vechile, and not the one
                params = {'tid': sTID, 'van': vehicle['an']}
                ret = self.callAPI('sup-rent-vehicle-assign', params)
                if not self.logIfErr(ret):
                    self.sTID = params['tid']
                    self.iDstPID = ret['dstname']
                    self.log('Assigned trip %s , expected drop PID %s for %s hrs' % (
                        json.dumps(params), self.iDstPID, ret['hrs']))
                # SENT TO AS state

        elif sTripState == 'AS':

            self.log('Waiting for user to give advance payment with OTP')

            if prob(0.0001): # only 0.01 % chance of cancelling a trip
                self.log('Canceling trip!')
                params = {'tid': sTID}
                ret = self.callAPI('sup-rent-cancel', params)
                self.logIfErr(ret)
            else:
                # enter the OTP
                otp = self.tryReadFileData('otp.%d' % sTID, 'otp')
                pay = self.tryReadFileData('money.%d' % sTID, 'payment')
                if pay is not None and otp is not None:
                    self.log('Received payment: %.2f' % pay)
                    os.remove('money.%d' % sTID)
                    self.log('Received OTP: %d, starting trip' % otp)
                    ret = self.callAPI('sup-rent-start', {'otp': otp, 'tid':sTID})
                    if not self.logIfErr(ret):
                        os.remove('otp.%d' % sTID)
                else:
                    self.log("No payment/OTP given")
        else:
            self.log('Trip in unexpected state: %s , for : %s' % (sTripState, 'superVisor'))

    def handleEnding(self, sTripState, sTID):
        params = {'tid': sTID}
        ret = self.callAPI('sup-rent-get-status', params)

        if 'active' in ret and ret['active']=='true' :
            sNewTripState = ret['st']

            if sNewTripState == ['FN', 'TR']:

                print('values : ', ret)
                if int(ret['price']) > 0:

                    self.log('Checked vehicle. Waiting for payment: Cost: %.2f...' % (ret['price']))

                    pay = self.tryReadFileData('money.%d' % self.sTID, 'payment')
                    if pay is not None:
                        self.log('Received payment: %.2f, rating user' % pay)
                        ret = self.callAPI('sup-payment-confirm', params)
                        if not self.logIfErr(ret):
                            os.remove('money.%d' % self.sTID)
                            endingRet = self.callAPI('sup-rent-end', params)
                            self.log('Ending trip. %s ' % (sTID))

    def handleHub(self):
        """
        look for RQ rental trips,
        this is because the supervisor is sitting at the particular hub
        and is always waiting for requests
        """
        ret = self.callAPI('sup-rent-check')
        if not self.logIfErr(ret):
            if 'tid' in ret:
                sTID = ret['tid']
                sTripState = ret['st']
                if sTripState in ['RQ', 'AS']:
                    self.handleStarting(sTripState, sTID)
                elif sTripState in ['TR', 'FN']:
                    self.handleEnding(sTripState, sTID)
                else:
                    self.log('Trip in unexpected state: %s , for : %s' % (sTripState, 'superVisor'))
            else :
                self.log('No rental requests at pid : %s , for supervisor number: %s' % (self.iPID, self.idx))

    def handleSuperStatus(self):
        '''
        unlike Driver, super is 24x7 AV
        probably a bot ...
        '''
        return True  # Anna, chaubees ghante chaukanna


    def run(self, idxSupervisor, fDelay):

        # Get list of Supers and choose the idxSuper'th one
        arrSupes = self.callAPI('admin-data-get', {'table': 'Supervisor'})
        dctSuper = arrSupes[idxSupervisor]
        #print('######################; ', dctSuper)
        self.sAuth = dctSuper['auth']
        self.log('Supervisor an: %d' % dctSuper['an'])
        self.idx = idxSupervisor
        # Assume Super is in some valid place get it
        pid = dctSuper['pid']
        dctPlace = self.callAPI('admin-data-get', {'table': 'Place', 'pk': pid})[0]
        self.log('Supvervisor located at hub with pid %d: %s' % (pid, dctPlace['pn']))
        self.iPID = pid

        bOnline = True # assuming super is always online

        # code follows the state machine in the flowchart
        while True:

            if self.bChangeStatus:
                bOnline = self.handleSuperStatus()

            if bOnline:
                self.handleHub()

            time.sleep(fDelay)


USAGE = \
'''
Usage supervisorSim.py <n> [sleep] 
    n : index of the supervisor to simulate (nth record in DB)
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
#TODO need to integrate the simpy module
