from django.db import models
from django.conf import settings

########################
# Ride/Rent module
########################


class User(models.Model):
    """
    User is the client using our app and APIs
    -----------------------------------------
    an(int)    :   primary key 91+phone number
    pn(str)    :   Phone number
    auth(str)  :   Client auth token
    pid(int)   :   Index of current place - see Place table
    tid(int)   :   Index of current trip - see Trip table
    did(int)   :   Index of current Delivery - see Delivery table
    name(str)  :   Name
    gdr(str)   :   Gender
    age(int)   :   Age
    dl(str)    :   Driver license in case of rent a ride
    hs (str)   :   Home state of the User
    mark(float):   rating system, BINARY, 1 mark given for good, 0 given for bad
    adhar(int) :   Aadhaar number of the user
    email(str) :   Email of the user
    fcm(str)   :   Firebase Cloud Messaging token of the User android app
    -----------------------------------------
    """
    an   = models.BigIntegerField(primary_key=True)
    pn   = models.CharField(max_length=32, db_index=True)
    auth = models.CharField(max_length=16, db_index=True)

    pid  = models.IntegerField(null=True, db_index=True)
    tid  = models.IntegerField(default=-1, db_index=True)
    did  = models.CharField(db_index=True, null=False, default='', max_length=11)

    name = models.CharField(null=True, max_length=64, db_index=True)
    gdr  = models.CharField(null=True, max_length=16, db_index=True)
    age  = models.SmallIntegerField(null=True, db_index=True)
    dl   = models.CharField(null=True, max_length=20)
    hs   = models.CharField(db_index=True,null=True, max_length=50)
    mark = models.FloatField(db_index=True, default=0.0)
    adhar= models.BigIntegerField(db_index=True, null=True)
    email= models.CharField(db_index=True, null=True, max_length=100)
    fcm  = models.CharField(db_index=True, null=True, max_length=512)
    class Meta:
        db_table = 'user'
        managed = True


class Driver(models.Model):
    """
    Driver is the partner who is driving our vehicles in RIDE
    ---------------------------------------------------------
    an(int)    :   Aadhaar number
    pn(str)    :   Phone number
    auth(str)  :   Client auth token
    dl(str)    :   Driving licence no.
    name(str)  :   Real name
    gdr(str)   :   Gender
    age(int)   :   Age
    mode(str)  :   Driver mode registering(RG), available(AV), booked(BK), offline(OF), locked(LK)
    pid(int)   :   Index of current place - see Place table
    tid(int)   :   Index of current trip - see Trip table
    van(int)   :   Vehicle number assigned
    hs(str)    :   Home state of the Driver
    mark(float):   Float field for the rating of the driver.
    fcm(str)   :   Firebase Cloud Messaging token of the Driver android app
    -----------------------------------------
    """
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
    van  = models.BigIntegerField(db_index=True, default=-1)
    mark = models.FloatField(db_index=True, default=0.0)
    fcm  = models.CharField(db_index=True, null=True, max_length=512)

    class Meta:
        db_table = 'driver'
        managed = True


class Place(models.Model):
    """
    Places are predefined pickup points or vehicle stands
    -----------------------------------------------------
    id(int)    :   Autoincrement primary key
    pn(str)    :   Place name - human readable name of the place
    lat(float) :   Latitude
    lng(float) :   Longitude
    alt(int)   :   Altitude in meters
    wt(int)    :   Weightage - economic weightage (0 is remote village, 100 is a commercial place)
    -----------------------------------------
    """
    pn = models.CharField(max_length=64, db_index=True, unique=True, null=False)
    lat = models.FloatField()
    lng = models.FloatField()
    alt = models.IntegerField()
    wt = models.IntegerField()

    class Meta:
        db_table = 'place'
        managed = True


class Route(models.Model):
    """
    Route is a convenience table storing road distance between any pair of places
    -----------------------------------------------------------------------------
    idx(int)  : first place id from places table
    idy(int)  : second place id from places table
    dist(int) : distance in meters
    -----------------------------------------
    """
    idx = models.IntegerField()
    idy = models.IntegerField()
    dist = models.IntegerField()

    @staticmethod
    def getRoute(idx, idy):
        if int(idx) > int(idy):  # removed TypeError in comparing int and str
            idx, idy = idy, idx
        return Route.objects.filter(idx=idx, idy=idy)[0]

    class Meta:
        db_table = 'route'
        managed = True
        indexes = [models.Index(fields=['idx', 'idy'])]


class Vehicle(models.Model):
    """
    Vehicle stored all data about the vehicle
    -----------------------------------------
    an (int)      :  Entity identifier (similar to aadhaar for User) - primary key
    tid(int)      :  Trip ID if this vehicle is assigned
    regn (str)    :  Vehicle registration details
    dist(int)     :  Total distance travelled in meters
    hrs(float)    :  Total active hours
    pid(id)       :  Place where this vehicle is parked
    vtype(int)    :  vehicle type (e-cycle/ escooty/ ebike/ zbee)
    mark(float)   :  float field has the rating, binary system same as that of the user/driver
    -----------------------------------------
    Note:
        hrs represents total time when the vehicle was mobile, not total trip time
        - this data has to come from the vehicle IoT data
        Total trip time is aggregated from the Trip table
    """
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

    # TODO update these as per actuals
    # meters per second
    AVG_SPEED_M_PER_S = [3, 3.5, 4, 5.5]

    # default fare applied per trip
    BASE_FARE = [10, 15, 20, 30]

    # Charge per minute
    TIME_FARE = [0.25, 0.5, 0.75, 1.0]

    FAILED = -2
    AVAILABLE = -1

    an = models.BigIntegerField(primary_key=True)
    tid = models.BigIntegerField(db_index=True, default=-1)
    dan = models.BigIntegerField(db_index=True, default=-1)

    regn = models.CharField(db_index=True, max_length=16)
    dist = models.IntegerField(null=True)
    hrs = models.FloatField(null=True)
    pid = models.IntegerField(null=True, db_index=True)
    vtype = models.IntegerField(null=True, default=3)
    mark = models.FloatField(db_index=True, default=0.0)

    class Meta:
        db_table = 'vehicle'
        managed = True


class Trip(models.Model):
    """
    Trip table stores the details about trips
    -----------------------------------------
    id (int)  : Autoincrement primary key
    st(str)   : Current trip status see Trip.STATUSES
    uan(int)  : User aadhaar
    dan(int)  : Driver aadhaar
    van(int)  : Vehicle assigned number
    rtime     : Trip request timestamp
    atime     : Trip assign timestamp
    stime     : Trip start timestamp
    etime     : Trip end time (regardless of completion or failure)
    srcid     : Source place id
    dstid     : Destination place id
    srcname   : Name of Source
    srclat    : Latitude of source
    srclng    : Longitude of source
    dstname   : Name of Destination
    dstlat    : Latitude of destination
    dstlng    : Longitude of destination
    npas      : Number of passengers
    rtype     : rent or ride
    pmode     : payment mode (cash / upi)
    hrs       : for RENTAL, number of hours
    rvtype    : requested vehicle type
    url       : url for tracking
    htid      : tid for trip
    -----------------------------------------
    """
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
        # ('RT', 'rated'), # THIS MEANS THAT THE TRIP IS DONE
    ]

    # Active trip states wrt users and drivers perspective
    USER_ACTIVE = ['RQ', 'AS', 'ST', 'FN', 'TR']  # not TO, CN, DN, FL, PD
    DRIVER_ACTIVE = ['AS', 'ST', 'FN', 'TR']      # not TO, CN, DN, RQ, FL, PD
    SUPER_ACTIVE = ['AS', 'FN', 'TR', 'ST']             # not TO, CN, DN, RQ, ST, PD, FL
    STATES = ['RQ', 'AS', 'ST', 'FN', 'TR', 'TO', 'CN', 'DN', 'FL', 'PD']

    # States requiring payment to be done
    PAYABLE = ['FN', 'TR']

    CASH = 0
    UPI = 1
    PAYMENT = [('CASH', CASH),('UPI', UPI)]

    RIDE = 0
    RENT = 1
    TYPE =[ ('RIDE', RIDE),('RENT', RENT)]
    TYPES =[ ('0', RIDE),('1', RENT)]

    st    = models.CharField( max_length=2, choices=STATUSES, default='RQ', db_index=True)
    uan   = models.BigIntegerField (db_index=True)
    dan   = models.BigIntegerField(db_index=True, default=-1)
    van   = models.BigIntegerField(db_index=True, default=-1)
    rtime = models.DateTimeField(auto_now_add=True, db_index=True)
    atime = models.DateTimeField(db_index=True, null=True)
    stime = models.DateTimeField(db_index=True, null=True)
    etime = models.DateTimeField(db_index=True, null=True)
    srcid = models.IntegerField(db_index=True, default=1) #dummy values
    dstid = models.IntegerField(db_index=True, default=2) #dummy values
    srclat = models.FloatField(db_index=True, default=-1)
    srclng = models.FloatField(db_index=True, default=-1)
    dstlat = models.FloatField(db_index=True, default=-1)
    dstlng = models.FloatField(db_index=True, default=-1)
    srcname = models.CharField(max_length=314, null=False, default='', db_index=True)
    dstname = models.CharField(max_length=314, null=False, default='', db_index=True)

    rvtype = models.IntegerField(null=True, default=3)  # default is ZBEE
    npas  = models.IntegerField(db_index=True, default=0)
    rtype = models.CharField(db_index=True, choices=TYPES,  max_length=10, default=2)  # default 2, dummy value
    pmode = models.CharField(db_index=True, choices=PAYMENT, max_length=10, default=1)  # default UPI
    hrs = models.IntegerField(db_index=True, default=0)
    url = models.CharField(max_length=30, null=True)
    htid = models.CharField(max_length=50, null=True)

    class Meta:
        db_table = 'trip'
        managed = True


class Progress(models.Model):
    """
    Progress table contains the live percentage progress of every trip
    ------------------------------------------------------------------
    tid(int) : trip id
    pct(int) :  progress percent
    -----------------------------------------
    """
    tid = models.IntegerField(primary_key=True)
    pct = models.IntegerField(db_index=True)

    class Meta:
        db_table = 'progress'
        managed = True


class Supervisor(models.Model):
    """
    Supervisor is for the rental module, supervises the vehicles
    ------------------------------------------------------------
    an(int)   :    Aadhaar number
    pn(str)   :    Phone number
    auth(str) :    Supervisor auth token
    dl(str)   :    Driving licence no.
    name(str) :    Real name
    gdr(str)  :    Gender
    age(int)  :    Age
    pid(int)  :    Index of current place - see Place table
    hs(str)   :    Home state of the Supervisor
    -----------------------------------------
    """

    an   = models.BigIntegerField(primary_key=True)
    pn   = models.CharField(max_length=32, db_index=True)
    auth = models.CharField(max_length=16, db_index=True)

    pid  = models.IntegerField(null=True, db_index=True)

    dl   = models.CharField(null=True, max_length=20)
    name = models.CharField(null=True, max_length=64, db_index=True)
    gdr  = models.CharField(null=True, max_length=16, db_index=True)
    age  = models.IntegerField(null=True, db_index=True)
    hs   = models.CharField(null=True, max_length=50)

    class Meta:
        db_table = 'super'
        managed = True


class Manager(models.Model):
    """
    Manager is for the ride module, assgins the vehicles to Driver
    --------------------------------------------------------------
    an(int)   : Aadhaar number
    pn(str)   : Phone number
    auth(str) : Hub manager auth token
    dl(str)   : DL no.
    name(str) : Name
    gdr(str)  : Gender
    age(int)  : Age in years
    pid(int)  : Index of the current hub - todo Hub table
    #todo how to add list of vehicle to this Manager
    hs(str)   : Home state
    -----------------------------------------
    """
    an   = models.BigIntegerField(primary_key=True)
    pn   = models.CharField(max_length=32, db_index=True)
    auth = models.CharField(max_length=16, db_index=True)
    pid  = models.IntegerField(null=True, db_index=True)
    dl   = models.CharField(null=True, max_length=20)
    name = models.CharField(null=True, max_length=64, db_index=True)
    gdr  = models.CharField(null=True, max_length=16, db_index=True)
    age  = models.IntegerField(null=True, db_index=True)
    hs   = models.CharField(null=True, max_length=50)
    
    class Meta:
        db_table = 'manager'
        managed = True   


########################
# Delivery module
########################

class Delivery(models.Model):
    """
    tabula rasa
    -----------------------------------------
    -----------------------------------------
    """
    STATUSES = [
        ('SC', 'scheduled'),  # scheduler by the user via app
        ('PD', 'paid'),     #paid
        ('RC', 'reached'),

        ('RQ', 'requested'),  # requested from the user via app
        ('AS', 'assigned'),  # assigned a delivery agent to this, waiting for the agent to come to user location
        ('ST', 'started'),  # agent started delivery
        ('FN', 'finished'),  # delivered successfully
        ('CN', 'cancelled'),  # cancelled by the user
        ('DN', 'denied'),  # refused by agent, this happens only after AS state
        ('TO', 'timeout'),  # request timed out
        ('FL', 'failed'),  # failed due to any reason other than cancellation
    ]

    # Active delivery states wrt user's and agent's perspective
    USER_ACTIVE = ['SC']  # not PD, RQ, AS, ST, TO, CN, DN, FL, FN
    AGENT_ACTIVE = ['AS', 'RC', 'ST']  # not PD, RQ, TO, CN, DN, FL, FN

    # States requiring payment to be done
    PAYABLE = ['SC']

    CASH = 0
    UPI = 1
    PAYMENT = [('CASH', CASH), ('UPI', UPI)]

    DOCUMENT = 0
    CLOTHES = 1
    FOOD = 2
    HOUSEHOLD = 3
    ELETRONIC = 4
    OTHER = 5

    CATEGORIES = [('DOC',DOCUMENT) , ('CLO', CLOTHES), ('FOO', FOOD),
                  ('HOU', HOUSEHOLD),('ELE', ELETRONIC), ('OTH', OTHER ) ]

    DIMENSIONS = [('S', 'SMALL'),
                  ('M', 'MEDIUM'),
                  ('L', 'LARGE'),
                  ('XL', 'EXTRALARGE'),
                  ('XXL', 'EXTRAALARGE')
                ]
    # 6 check
    # fragile,flammable, liquid, keep dry, keep warm , keep cold
    CHECKBOXES = [('NO', 'NONE'),
                  ('BR', 'BREAKABLE'),
                  ('FR', 'FRAGILE'),
                  ('LI', 'LIQUID'),
                  ('KW', 'KEEPWARM'),
                  ('KC', 'KEEPCOLD'),
                  ('PE', 'PERISHABLE')
                ]

    scid = models.CharField(db_index=True, null=False, default='', max_length=11)

    fr = models.BooleanField(default=False, db_index=True)
    br = models.BooleanField(default=False, db_index=True)
    li = models.BooleanField(default=False, db_index=True)
    pe = models.BooleanField(default=False, db_index=True)
    kw = models.BooleanField(default=False, db_index=True)
    kc = models.BooleanField(default=False, db_index=True)

    express = models.BooleanField(default=False, db_index=True)

    st = models.CharField(max_length=2, choices=STATUSES, default='SC', db_index=True)  # scheduled
    uan = models.BigIntegerField(db_index=True)
    dan = models.BigIntegerField(db_index=True, default=-1)
    van = models.BigIntegerField(db_index=True, default=-1)
    rtime = models.DateTimeField(auto_now_add=True, db_index=True)
    atime = models.DateTimeField(db_index=True, null=True)
    stime = models.DateTimeField(db_index=True, null=True)
    etime = models.DateTimeField(db_index=True, null=True)

    picktime = models.DateTimeField(db_index=True, null=True)
    droptime = models.DateTimeField(db_index=True, null=True)

    srcpin = models.IntegerField(db_index=True)
    srclat = models.FloatField(null=False, db_index=True, default=0)
    srclng = models.FloatField(null=False, db_index=True, default=0)
    dstpin = models.IntegerField(db_index=True)
    dstlat = models.FloatField(null=False, db_index=True, default=0)
    dstlng = models.FloatField(null=False, db_index=True, default=0)

    itype = models.CharField(db_index=True, choices=CATEGORIES, max_length=20, default='OTH')
    idim = models.CharField(db_index=True, choices=DIMENSIONS, max_length=6, default='M')

    # delivery address
    srcper = models.CharField(null=True, max_length=64, db_index=True)
    dstper = models.CharField(null=True, max_length=64, db_index=True)

    srcadd = models.CharField(db_index=False, max_length=200, null=True)
    dstadd = models.CharField(db_index=False, max_length=200, null=True)
    srcland = models.CharField(db_index=True, max_length=200, null=True)
    dstland = models.CharField(db_index=True, max_length=200, null=True)
    srcphone = models.CharField(max_length=15, db_index=True, null=True)
    dstphone = models.CharField(max_length=15, db_index=True, null=True)

    # weights are as per dimensions
    pmode = models.CharField(db_index=True, choices=PAYMENT, max_length=10, default=1)

    det = models.CharField(db_index=False, max_length=150, null=True)
    srcdet = models.CharField(db_index=False, max_length=150, null=True)
    dstdet = models.CharField(db_index=False, max_length=150, null=True)

    tip = models.IntegerField(db_index=False, default=0)

    class Meta:
        db_table = 'delivery'
        managed = True


class Agent(models.Model):
    """
    Delivery agent
    --------------
    an(int)    :   Aadhaar number
    pn(str)    :   Phone number
    auth(str)  :   Client auth token
    dl(str)    :   Agent licence no.
    name(str)  :   Real name
    gdr(str)   :   Gender
    age(int)   :   Age
    mode(str)  :   Driver mode registering(RG), available(AV), booked(BK), offline(OF), locked(LK)
    pid(int)   :   Index of current place - see Place table
    tid(int)   :   Index of current trip - see Trip table
    hs(str)    :   Home state of the Agent
    veh(int)   :   Has a vehicle or not
    mark(float):   float field has the rating, binary system same as that of the user/driver
    fcm(str)   :   Firebase Cloud Messaging token of the Agent android app
    -----------------------------------------
    """
    MODES = [
        ('RG', 'registering'),  # Agent is under registration process
        ('AV', 'available'),  # Agent is online, waiting for deliveries
        ('BK', 'booked'),     # Agent booked by user
        ('OF', 'offline'),    # Agent is offline
        ('LK', 'locked'),      # Agent is locked
    ]
    VEH = [
        ('0', 'false'), # agent is using our vehicle
        ('1', 'true') # agent has his her own vehicle
    ]
    an   = models.BigIntegerField(primary_key=True)
    pn   = models.CharField(max_length=32, db_index=True)
    auth = models.CharField(max_length=16, db_index=True)
    mode = models.CharField(max_length=2, choices=MODES, default='OF', db_index=True)

    pid  = models.IntegerField(null=True, db_index=True)
    did  = models.IntegerField(default=-1, db_index=True)

    dl   = models.CharField(null=True, max_length=20)
    name = models.CharField(null=True, max_length=64, db_index=True)
    gdr  = models.CharField(null=True, max_length=16, db_index=True)
    age  = models.IntegerField(null=True, db_index=True)
    hs   = models.CharField(null=True, max_length=50)
    veh  = models.CharField(max_length=1, choices=VEH, default='0', db_index=True)
    mark = models.FloatField(db_index=True, default=0.0)
    fcm  = models.CharField(db_index=True, null=True, max_length=512)

    class Meta:
        db_table = 'agent'
        managed = True


class Rate(models.Model):
    """
    Rate stores the rating and the money paid for the trip
    ------------------------------------------------------
    id (int)    : Autoincrement primary key
    type(str)   : type of the rating, which is rent+rentid, or ride+rideid, deli+deliveryid
    rating(str) : basic rating
    rev(str)    : review in detail
    time(time)  : timestamp when rating was done
    dan(int)    : driver/supervisor aadhaar number
    -----------------------------------------
    """
    TYPE = [
        ('RIDE', 'ride'),
        ('RENT', 'rental'),
        ('DELI', 'delivery'),
        ('NAN', 'null')
    ]

    RATINGS = [
        ('attitude', 'attitude'),  # Attitude of contact person (driver/supervisor/delivery agent)
        ('vehiclecon', 'vehiclecondition'),   # Vehicle condition
        ('cleanliness',  'cleanliness'),  # cleanliness of the vehicle
        ('other', ' other '),  # failed due to any reason other than cancellation
    ]
    id = models.CharField(primary_key=True, max_length=10)
    type = models.CharField(max_length=4, choices=TYPE, default='NAN', db_index=True)
    rating = models.CharField(max_length=20, choices=RATINGS, default='OT', db_index=True)
    money = models.FloatField(db_index=True, default=0.0)
    rev = models.CharField(max_length=280, default='')
    time  = models.DateTimeField(db_index=True, auto_now=True)
    dan   = models.BigIntegerField(db_index=True, default=-1)
    class Meta:
        db_table = 'rate'
        managed = True


########################
# Shop module
########################


class Product(models.Model):
    """
    Product in the SHOP module
    --------------------------

    name(str)             : name of the product
    type(bool)            : simple 0 or grouped 1
    regular_price (float) : MRP of the product
    cost_price (float)    : price we are getting the product at
    sale_price (float)    : selling price of the product
    stock_quantity(int)   : quantity of the item in the stock
    categories(str)       : product categories( see Category table)
    weight (float)        : weight (in grams) of one unit of the product
    SKU(str)              : PRIMARY key
    tax_class(float)      : how much tax on the product
    low_stock_amount(int) : low stock alert
    -----------------------------------------
    """
    #id is given by default
    #id = models.AutoField(primary_key=True)
    name  = models.CharField(null=True, max_length=200, db_index=True)
    
    TYPE = [
        ('simple', 'simple'),
        ('grouped', 'grouped'),
        ('external', 'external'),
        ('variable', 'variable')
    ]

    type = models.CharField(max_length=10, choices=TYPE, default='simple')
    catalog_visibility = models.CharField(max_length=10, default='visible')
    published = models.CharField(max_length=10, default='publish')
    # 4 
    published = models.CharField(max_length=10, default='publish', )
    description = models.CharField(max_length=2000, default='', null=True)
    short_description = models.CharField(max_length=500, default='', null=True)
    # 7 
    regular_price = models.FloatField(db_index=True, default=0.0)
    sale_price = models.FloatField(db_index=True, default=0.0)
    stock_quantity = models.IntegerField(default=0, db_index=True)
    # 10
    backorders = models.CharField(max_length=10, default='yes')
    categories = models.CharField(null=True, db_index=True, max_length=50)
    weight = models.FloatField(db_index=True, default=0.0)
    # 13
    sold_individually = models.CharField(max_length=10, default='FALSE')
    tags = models.CharField(null=True, max_length=50)
    menu_order = models.IntegerField(default=1, db_index=True)
    # 16
    grouped = models.CharField(max_length=10, default='FALSE')
    sku  = models.CharField(null=True, max_length=200, db_index=True)
    tax_status = models.CharField(null=True,default='taxable', max_length=10)
    # 19
    tax_class = models.FloatField(db_index=True, default=0.0)
    purchase_note = models.CharField(max_length=250, default='Thanks :) kindly check the package for expiry details.', null=True)
    # 21
    upsell_ids = models.CharField(null=True, max_length=200)
    cross_sell_ids = models.CharField(null=True, max_length=200)
    parent_id = models.CharField(null=True, max_length=200)
    # 24
    low_stock_amount  = models.SmallIntegerField(default=1, db_index=True)
    images = models.CharField(max_length=250, default='https://cdn.business2community.com/wp-content/uploads/2014/01/product-coming-soon.jpg', null=True)
    cost_price = models.FloatField(default=0.0, db_index=True)
    #27
    
    def save(self, *args, **kwargs):
        self.menu_order = self.menu_order + 1
        super().save(*args, **kwargs)
    
    class Meta:
        db_table = 'product'
        managed = True
        
        
class Purchaser(models.Model):
    """
    Purchaser goes to have a purchase for SHOP module
    -------------------------------------------------
    an(int)   :   Aadhaar number
    pn(str)   :   Phone number
    auth(str) :   Purchaser auth token
    dl(str)   :   Driving licence no.
    name(str) :   Real name
    gdr(str)  :   Gender
    age(int)  :   Age
    pid(int)  :   Index of current place - see Place table
    hs(str)   :   Home state of the Purchaser
    fcm(str)  :   Firebase cloud messaging token of the Purchaser android app
    -----------------------------------------
    """

    an   = models.BigIntegerField(primary_key=True)
    pn   = models.CharField(max_length=32, db_index=True)
    auth = models.CharField(max_length=16, db_index=True)
    pid  = models.IntegerField(null=True, db_index=True)

    dl   = models.CharField(null=True, max_length=20)
    name = models.CharField(null=True, max_length=64, db_index=True)
    gdr  = models.CharField(null=True, max_length=16, db_index=True)
    age  = models.IntegerField(null=True, db_index=True)
    hs   = models.CharField(null=True, max_length=50)
    fcm  = models.CharField(db_index=True, null=True, max_length=512)

    class Meta:
        db_table = 'purchaser'
        managed = True


########################
# Service module
########################

class Servitor(models.Model):
    """
    Servitor is the the Service providing person
    --------------------------------------------

    an(int)    :   Aadhaar number
    pn(str)    :   Phone number
    auth(str)  :   Servitor auth token
    dl(str)    :   Driving licence no.
    name(str)  :   Real name
    gdr(str)   :   Gender
    age(int)   :   Age
    pid(int)   :   Index of current place - see Place table
    hs(str)    :   Home state of the Servitor
    job(str)   :   What does that person does
    wage(float):   hourly charge
    mark(float):   rating of the service person
    bank(str)  :   bank details of the Servitor
    -----------------------------------------
    """

    an   = models.BigIntegerField(primary_key=True)
    pn   = models.CharField(max_length=32, db_index=True)
    auth = models.CharField(max_length=16, db_index=True)
    pid  = models.IntegerField(null=True, db_index=True)

    dl   = models.CharField(null=True, max_length=20)
    name = models.CharField(null=True, max_length=64, db_index=True)
    gdr  = models.CharField(null=True, max_length=10, db_index=True)
    age  = models.IntegerField(null=True, db_index=True)
    hs   = models.CharField(null=True, max_length=50)

    # 5 jobs of the Servitor
    job1 = models.CharField(null=False, max_length=3, default='')
    job2 = models.CharField(null=True, max_length=3)
    job3 = models.CharField(null=True, max_length=3)
    job4 = models.CharField(null=True, max_length=3)
    job5 = models.CharField(null=True, max_length=3)

    wage = models.FloatField(db_index=True, default=0.0)
    mark = models.FloatField(db_index=True, default=0.0)
    bank = models.CharField(null=False, max_length=300, default='')

    class Meta:
        db_table = 'servitor'
        managed = True


class Job(models.Model):
    """
    predefined Jobs for the servitor
    -----------------------------------------------------
    id(int)    :   Autoincrement primary key
    jname(str) :   Job name
    jtype(int) :   Type of the job
    -----------------------------------------
    """
    TYPE = [
        ('esse', 'EssentialServices'),
        ('prof', 'ProfessionalService'),
        ('hire', 'ForHire')
    ]
    jname = models.CharField(max_length=100, db_index=True, unique=True, null=False)
    jtype = models.CharField(max_length=5, choices=TYPE, default='None')

    class Meta:
        db_table = 'job'
        managed = True


class Booking(models.Model):
    """
    Bookings for the servitor
    -----------------------------------------------------
    order_number(BIG int)    :   Order number of the booking primary key

    -----------------------------------------------------
    Note : Bookings are uploaded from the booking csv
    """

    order_number = models.BigIntegerField(primary_key=True)
    order_status = models.CharField(max_length=10, null=False, default='')
    order_date  = models.DateTimeField(auto_now=True)

    customer_note = models.CharField(null=True, max_length=256)

    first_name_billing = models.CharField(max_length=32, null=False, default='')
    last_name_billing = models.CharField(max_length=32, null=False, default='')

    last_name_billing = models.CharField(max_length=32, null=False, default='')
    last_name_billing = models.CharField(max_length=32, null=False, default='')

    company_billing = models.CharField(max_length=32, null=False, default='')
    address_1_2_billing = models.CharField(max_length=64, null=False, default='')
    city_billing = models.CharField(max_length=32, null=False, default='')
    state_code_billing = models.CharField(max_length=8, null=False, default='')
    postcode_billing = models.CharField(max_length=6, null=False, default='')
    country_code_billing = models.CharField(max_length=4, null=False, default='IN')

    email_billing = models.CharField(max_length=16, null=False, default='')
    phone_billing = models.CharField(max_length=12, null=False, default='')

    first_name_shipping = models.CharField(null=True, max_length=32)
    last_name_shipping = models.CharField(null=True, max_length=32)
    address_1_2_shipping = models.CharField(null=True, max_length=32)
    city_shipping = models.CharField(null=True, max_length=32)
    state_code_shipping = models.CharField(null=True, max_length=32)
    postcode_shipping = models.CharField(null=True, max_length=6)
    country_code_shipping = models.CharField(null=True, max_length=4)
    payment_method_title = models.CharField(null=True, max_length=20)

    cart_discount_amount = models.FloatField(db_index=True, default=0.0)
    order_subtotal_amount = models.FloatField(db_index=True, default=0.0)
    shipping_method_title = models.CharField(null=True, max_length=32)
    order_shipping_amount = models.FloatField(db_index=True, default=0.0)
    order_refund_amount = models.FloatField(db_index=True, default=0.0)

    order_total_amount = models.FloatField(db_index=True, default=0.0)
    order_total_tax_amount = models.FloatField(db_index=True, default=0.0)

    sku = models.CharField(null=True, max_length=32)

    item_qty = models.CharField(null=True, max_length=32)
    item_name = models.CharField(null=True, max_length=32)
    quantity = models.IntegerField(db_index=True, default=1)
    item_cost = models.FloatField(db_index=True, default=0.0)

    coupon_Code = models.CharField(null=True, max_length=32)
    discount_amount = models.FloatField(null=True)
    discount_amount_tax = models.FloatField(null=True)

    class Meta:
        db_table = 'booking'
        managed = True


########################
# Location
########################

class Location(models.Model):
    '''
    Locations logs the the locations of the drivers/users updated periodically
    --------------------------------------------------------------------------
    an(int)     :    User/Driver Aadhaar number or vehicle ID
    time        :    Log entry timestamp
    lat(float)  :    Latitude of user/driver
    long(float) :    Longitude of user/driver
    kind(int)   :    0 is user, 1 is driver, 2 rental user, 3 is a vehicle
    -----------------------------------------
    '''
    an    = models.BigIntegerField(primary_key=True, default=0)
    lat   = models.FloatField()
    lng   = models.FloatField()
    time  = models.DateTimeField(auto_now=True)
    kind  = models.IntegerField(db_index=True, default=0)

    KINDS = {User: 0, Driver: 1, Vehicle: 2, Supervisor: 3, Agent: 4, Servitor:5}
    ENTITIES = [User, Driver, Vehicle, Supervisor, Agent, Servitor]

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


