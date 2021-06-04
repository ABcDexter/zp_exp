#!/bin/python3

import os
import sys

sys.path.insert(0, os.path.dirname(__file__))
from entity import *

'''
Script simulates  admin behaviour
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

        # update routes table
        # self.log('Updating routes')
        # dctRet = self.callAPI('admin-route-update', {'gmaps_key': 'AIzaSyCmGlXzWf-xk_6PjLtebyXr1pyQ8Lc3_RI'})
        #self.logIfErr(dctRet)
        self.log('Admin active')

        while True:
            dctRet = self.callAPI('admin-refresh')
            self.logIfErr(dctRet)
            if not self.logIfErr(dctRet) and prob(0.5):
            # Fix failed vehicles and super very lazily with 50% probability

                failedList = self.callAPI('admin-handle-failed-trip')

                if len(failedList):
                    self.log('Fixing trip failures')
                    self.log('Recovering %d vehicles' % len(failedList))
                    self.log('Freeing %d users' % len(failedList))

                    for failed in failedList:

                        dctRetVehicle = self.callAPI('admin-vehicle-update', {'van': failed['van'], 'tid': -1})
                        if not self.logIfErr(dctRetVehicle):
                            self.log('Recovered vehicle with an : %d' % failed['van'] )
                        time.sleep(fDelay)

                        dctRetUser = self.callAPI('auth-admin-entity-update', {'auth': failed['uauth'], 'tid': -1}, failed['uauth'])
                        if not self.logIfErr(dctRetUser):
                            self.log('Freed user with auth : %s' % failed['uauth'] )
                        time.sleep(fDelay)
                else:
                    self.log("No failed trips!")

            assigned = self.callAPI('admin-vehicle-assign')
            try:
                if not self.logIfErr(assigned) :
                    if 'tid' in assigned:
                        self.log('Assigning rental vehicles')
                        if 'vid' in assigned:
                            self.log("assigned vehicle %d to trip %d" % (assigned['vid'], assigned['tid']))
                        else:
                            self.log("Vehicle not Found for trip %d " % (assigned['tid']))
                    else:
                        self.log("No incoming rental request!")
            except KeyError:
                self.log("assigning failed! please have a look")
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
