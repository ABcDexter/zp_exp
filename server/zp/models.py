from django.db import models
from django.conf import settings

class User(models.Model):
    '''
    an(int):    Aadhaar number
    pn(str):    Phone number
    auth(str):  Client auth token
    pid(int):   Index of current place - see Place table
    tid(int):   Index of current trip - see Trip table
    name(str):
    gdr(str):   Gender
    age(int):
    dl(str):    Driver license in case of rent a ride
    hs (str):   Home state of the User
    '''
    an   = models.BigIntegerField(primary_key=True)
    pn   = models.CharField(max_length=32, db_index=True)
    auth = models.CharField(max_length=16, db_index=True)

    pid  = models.IntegerField(null=True, db_index=True)
    tid  = models.IntegerField(default=-1, db_index=True)

    name = models.CharField(null=True, max_length=64, db_index=True)
    gdr  = models.CharField(null=True, max_length=16, db_index=True)
    age  = models.SmallIntegerField(null=True, db_index=True)
    dl   = models.CharField(null=True, max_length=20)
    hs   = models.CharField(null=True, max_length=50)

    class Meta:
        db_table = 'user'
        managed = True


class Driver(models.Model):
    '''
    an(int):    Aadhaar number
    pn(str):    Phone number
    auth(str):  Client auth token
    dl(str):    Driving licence no.
    name(str):  Real name
    gdr(str):   Gender
    age(int)
    mode(str):   Driver mode registering(RG), available(AV), booked(BK), offline(OF), locked(LK)
    pid(int):   Index of current place - see Place table
    tid(int):   Index of current trip - see Trip table
    hs(str):    Home state of the Driver
    '''
    MODES = [
        ('RG', 'registering'),  # driver is under registration process
        ('AV', 'available'),  # driver is online, waiting for bookings
        ('BK', 'booked'),     # driver booked by client/user
        ('OF', 'offline'),    # driver is offline
        ('LK', 'locked'),      # driver is locked
    ]

    an   = models.BigIntegerField(primary_key=True)
    pn   = models.CharField(max_length=32, db_index=True)
    auth = models.CharField(max_length=16, db_index=True)
    mode = models.CharField(max_length=2, choices=MODES, default='OF', db_index=True)

    pid  = models.IntegerField(null=True, db_index=True)
    tid  = models.IntegerField(default=-1, db_index=True)

    dl   = models.CharField(null=True, max_length=20)
    name = models.CharField(null=True, max_length=64, db_index=True)
    gdr  = models.CharField(null=True, max_length=16, db_index=True)
    age  = models.IntegerField(null=True, db_index=True)
    hs   = models.CharField(null=True, max_length=50)

    class Meta:
        db_table = 'driver'
        managed = True


class Place(models.Model):
    '''
    Places are predefined pickup points or vehicle stands
    -----------------------------------------------------
    id(int):    Autoincrement primary key
    pn(str):    Place name - human readable name of the place
    lat(float): Latitude
    lng(float): Longitude
    alt(int):   Altitude in meters
    wt(int):    Weightage - economic weightage (0 is remote village, 100 is a commercial place)
    '''
    pn = models.CharField(max_length=64, db_index=True, unique=True, null=False)
    lat = models.FloatField()
    lng = models.FloatField()
    alt = models.IntegerField()
    wt = models.IntegerField()

    class Meta:
        db_table = 'place'
        managed = True  


class Route(models.Model):
    '''
    Route is a convenience table storing road distance between any pair of places
    -----------------------------------------------------------------------------
    idx(int): first place id from places table
    idy(int): second place id from places table
    dist(int): distance in meters
    '''
    # TODO: Add a Validator that will reject a,b if b,a is already in the DB
    idx = models.IntegerField()
    idy = models.IntegerField()
    dist = models.IntegerField()

    @staticmethod
    def getRoute(idx, idy):
        if int(idx) > int(idy): #TypeError in comparing int and str
            idx, idy = idy, idx
        return Route.objects.filter(idx=idx, idy=idy)[0]

    class Meta:
        db_table = 'route'
        managed = True  
        indexes = [models.Index(fields=['idx', 'idy'])]


class Vehicle(models.Model):
    '''
    an (int) : Entity identifier (similar to aadhaar for User) - primary key
    tid(int) : Trip ID if this vehicle is assigned

    regn (string) : Vehicle registration details

    dist(int):   Total distance travelled in meters
    hrs(float): Total active hours
    pid(id):    Place where this vehicle is parked
    vtype : vehicle type (e-cycle/ escooty/ ebike/ zbee)

    Note:
        hrs represents total time when the vehicle was mobile, not total trip time - this data has to come from the vehicle IoT data
        Total trip time is aggregated from the Trip table
    '''
    CYCLE = 0
    SCOOTY = 1
    BIKE = 2
    ZBEE = 3

    NAMES = [
        'CYCLE',
        'SCOOTY',
        'BIKE',
        'ZBEE',
    ]

    # meters per second
    AVG_SPEED_M_PER_S = [3, 3.5, 4, 5.5]

    # default fare applied per trip
    BASE_FARE = [10, 15, 20, 30]

    # Charge per minute
    TIME_FARE = [0.1, 0.2, 0.3, 0.5]

    FAILED = -2
    AVAILABLE = -1;

    an = models.BigIntegerField(primary_key=True)
    tid = models.BigIntegerField(db_index=True, default=-1)
    regn = models.CharField(db_index=True, max_length=16)
    dist = models.IntegerField(null=True)
    hrs = models.FloatField(null=True)
    pid = models.IntegerField(null=True, db_index=True)
    vtype = models.IntegerField(null=True, default=3)

    class Meta:
        db_table = 'vehicle'
        managed = True


class Trip(models.Model):
    '''
    id (int): Autoincrement primary key
    st(str):  Current trip status see Trip.STATUSES
    uan(int): User aadhaar
    dan(int): Driver aadhaar
    van(int): Vehicle assigned number
    rtime:    Trip request timestamp
    atime:    Trip assign timestamp
    stime:    Trip start timestamp
    etime:    Trip end time (regardless of completion or failure)
    srcid:    Source place id
    dstid     Destination place id
    npas      Number of passengers
    rtype ; rent or ride
    pmode : payment mode (cash / upi)
    hrs : for RENTAL, number of hours
    '''
    STATUSES = [
        ('RQ', 'requested'),  # requested from the user via app
        ('AS', 'assigned'),   # accepted by the driver, waiting for the user to either come to driver or CN
        ('ST', 'started'),  # started by the driver
        ('TR', 'terminated'),  # aborted(by the user) en route
        ('FN', 'finished'),  # completed trip
        ('PD', 'paid'),  # payment completed

        ('CN', 'cancelled'),  # cancelled by the user
        ('DN', 'denied'),     # refused by driver, this happens only after AS state
        ('TO', 'timeout'),  # request timed out
        ('FL', 'failed'),  # failed due to any reason other than cancellation
    ]

    # Active trip states wrt users and drivers perspective
    USER_ACTIVE = ['RQ', 'AS', 'ST', 'FN', 'TR']  # not TO, CN, DN, FL, PD
    DRIVER_ACTIVE = ['AS', 'ST', 'FN', 'TR']      # not TO, CN, DN, RQ, FL, PD

    # States requiring payment to be done
    PAYABLE = ['FN', 'TR']

    CASH = 0
    UPI = 1
    PAYMENT = [('CASH', CASH),('UPI', UPI)]

    RIDE = 0
    RENT = 1
    TYPE =[ ('RIDE', RIDE),('RENT', RENT)]

    st    = models.CharField( max_length=2, choices=STATUSES, default='RQ', db_index=True)
    uan   = models.BigIntegerField (db_index=True)
    dan   = models.BigIntegerField(db_index=True, default=0)
    van   = models.BigIntegerField(db_index=True, default=0)
    rtime = models.DateTimeField(auto_now_add=True, db_index=True)
    atime = models.DateTimeField(db_index=True, null=True)
    stime = models.DateTimeField(db_index=True, null=True)
    etime = models.DateTimeField(db_index=True, null=True)
    srcid = models.IntegerField(db_index=True)
    dstid = models.IntegerField(db_index=True)
    npas  = models.IntegerField()
    rtype = models.CharField(db_index=True, choices=TYPE,  max_length=10, default=2)
    pmode = models.CharField(db_index=True, choices=PAYMENT, max_length=10, default=1)
    hrs = models.IntegerField(db_index=True, default=0)

    class Meta:
        db_table = 'trip'
        managed = True


class Progress(models.Model):
    '''
    Progress table contains the live percentage progress of every trip
    ------------------------------------------------------------------
    tid(int): trip id
    pct(int):  progress percent

    '''
    tid = models.IntegerField(primary_key=True)
    pct = models.IntegerField(db_index=True)

    class Meta:
        db_table = 'progress'
        managed = True


class Location(models.Model):
    '''
    Locations logs the the locations of the drivers/users updated periodically
    --------------------------------------------------------------------------
    an(int):      User/Driver Aadhaar number or vehicle ID
    time:         Log entry timestamp
    lat(float):   Latitude of user/driver
    long(float):  Longitude of user/driver
    kind(int):    0 is user, 1 is driver, 2 rental user, 3 is a vehicle

    '''
    an    = models.BigIntegerField(primary_key=True, default=0)
    lat   = models.FloatField()
    lng   = models.FloatField()
    time  = models.DateTimeField(auto_now=True)
    kind  = models.IntegerField(db_index=True, default=0)

    KINDS = {User: 0, Driver: 1, Vehicle: 2, User: 3}
    ENTITIES = [User, Driver, Vehicle, User]

    def save(self, *args, **kwargs):
        '''
        When saving, update user/driver or vehicle pid
        '''
        # Do the default superclass actions for save first
        #self.full_clean() # this method calls any validators we set ( will add later )
        super(Location, self).save(*args, **kwargs)

        # Get any  places within DSQUARE_THRESH of this lat/long
        # sqlite
        # qsNearest = Place.objects.raw('SELECT id, ABS(%s * %s * (place.lat-%s) * (place.lng-%s)) AS dsquare from place '
        #                               'WHERE dsquare < %s ORDER BY dsquare ASC',
        #                               [ settings.M_PER_DEG_LAT, settings.M_PER_DEG_LNG,
        #                                 self.lat, self.lng, settings.DSQUARE_THRESH])
        # mysql
        qsNearest = Place.objects.raw('SELECT id, ABS(%s * %s * (place.lat-%s) * (place.lng-%s)) AS \'dsquare\' from place '
                                      'WHERE \'dsquare\' < %s ORDER BY dsquare ASC',
                                      [settings.M_PER_DEG_LAT, settings.M_PER_DEG_LNG,
                                       self.lat, self.lng, settings.DSQUARE_THRESH])

        if len(qsNearest) > 0:
            idNew = qsNearest[0].id

            # Get the proper class, for which update of pid should take place
            theClass = Location.ENTITIES[self.kind]
            qsObj = theClass.objects.filter(pk=self.an)[0]

            # if place changed, update and save
            if qsObj.pid != idNew:
                qsObj.pid = idNew
                qsObj.save()

    class Meta:
        db_table = 'location'
        ordering = ['lat', 'lng']
        managed = True

