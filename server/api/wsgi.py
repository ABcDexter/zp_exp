"""
WSGI config for api project.

It exposes the WSGI callable as a module-level variable named ``application``.

For more information on this file, see
https://docs.djangoproject.com/en/3.0/howto/deployment/wsgi/
"""

import os
import sys

from django.core.wsgi import get_wsgi_application

sys.path.append(os.path.dirname(os.path.abspath(__file__)) + '/../' )
sys.path.append(os.path.dirname(os.path.abspath(__file__)) + '/../api' )

os.environ.setdefault('ZP_DB_NAME', 'zp')
os.environ.setdefault('ZP_DB_USER', 'zpadmin')
os.environ.setdefault('ZP_DB_PASSWD', 'appleelppa')
os.environ.setdefault('ZP_DB_HOST', 'localhost')
os.environ.setdefault('ZP_DB_PORT', '3306')

os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'api.settings')

application = get_wsgi_application()
