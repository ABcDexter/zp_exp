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
        self.sTripState = ''


    # log a message if its different from the last logged message
    def log(self, sMsg):
        if sMsg != self.sLastMsg:
            t = datetime.datetime.now()
            print('[%s] [PID: %s] [TID: %s] [TS: %s] %s' %
                  (datetime.datetime.strftime(t, '%x %X'), self.iPID, self.sTID, self.sTripState, sMsg))
            self.sLastMsg = sMsg


    def writeJSON(self, sFile, dct):
        with open(sFile, 'w') as f:
            json.dump(dct, f)


    def doRequestRide(self):
        if prob(0.8):
            iDstPID = self.iPID
            while iDstPID == self.iPID:
                dctPlace = random.choice(self.arrPlaces)
                iDstPID = dctPlace['id']

            self.log('Requesting trip to %d: %s' % (iDstPID, dctPlace['pn']))

            ret = self.callAPI('user-ride-request', {'srcid': self.iPID, 'dstid': iDstPID, 'rtype': 0, 'vtype': 3, 'npas': 1, 'pmode': 0 })
            if not self.logIfErr(ret):
                self.log('Trip estimate: %s, ' % json.dumps(ret))
                self.iDstPID = iDstPID
                self.sTID = ret['tid']


    def handleActive(self, dct):
        self.sTID = dct['tid']
        self.bPollRide = True
        self.sTripState = dct['st']
        st = self.sTripState

        if st in ['FN', 'TR']:
            self.log('Made payment for trip: '
                     'Cost: %.2f, '
                     'Dist %.2f km' % (dct['price'], dct['dist']))
            self.writeJSON('money.%d' % self.sTID, {'payment': dct['price']})
        elif st in ['TO', 'CN', 'DN', 'PD', 'FL']:
            self.handleFinishedTrip()
        else:
            if not self.maybeCancelTrip('user', 0.00):
                if st == 'AS':
                    ret = self.callAPI('user-ride-get-driver')
                    if not self.logIfErr(ret):
                        ret['otp'] = dct['otp']
                        ret['van'] = dct['van']
                        sMsg = 'Trip confirmed: %s, ' % json.dumps(ret)

                        if prob(0.95):
                            self.writeJSON('otp.%d' % self.sTID, {'otp': dct['otp']})
                            self.log(sMsg + ': OTP shared with driver')
                elif st == 'ST':
                    self.showTripProgress()
                    self.maybeFailTrip('user', 0.01) # Try to fail the trip.

                elif st == 'RQ':
                    self.log('Waiting for driver')



    def handleTripStatus(self):
        ret = self.callAPI('user-ride-get-status')
        self.sTID = ret.get('tid', -1)
        if self.sTID == -1:
            self.waitForVehicles()
            self.doRequestRide()
        else:
            if ret['active']:  # 'RQ', 'AS', 'ST', 'FN', 'TR'
                self.handleActive(ret)
            else: #  TO, CN, DN, FL, PD
                self.handleFinishedTrip('user')


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
            self.handleTripStatus()
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