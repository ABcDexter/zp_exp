# Generated by Django 3.0.6 on 2020-07-29 10:34

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('zp', '0024_auto_20200728_1151'),
    ]

    operations = [
        migrations.RemoveField(
            model_name='delivery',
            name='details',
        ),
        migrations.AddField(
            model_name='delivery',
            name='det',
            field=models.CharField(max_length=150, null=True),
        ),
        migrations.AddField(
            model_name='delivery',
            name='droptime',
            field=models.DateTimeField(db_index=True, null=True),
        ),
        migrations.AddField(
            model_name='delivery',
            name='dstdet',
            field=models.CharField(max_length=150, null=True),
        ),
        migrations.AddField(
            model_name='delivery',
            name='picktime',
            field=models.DateTimeField(db_index=True, null=True),
        ),
        migrations.AddField(
            model_name='delivery',
            name='srcdet',
            field=models.CharField(max_length=150, null=True),
        ),
    ]