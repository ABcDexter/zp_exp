#!/bin/python3

import os
import sys

sys.path.insert(0, os.path.dirname(__file__))
from entity import *

'''
Script simulates a users behaviour
Run multiple instances of this to simulate multiple
'''

class User(Entity):

    def __init__(self):
        super().__init__()
        self.arrPlaces = []
        self.sTripState = ''
        self.lstHrs = [1, 2, 4, 6, 8, 10]
        self.iRentHr = 1 # default 1 hour


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


    def doRequestRent(self):
        if prob(0.8):
            iDstPID = self.iPID
            iHrs= self.iHrs
            while iDstPID == self.iPID:
                dctPlace = random.choice(self.arrPlaces)
                iDstPID = dctPlace['id']

            self.log('Requesting trip rental for %d hours, expected drop to %d: %s' % (iHrs, iDstPID, dctPlace['pn']))

            ret = self.callAPI('user-trip-request', {'srcid': self.iPID, 'dstid': iDstPID, 'rtype': 1, 'vtype': 3, 'hrs': iHrs, 'pmode': 0 })
            if not self.logIfErr(ret):
                self.log('Trip estimate: %s, ' % json.dumps(ret))
                self.iDstPID = iDstPID
                self.sTID = ret['tid']

    def doExtendtrip(self, p):
        if prob(p):
            hrs = random.choice(self.lstHrs)
            self.iRentHr = hrs
            self.log('Extending trip!... for %d hours ' % (self.iRentHr))
            self.callAPI('user-rental-update', {'hrs': self.iRentHr})
            return hrs
        return 0

    def handleActive(self, dct):
        self.sTID = dct['tid']
        self.bPollRide = True
        self.sTripState = dct['st']
        st = self.sTripState

        if st in ['FN', 'TR']:
            self.log('Balance payment for trip: '
                     'Cost: %.2f, ' % (dct['price'])) #, dct['dist']))
            self.writeJSON('money.%d' % self.sTID, {'payment': dct['price']})
        elif st in ['TO', 'CN', 'DN', 'PD', 'FL']:
            self.handleFinishedTrip()
        else:
            if not self.maybeCancelTrip('user', 0.001):
                if st == 'AS':
                    ret = self.callAPI('user-rent-get-vehicle')
                    if not self.logIfErr(ret):
                        ret['otp'] = dct['otp']
                        ret['vno'] = dct['vno']
                        sMsg = 'Trip confirmed: %s, ' % json.dumps(ret)

                        if prob(0.95):
                            self.writeJSON('otp.%d' % self.sTID, {'otp': dct['otp']})
                            self.log('Initial payment for trip: '
                                     'Cost: %.2f, ' % (ret['price']))
                            self.writeJSON('money.%d' % self.sTID, {'payment': ret['price']})
                            self.log(sMsg + ': OTP shared with Supervisor')
                elif st == 'ST':
                    self.sTID = dct['tid']

                    pct = self.showTripProgress()
                    self.callAPI('admin-progress-advance', {'tid': self.sTID, 'pct': 1}) # number of hours

                    if pct == self.iRentHr:
                        self.log('Trip completed - ending...')
                        self.callAPI('user-rent-end')
                        self.writeJSON('tid.%d' % self.sTID, {'tid': dct['tid']})
                        # TODO how do I physically end the trip.... :hmm think

                    else:
                        extend = self.doExtendtrip(0.01)  # 1 % probability
                        if extend > 0:
                            self.iRentHr += extend

                        self.maybeFailTrip('user', 0.01)  # Try to fail the trip.


                elif st == 'RQ':
                    self.log('Waiting for granting Vehicle...')



    def handleTripStatus(self):
        ret = self.callAPI('user-trip-get-status')
        self.sTID = ret.get('tid', -1)
        if self.sTID == -1:
            self.waitForVehicles()
            self.doRequestRent()
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
