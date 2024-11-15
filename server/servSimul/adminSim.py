#!/bin/python3

import os
import sys

sys.path.insert(0, os.path.dirname(__file__))
from entity import *

'''
Script simulates a admins behaviour
Run multiple instances of this to simulate multiple
'''

class Admin(Entity):

    def __init__(self):
        super().__init__()

    # log a message if its different from the last logged message
    def log(self, sMsg):
        if sMsg != self.sLastMsg:
            t = datetime.datetime.now()
            print('[%s] %s' % (datetime.datetime.strftime(t, '%x %X'), sMsg))
            self.sLastMsg = sMsg


    def run(self, fDelay):

        self.log('Admin active')

        while True:
            dctRet = self.callAPI('admin-refresh')
            self.logIfErr(dctRet)
            if not self.logIfErr(dctRet) and prob(0.5):
                # update with 50% probability
                self.log('Fixing bookings')
                '''
                failedList = self.callAPI('admin-handle-failed-trip')
                if len(failedList):
                    self.log('Unlocking %d drivers' % len(failedList))
                    self.log('Recovering %d vehicles' % len(failedList))
                    self.log('Freeing %d users' % len(failedList))

                    for failed in failedList:
                        dctRetDriver = self.callAPI('auth-admin-entity-update', {'auth': failed['dauth'], 'mode': 'OF', 'tid': -1}, failed['dauth'])
                        if not self.logIfErr(dctRetDriver):
                            self.log('Unlocked driver with auth : %s' % failed['dauth'] )
                        time.sleep(fDelay)

                        dctRetVehicle = self.callAPI('admin-vehicle-update', {'van': failed['van'], 'tid': -1})
                        if not self.logIfErr(dctRetVehicle):
                            self.log('Recovered vehicle with an : %d' % failed['van'] )
                        time.sleep(fDelay)

                        dctRetUser = self.callAPI('auth-admin-entity-update', {'auth': failed['uauth'], 'tid': -1}, failed['uauth'])
                        if not self.logIfErr(dctRetUser):
                            self.log('Freed user with auth : %s' % failed['uauth'] )
                        time.sleep(fDelay)
                '''

            if not self.logIfErr(dctRet) and prob(0.99999):

                self.log('Syncing booking table from wordpress')

                synced = self.callAPI('auth-booking-sync', {'auth': '1048576'})
                if not self.logIfErr(synced):
                    self.log('Synced ' + str(synced))
                else:
                    self.log('FAILED to sync ' + str(synced))

                # self.log('Assigning Servitors to Bookings')
                '''
                resp = self.callAPI('admin-agent-assign')
                if not self.logIfErr(resp):
                    did = resp['did']
                    auth = resp['babua']
                    print(" values : ", did, "auth of agent closest ", auth)
                    response = self.callAPI('agent-delivery-accept', {'did': did}, auth)

                self.log('Checking from AS to RC')

                resp = self.callAPI('admin-agent-reached')
                if not self.logIfErr(resp):
                    did = resp['did']
                    auth = resp['babua']
                    print(" values : ", did, "auth of agent reached ", auth)
                    response = self.callAPI('agent-delivery-reached', {'did': did}, auth)

                self.log('Retiring Users with TO did')
                resp = self.callAPI('admin-retire-to-users')
                if not self.logIfErr(resp):
                    print("## DONE ##")
                '''
            pass

            # Sleep for specified time
            time.sleep(fDelay)

USAGE = \
'''
Usage adminSim.py sleep 
    sleep: optional number of seconds (float) to sleep between actions (default 3.0)
'''

def main():
    if len(sys.argv) < 2:
        print(USAGE)
        sys.exit(-1)

    delay = float(sys.argv[1])

    entity = Admin()
    entity.run(delay)


if __name__ == '__main__':
    main()
