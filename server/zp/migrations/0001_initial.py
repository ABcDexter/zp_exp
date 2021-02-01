# Generated by Django 3.1.5 on 2021-01-30 06:14

from django.db import migrations, models


class Migration(migrations.Migration):

    initial = True

    dependencies = [
    ]

    operations = [
        migrations.CreateModel(
            name='Agent',
            fields=[
                ('an', models.BigIntegerField(primary_key=True, serialize=False)),
                ('pn', models.CharField(db_index=True, max_length=32)),
                ('auth', models.CharField(db_index=True, max_length=16)),
                ('mode', models.CharField(choices=[('RG', 'registering'), ('AV', 'available'), ('BK', 'booked'), ('OF', 'offline'), ('LK', 'locked')], db_index=True, default='OF', max_length=2)),
                ('pid', models.IntegerField(db_index=True, null=True)),
                ('did', models.IntegerField(db_index=True, default=-1)),
                ('dl', models.CharField(max_length=20, null=True)),
                ('name', models.CharField(db_index=True, max_length=64, null=True)),
                ('gdr', models.CharField(db_index=True, max_length=16, null=True)),
                ('age', models.IntegerField(db_index=True, null=True)),
                ('hs', models.CharField(max_length=50, null=True)),
                ('veh', models.CharField(choices=[('0', 'false'), ('1', 'true')], db_index=True, default='0', max_length=1)),
                ('mark', models.FloatField(db_index=True, default=0.0)),
                ('fcm', models.CharField(db_index=True, max_length=512, null=True)),
            ],
            options={
                'db_table': 'agent',
                'managed': True,
            },
        ),
        migrations.CreateModel(
            name='Booking',
            fields=[
                ('order_number', models.BigIntegerField(primary_key=True, serialize=False)),
                ('order_status', models.CharField(default='', max_length=10)),
                ('order_date', models.DateTimeField(auto_now=True)),
                ('customer_note', models.CharField(max_length=256, null=True)),
                ('first_name_billing', models.CharField(default='', max_length=32)),
                ('last_name_billing', models.CharField(default='', max_length=32)),
                ('company_billing', models.CharField(default='', max_length=32)),
                ('address_1_2_billing', models.CharField(default='', max_length=64)),
                ('city_billing', models.CharField(default='', max_length=32)),
                ('state_code_billing', models.CharField(default='', max_length=8)),
                ('postcode_billing', models.CharField(default='', max_length=6)),
                ('country_code_billing', models.CharField(default='IN', max_length=4)),
                ('email_billing', models.CharField(default='', max_length=16)),
                ('phone_billing', models.CharField(default='', max_length=12)),
                ('first_name_shipping', models.CharField(max_length=32, null=True)),
                ('last_name_shipping', models.CharField(max_length=32, null=True)),
                ('address_1_2_shipping', models.CharField(max_length=32, null=True)),
                ('city_shipping', models.CharField(max_length=32, null=True)),
                ('state_code_shipping', models.CharField(max_length=32, null=True)),
                ('postcode_shipping', models.CharField(max_length=6, null=True)),
                ('country_code_shipping', models.CharField(max_length=4, null=True)),
                ('payment_method_title', models.CharField(max_length=20, null=True)),
                ('cart_discount_amount', models.FloatField(db_index=True, default=0.0)),
                ('order_subtotal_amount', models.FloatField(db_index=True, default=0.0)),
                ('shipping_method_title', models.CharField(max_length=32, null=True)),
                ('order_shipping_amount', models.FloatField(db_index=True, default=0.0)),
                ('order_refund_amount', models.FloatField(db_index=True, default=0.0)),
                ('order_total_amount', models.FloatField(db_index=True, default=0.0)),
                ('order_total_tax_amount', models.FloatField(db_index=True, default=0.0)),
                ('sku', models.CharField(max_length=32, null=True)),
                ('item_qty', models.CharField(max_length=32, null=True)),
                ('item_name', models.CharField(max_length=32, null=True)),
                ('quantity', models.IntegerField(db_index=True, default=1)),
                ('item_cost', models.FloatField(db_index=True, default=0.0)),
                ('coupon_Code', models.CharField(max_length=32, null=True)),
                ('discount_amount', models.FloatField(null=True)),
                ('discount_amount_tax', models.FloatField(null=True)),
            ],
            options={
                'db_table': 'booking',
                'managed': True,
            },
        ),
        migrations.CreateModel(
            name='Delivery',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('scid', models.CharField(db_index=True, default='', max_length=11)),
                ('fr', models.BooleanField(db_index=True, default=False)),
                ('br', models.BooleanField(db_index=True, default=False)),
                ('li', models.BooleanField(db_index=True, default=False)),
                ('pe', models.BooleanField(db_index=True, default=False)),
                ('kw', models.BooleanField(db_index=True, default=False)),
                ('kc', models.BooleanField(db_index=True, default=False)),
                ('express', models.BooleanField(db_index=True, default=False)),
                ('st', models.CharField(choices=[('SC', 'scheduled'), ('PD', 'paid'), ('RC', 'reached'), ('RQ', 'requested'), ('AS', 'assigned'), ('ST', 'started'), ('FN', 'finished'), ('CN', 'cancelled'), ('DN', 'denied'), ('TO', 'timeout'), ('FL', 'failed')], db_index=True, default='SC', max_length=2)),
                ('uan', models.BigIntegerField(db_index=True)),
                ('dan', models.BigIntegerField(db_index=True, default=-1)),
                ('van', models.BigIntegerField(db_index=True, default=-1)),
                ('rtime', models.DateTimeField(auto_now_add=True, db_index=True)),
                ('atime', models.DateTimeField(db_index=True, null=True)),
                ('stime', models.DateTimeField(db_index=True, null=True)),
                ('etime', models.DateTimeField(db_index=True, null=True)),
                ('picktime', models.DateTimeField(db_index=True, null=True)),
                ('droptime', models.DateTimeField(db_index=True, null=True)),
                ('srcpin', models.IntegerField(db_index=True)),
                ('srclat', models.FloatField(db_index=True, default=0)),
                ('srclng', models.FloatField(db_index=True, default=0)),
                ('dstpin', models.IntegerField(db_index=True)),
                ('dstlat', models.FloatField(db_index=True, default=0)),
                ('dstlng', models.FloatField(db_index=True, default=0)),
                ('itype', models.CharField(choices=[('DOC', 0), ('CLO', 1), ('FOO', 2), ('HOU', 3), ('ELE', 4), ('OTH', 5)], db_index=True, default='OTH', max_length=20)),
                ('idim', models.CharField(choices=[('S', 'SMALL'), ('M', 'MEDIUM'), ('L', 'LARGE'), ('XL', 'EXTRALARGE'), ('XXL', 'EXTRAALARGE')], db_index=True, default='M', max_length=6)),
                ('srcper', models.CharField(db_index=True, max_length=64, null=True)),
                ('dstper', models.CharField(db_index=True, max_length=64, null=True)),
                ('srcadd', models.CharField(max_length=200, null=True)),
                ('dstadd', models.CharField(max_length=200, null=True)),
                ('srcland', models.CharField(db_index=True, max_length=200, null=True)),
                ('dstland', models.CharField(db_index=True, max_length=200, null=True)),
                ('srcphone', models.CharField(db_index=True, max_length=15, null=True)),
                ('dstphone', models.CharField(db_index=True, max_length=15, null=True)),
                ('pmode', models.CharField(choices=[('CASH', 0), ('UPI', 1)], db_index=True, default=1, max_length=10)),
                ('det', models.CharField(max_length=150, null=True)),
                ('srcdet', models.CharField(max_length=150, null=True)),
                ('dstdet', models.CharField(max_length=150, null=True)),
                ('tip', models.IntegerField(default=0)),
            ],
            options={
                'db_table': 'delivery',
                'managed': True,
            },
        ),
        migrations.CreateModel(
            name='Driver',
            fields=[
                ('an', models.BigIntegerField(primary_key=True, serialize=False)),
                ('pn', models.CharField(db_index=True, max_length=32)),
                ('auth', models.CharField(db_index=True, max_length=16)),
                ('mode', models.CharField(choices=[('RG', 'registering'), ('AV', 'available'), ('BK', 'booked'), ('OF', 'offline'), ('LK', 'locked')], db_index=True, default='OF', max_length=2)),
                ('pid', models.IntegerField(db_index=True, null=True)),
                ('tid', models.IntegerField(db_index=True, default=-1)),
                ('dl', models.CharField(max_length=20, null=True)),
                ('name', models.CharField(db_index=True, max_length=64, null=True)),
                ('gdr', models.CharField(db_index=True, max_length=16, null=True)),
                ('age', models.IntegerField(db_index=True, null=True)),
                ('hs', models.CharField(max_length=50, null=True)),
                ('van', models.BigIntegerField(db_index=True, default=-1)),
                ('mark', models.FloatField(db_index=True, default=0.0)),
                ('fcm', models.CharField(db_index=True, max_length=512, null=True)),
            ],
            options={
                'db_table': 'driver',
                'managed': True,
            },
        ),
        migrations.CreateModel(
            name='Job',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('jname', models.CharField(db_index=True, max_length=100, unique=True)),
                ('jtype', models.CharField(choices=[('esse', 'EssentialServices'), ('prof', 'ProfessionalService'), ('hire', 'ForHire')], default='None', max_length=5)),
            ],
            options={
                'db_table': 'job',
                'managed': True,
            },
        ),
        migrations.CreateModel(
            name='Location',
            fields=[
                ('an', models.BigIntegerField(default=0, primary_key=True, serialize=False)),
                ('lat', models.FloatField()),
                ('lng', models.FloatField()),
                ('time', models.DateTimeField(auto_now=True)),
                ('kind', models.IntegerField(db_index=True, default=0)),
            ],
            options={
                'db_table': 'location',
                'ordering': ['lat', 'lng'],
                'managed': True,
            },
        ),
        migrations.CreateModel(
            name='Manager',
            fields=[
                ('an', models.BigIntegerField(primary_key=True, serialize=False)),
                ('pn', models.CharField(db_index=True, max_length=32)),
                ('auth', models.CharField(db_index=True, max_length=16)),
                ('pid', models.IntegerField(db_index=True, null=True)),
                ('dl', models.CharField(max_length=20, null=True)),
                ('name', models.CharField(db_index=True, max_length=64, null=True)),
                ('gdr', models.CharField(db_index=True, max_length=16, null=True)),
                ('age', models.IntegerField(db_index=True, null=True)),
                ('hs', models.CharField(max_length=50, null=True)),
            ],
            options={
                'db_table': 'manager',
                'managed': True,
            },
        ),
        migrations.CreateModel(
            name='Place',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('pn', models.CharField(db_index=True, max_length=64, unique=True)),
                ('lat', models.FloatField()),
                ('lng', models.FloatField()),
                ('alt', models.IntegerField()),
                ('wt', models.IntegerField()),
            ],
            options={
                'db_table': 'place',
                'managed': True,
            },
        ),
        migrations.CreateModel(
            name='Product',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('name', models.CharField(db_index=True, max_length=200, null=True)),
                ('type', models.CharField(choices=[('simple', 'simple'), ('grouped', 'grouped'), ('external', 'external'), ('variable', 'variable')], default='simple', max_length=10)),
                ('catalog_visibility', models.CharField(default='visible', max_length=10)),
                ('published', models.CharField(default='publish', max_length=10)),
                ('description', models.CharField(default='', max_length=2000, null=True)),
                ('short_description', models.CharField(default='', max_length=500, null=True)),
                ('regular_price', models.FloatField(db_index=True, default=0.0)),
                ('sale_price', models.FloatField(db_index=True, default=0.0)),
                ('stock_quantity', models.IntegerField(db_index=True, default=0)),
                ('backorders', models.CharField(default='yes', max_length=10)),
                ('categories', models.CharField(db_index=True, max_length=50, null=True)),
                ('weight', models.FloatField(db_index=True, default=0.0)),
                ('sold_individually', models.CharField(default='FALSE', max_length=10)),
                ('tags', models.CharField(max_length=50, null=True)),
                ('menu_order', models.IntegerField(db_index=True, default=1)),
                ('grouped', models.CharField(default='FALSE', max_length=10)),
                ('sku', models.CharField(db_index=True, max_length=200, null=True)),
                ('tax_status', models.CharField(default='taxable', max_length=10, null=True)),
                ('tax_class', models.FloatField(db_index=True, default=0.0)),
                ('purchase_note', models.CharField(default='Thanks :) kindly check the package for expiry details.', max_length=250, null=True)),
                ('upsell_ids', models.CharField(max_length=200, null=True)),
                ('cross_sell_ids', models.CharField(max_length=200, null=True)),
                ('parent_id', models.CharField(max_length=200, null=True)),
                ('low_stock_amount', models.SmallIntegerField(db_index=True, default=1)),
                ('images', models.CharField(default='https://cdn.business2community.com/wp-content/uploads/2014/01/product-coming-soon.jpg', max_length=250, null=True)),
                ('cost_price', models.FloatField(db_index=True, default=0.0)),
            ],
            options={
                'db_table': 'product',
                'managed': True,
            },
        ),
        migrations.CreateModel(
            name='Progress',
            fields=[
                ('tid', models.IntegerField(primary_key=True, serialize=False)),
                ('pct', models.IntegerField(db_index=True)),
            ],
            options={
                'db_table': 'progress',
                'managed': True,
            },
        ),
        migrations.CreateModel(
            name='Purchaser',
            fields=[
                ('an', models.BigIntegerField(primary_key=True, serialize=False)),
                ('pn', models.CharField(db_index=True, max_length=32)),
                ('auth', models.CharField(db_index=True, max_length=16)),
                ('pid', models.IntegerField(db_index=True, null=True)),
                ('dl', models.CharField(max_length=20, null=True)),
                ('name', models.CharField(db_index=True, max_length=64, null=True)),
                ('gdr', models.CharField(db_index=True, max_length=16, null=True)),
                ('age', models.IntegerField(db_index=True, null=True)),
                ('hs', models.CharField(max_length=50, null=True)),
                ('fcm', models.CharField(db_index=True, max_length=512, null=True)),
            ],
            options={
                'db_table': 'purchaser',
                'managed': True,
            },
        ),
        migrations.CreateModel(
            name='Rate',
            fields=[
                ('id', models.CharField(max_length=10, primary_key=True, serialize=False)),
                ('type', models.CharField(choices=[('RIDE', 'ride'), ('RENT', 'rental'), ('DELI', 'delivery'), ('NAN', 'null')], db_index=True, default='NAN', max_length=4)),
                ('rating', models.CharField(choices=[('attitude', 'attitude'), ('vehiclecon', 'vehiclecondition'), ('cleanliness', 'cleanliness'), ('other', ' other ')], db_index=True, default='OT', max_length=20)),
                ('money', models.FloatField(db_index=True, default=0.0)),
                ('rev', models.CharField(default='', max_length=280)),
                ('time', models.DateTimeField(auto_now=True, db_index=True)),
                ('dan', models.BigIntegerField(db_index=True, default=-1)),
            ],
            options={
                'db_table': 'rate',
                'managed': True,
            },
        ),
        migrations.CreateModel(
            name='Route',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('idx', models.IntegerField()),
                ('idy', models.IntegerField()),
                ('dist', models.IntegerField()),
            ],
            options={
                'db_table': 'route',
                'managed': True,
            },
        ),
        migrations.CreateModel(
            name='Servitor',
            fields=[
                ('an', models.BigIntegerField(primary_key=True, serialize=False)),
                ('pn', models.CharField(db_index=True, max_length=32)),
                ('auth', models.CharField(db_index=True, max_length=16)),
                ('pid', models.IntegerField(db_index=True, null=True)),
                ('dl', models.CharField(max_length=20, null=True)),
                ('name', models.CharField(db_index=True, max_length=64, null=True)),
                ('gdr', models.CharField(db_index=True, max_length=10, null=True)),
                ('age', models.IntegerField(db_index=True, null=True)),
                ('hs', models.CharField(max_length=50, null=True)),
                ('job1', models.CharField(default='', max_length=3)),
                ('job2', models.CharField(max_length=3, null=True)),
                ('job3', models.CharField(max_length=3, null=True)),
                ('job4', models.CharField(max_length=3, null=True)),
                ('job5', models.CharField(max_length=3, null=True)),
                ('wage', models.FloatField(db_index=True, default=0.0)),
                ('mark', models.FloatField(db_index=True, default=0.0)),
                ('bank', models.CharField(default='', max_length=300)),
            ],
            options={
                'db_table': 'servitor',
                'managed': True,
            },
        ),
        migrations.CreateModel(
            name='Supervisor',
            fields=[
                ('an', models.BigIntegerField(primary_key=True, serialize=False)),
                ('pn', models.CharField(db_index=True, max_length=32)),
                ('auth', models.CharField(db_index=True, max_length=16)),
                ('pid', models.IntegerField(db_index=True, null=True)),
                ('dl', models.CharField(max_length=20, null=True)),
                ('name', models.CharField(db_index=True, max_length=64, null=True)),
                ('gdr', models.CharField(db_index=True, max_length=16, null=True)),
                ('age', models.IntegerField(db_index=True, null=True)),
                ('hs', models.CharField(max_length=50, null=True)),
            ],
            options={
                'db_table': 'super',
                'managed': True,
            },
        ),
        migrations.CreateModel(
            name='Trip',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('st', models.CharField(choices=[('RQ', 'requested'), ('AS', 'assigned'), ('ST', 'started'), ('TR', 'terminated'), ('FN', 'finished'), ('PD', 'paid'), ('CN', 'cancelled'), ('DN', 'denied'), ('TO', 'timeout'), ('FL', 'failed')], db_index=True, default='RQ', max_length=2)),
                ('uan', models.BigIntegerField(db_index=True)),
                ('dan', models.BigIntegerField(db_index=True, default=-1)),
                ('van', models.BigIntegerField(db_index=True, default=-1)),
                ('rtime', models.DateTimeField(auto_now_add=True, db_index=True)),
                ('atime', models.DateTimeField(db_index=True, null=True)),
                ('stime', models.DateTimeField(db_index=True, null=True)),
                ('etime', models.DateTimeField(db_index=True, null=True)),
                ('srcid', models.IntegerField(db_index=True, default=1)),
                ('dstid', models.IntegerField(db_index=True, default=2)),
                ('srclat', models.FloatField(db_index=True, default=-1)),
                ('srclng', models.FloatField(db_index=True, default=-1)),
                ('dstlat', models.FloatField(db_index=True, default=-1)),
                ('dstlng', models.FloatField(db_index=True, default=-1)),
                ('srcname', models.CharField(db_index=True, default='', max_length=314)),
                ('dstname', models.CharField(db_index=True, default='', max_length=314)),
                ('rvtype', models.IntegerField(default=3, null=True)),
                ('npas', models.IntegerField(db_index=True, default=0)),
                ('rtype', models.CharField(choices=[('0', 0), ('1', 1)], db_index=True, default=2, max_length=10)),
                ('pmode', models.CharField(choices=[('CASH', 0), ('UPI', 1)], db_index=True, default=1, max_length=10)),
                ('hrs', models.IntegerField(db_index=True, default=0)),
                ('url', models.CharField(max_length=30, null=True)),
                ('htid', models.CharField(max_length=50, null=True)),
            ],
            options={
                'db_table': 'trip',
                'managed': True,
            },
        ),
        migrations.CreateModel(
            name='User',
            fields=[
                ('an', models.BigIntegerField(primary_key=True, serialize=False)),
                ('pn', models.CharField(db_index=True, max_length=32)),
                ('auth', models.CharField(db_index=True, max_length=16)),
                ('pid', models.IntegerField(db_index=True, null=True)),
                ('tid', models.IntegerField(db_index=True, default=-1)),
                ('did', models.CharField(db_index=True, default='', max_length=11)),
                ('name', models.CharField(db_index=True, max_length=64, null=True)),
                ('gdr', models.CharField(db_index=True, max_length=16, null=True)),
                ('age', models.SmallIntegerField(db_index=True, null=True)),
                ('dl', models.CharField(max_length=20, null=True)),
                ('hs', models.CharField(db_index=True, max_length=50, null=True)),
                ('mark', models.FloatField(db_index=True, default=0.0)),
                ('adhar', models.BigIntegerField(db_index=True, null=True)),
                ('email', models.CharField(db_index=True, max_length=100, null=True)),
                ('fcm', models.CharField(db_index=True, max_length=512, null=True)),
            ],
            options={
                'db_table': 'user',
                'managed': True,
            },
        ),
        migrations.CreateModel(
            name='Vehicle',
            fields=[
                ('an', models.BigIntegerField(primary_key=True, serialize=False)),
                ('tid', models.BigIntegerField(db_index=True, default=-1)),
                ('dan', models.BigIntegerField(db_index=True, default=-1)),
                ('regn', models.CharField(db_index=True, max_length=16)),
                ('dist', models.IntegerField(null=True)),
                ('hrs', models.FloatField(null=True)),
                ('pid', models.IntegerField(db_index=True, null=True)),
                ('vtype', models.IntegerField(default=3, null=True)),
                ('mark', models.FloatField(db_index=True, default=0.0)),
            ],
            options={
                'db_table': 'vehicle',
                'managed': True,
            },
        ),
        migrations.AddIndex(
            model_name='route',
            index=models.Index(fields=['idx', 'idy'], name='route_idx_f49387_idx'),
        ),
    ]
