# Generated by Django 3.0.6 on 2020-07-06 11:51

from django.db import migrations


class Migration(migrations.Migration):

    dependencies = [
        ('zp', '0007_user_did'),
    ]

    operations = [
        migrations.RenameField(
            model_name='agent',
            old_name='tid',
            new_name='did',
        ),
    ]
