#!/bin/python3

import os
import sys

sys.path.insert(0, os.path.dirname(__file__))
from entity import *

'''
Script simulates a drivers behaviour
Run multiple instances of this to simulate multiple
'''

class User(Entity):

    def __init__(self):
        super().__init__()
        self.arrPlaces = []
        self.sDeliveryState = ''


    # log a message if its different from the last logged message
    def log(self, sMsg):
        if sMsg != self.sLastMsg:
            t = datetime.datetime.now()
            print('[%s] [PID: %s] [DID: %s] [TS: %s] %s' %
                  (datetime.datetime.strftime(t, '%x %X'), self.iPID, self.sDID, self.sDeliveryState, sMsg))
            self.sLastMsg = sMsg


    def writeJSON(self, sFile, dct):
        with open(sFile, 'w') as f:
            json.dump(dct, f)


    def doRequestDelivery(self):
        if prob(0.8):
            iDstPID = self.iPID
            while iDstPID == self.iPID:
                dctPlace = random.choice(self.arrPlaces)
                iDstPID = dctPlace['id']

            self.log('Requesting delivery to %d: %s' % (iDstPID, dctPlace['pn']))

            ret = self.callAPI('user-delivery-request', {'srcpin': self.iPID, 'dstpin': iDstPID, \
                "srclat": "29.317953","srclng": "79.587319","dstlat": "29.339276","dstlng": "79.58613",  'pmode': 1 ,
                                                   'itype':1,      'idim': 1})

            
            if not self.logIfErr(ret):
                self.log('Delivery estimate: %s, ' % json.dumps(ret))
                self.iDstPID = iDstPID
                self.sDID = ret['did']


    def handleActive(self, dct):
        self.sDID = dct['did']
        self.bPollDelivery= True
        self.sDeliveryState = dct['st']
        st = self.sDeliveryState

        if st in  ['TO', 'CN', 'DN', 'PD', 'FL']:
            self.log('Made payment for Delivery: '
                     'Cost: %.2f, '
                     'Dist %.2f km' % (dct['price'], dct['dist']))
            self.writeJSON('money.%d' % self.sDID, {'payment': dct['price']})
        elif st in ['FN']:
            self.handleFinishedDelivery()
        else:
            if not self.maybeCancelDelivery('user', 0.00):
                if st == 'AS':
                    ret = self.callAPI('user-ride-get-driver') #TODO something else
                    if not self.logIfErr(ret):
                        ret['otp'] = dct['otp']
                        ret['van'] = dct['van']
                        sMsg = 'Delivery confirmed: %s, ' % json.dumps(ret)

                        if prob(0.95):
                            self.writeJSON('otp.%d' % self.sDID, {'otp': dct['otp']})
                            self.log(sMsg + ': OTP shared with driver')
                elif st == 'ST':
                    self.showDeliveryProgress()

                elif st == 'RQ':
                    self.log('Waiting for driver')



    def handleDeliveryStatus(self):
        ret = self.callAPI('user-delivery-get-status')
        self.sDID = ret.get('did', -1)
        if self.sDID == -1: # no current delivery
            if prob(0.9):
                self.doRequestDelivery()
        else:
            # # thisd mean that the User has some delivery request pending in
            if ret['active']:  # 'RQ', 'AS',
                self.handleActive(ret)
            else: #  TO, CN, DN, FL, PD  'ST', 'FN',
                self.handleFinishedDelivery('user')


    def run(self, idxUser, fDelay, iPID=None):

        # Get list of users and choose the idxUser'th one
        arrUsers = self.callAPI('admin-data-get', {'table': 'User'})
        #print("users : ", arrUsers)
        dctUser = arrUsers[idxUser]
        self.delay = fDelay
        self.sAuth = dctUser['auth']

        # get list of places
        self.arrPlaces = self.callAPI('auth-place-get')['hublist']

        # if PID given , use that
        if iPID is None:
            self.iPID = dctUser['pid']
        else:
            self.iPID = iPID
            ret=self.callAPI('auth-admin-entity-update', {'pid': self.iPID})
            print(ret)

        # Assume user is in some valid place get it
        dctPlace = self.callAPI('admin-data-get', {'table': 'Place', 'pk': self.iPID})[0]
        self.log('User located at pid %d: %s' % (self.iPID, dctPlace['pn']))

        while True:
            self.handleDeliveryStatus()
            time.sleep(self.delay)


USAGE = \
'''
Usage userSim.py <n> [sleep] 
    n : index of the user to simulate (nth record in DB)
    sleep: optional number of seconds (float) to sleep between actions (default 3.0)
'''

def main():
    if len(sys.argv) < 2:
        print(USAGE)
        sys.exit(-1)

    idx = int(sys.argv[1])
    delay = 3 if len(sys.argv) < 3 else float(sys.argv[2])
    pid = None if len(sys.argv) < 4 else int(sys.argv[3]) #reset the pid for default value

    entity = User()
    entity.run(idx, delay, pid)


if __name__ == "__main__":
    main()