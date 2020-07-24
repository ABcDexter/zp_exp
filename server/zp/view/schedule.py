import os
import urllib.request
import json
from datetime import date, datetime
from apscheduler.schedulers.blocking import BlockingScheduler
from apscheduler.schedulers.background import BackgroundScheduler
from apscheduler.schedulers.asyncio import AsyncIOScheduler
from apscheduler.schedulers.asyncio import BaseScheduler
import datetime
from datetime import datetime, timedelta
from os import truncate
import time
from django.db import transaction
from django.utils import timezone
from django.views.decorators.csrf import csrf_exempt

from url_magic import makeView
from ..models import Place, Trip, Progress, Supervisor
from ..models import User, Vehicle
from ..utils import ZPException, HttpJSONResponse
from ..utils import getOTP
from ..utils import getTripPrice, getRentPrice
from ..utils import handleException, extractParams, checkAuth, checkTripStatus, retireEntity

###########################


# sched.add_job(lambda: sched.print_jobs(), 'interval', seconds=5)
# sched = BackgroundScheduler(daemon=True)
# sched.add_job(sensor, 'cron', minute='*')
# sched.start()
count = 0
#SERVER_URL = os.environ.get('ZP_URL', 'http://127.0.0.1:9999/')  # localhost
sched = BackgroundScheduler()  # daemon=True)
sched.start()

SERVER_URL = os.environ.get('ZP_URL', 'https://api.villageapps.in:8090/')  # server

###########################
# Helper functions for scheduling


def sensor():
    """
    a test function
    """
    global count
    # sched.print_jobs()
    print('Count: ', count)
    count += 1


def schedule():
    '''
    creates a non Blocking scheduler
    Returns:

    '''
    # sched = BlockingScheduler()  # daemon = True
    sched = BackgroundScheduler()  # daemon=True)
    # return sched


def callAPI(sAPI, dct={}, auth=None):
    '''
    need to unit tests these:
    # The job will be executed on July 23rd, 2020
    # sched.add_job(sensor, 'date', run_date=date(2020, 7, 23), args=['text'])
    # The job will be executed on uly 23rd, 2020 at 22:23:00


    sched.add_job(callAPI, 'date', run_date=datetime(2020, 7, 23, 22, 28, 00), args=['auth-place-get', {}, auth])
    sched.start()

    Args:
        sAPI:
        dct:
        auth:

    Returns:

    '''
    print(sAPI, dct, auth)
    if sAPI.startswith('admin'):
        dct['auth'] = auth
    else:
        dct['auth'] = auth
        # print(sAPI.startswith('auth-admin'))
        if sAPI.startswith('auth-admin'):  # needed for authAdminEntityUpdate
            dct['adminAuth'] = auth
    # print("### new  :::", sAPI, dct)
    sUrl = SERVER_URL + sAPI
    dctHdrs = {'Content-Type': 'application/json'}
    jsonData = json.dumps(dct).encode()
    req = urllib.request.Request(sUrl, headers=dctHdrs, data=jsonData)
    jsonResp = urllib.request.urlopen(req, timeout=30).read()
    ret = json.loads(jsonResp)
    print(ret)
    return ret


@makeView()
@csrf_exempt
@handleException(KeyError, 'Invalid parameters', 501)
@extractParams
@transaction.atomic
@checkAuth()
def userRentSchedule(dct, user):
    '''
    User calls this to schedule a rental

    HTTP args:
        auth
     Rent :
        srcid - id of the selected start place
        dstid - id of the selected destination
        rtype - rent or ride
        vtype - vehicle type
        hrs   - number of hours
        pmode - 1 from the user

        5 more things
        date, month, year,
        hour, minute.
    '''
    print("Rental Request param : ", dct)

    # Even though we can use IDs directly, look them up in the DB to prevent bogus IDs
    placeSrc = Place.objects.filter(id=dct['srcid'])[0]
    placeDst = Place.objects.filter(id=dct['dstid'])[0]

    year = int(dct['year'])
    month = int(dct['month'])
    date = int(dct['date'])

    hour = int(dct['hour'])
    minute = int(dct['min'])

    if minute - 30 < 0:
        dinaank = datetime(year, month, date, hour - 1, (minute - 30) % 60, 00)
    else:
        dinaank = datetime(year, month, date, hour, minute - 30, 00)
    dinaank = datetime(year, month, date, hour - 1, minute, 30)
    print(dinaank)
    global sched
    sched.pause()
    sched.add_job(callAPI, 'date', run_date=dinaank,
                  args=['user-rent-request', {'srcid': dct['srcid'], 'dstid': dct['dstid'], 'rtype': '1',
                                              'vtype': dct['vtype'], 'hrs': dct['hrs'], 'pmode': dct['pmode']},
                        user.auth])
    print(sched)
    print(sched.get_jobs())  # _jobstores.ne #job.next_run_time)
    sched.resume()
    time.sleep(10)
    ret = {}
    # ret = getRentPrice(dct['hrs'])
    # ret['tid'] = trip.id

    return HttpJSONResponse(ret)

