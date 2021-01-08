# Generated by Django 3.1.5 on 2021-01-08 06:49

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('zp', '0029_auto_20200810_1140'),
    ]

    operations = [
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
            ],
            options={
                'db_table': 'purchaser',
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
                ('gdr', models.CharField(db_index=True, max_length=16, null=True)),
                ('age', models.IntegerField(db_index=True, null=True)),
                ('hs', models.CharField(max_length=50, null=True)),
                ('job', models.CharField(max_length=50, null=True)),
                ('wage', models.FloatField(db_index=True, default=0.0)),
            ],
            options={
                'db_table': 'servitor',
                'managed': True,
            },
        ),
        migrations.AddField(
            model_name='rate',
            name='dan',
            field=models.BigIntegerField(db_index=True, default=-1),
        ),
        migrations.AddField(
            model_name='rate',
            name='time',
            field=models.DateTimeField(auto_now=True, db_index=True),
        ),
        migrations.AddField(
            model_name='trip',
            name='dstname',
            field=models.CharField(db_index=True, default='', max_length=314),
        ),
        migrations.AddField(
            model_name='trip',
            name='srcname',
            field=models.CharField(db_index=True, default='', max_length=314),
        ),
    ]
