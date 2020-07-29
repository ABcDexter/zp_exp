"""
Django settings for api project.

Generated by 'django-admin startproject' using Django 3.0.4.

For more information on this file, see
https://docs.djangoproject.com/en/3.0/topics/settings/

For the full list of settings and their values, see
https://docs.djangoproject.com/en/3.0/ref/settings/
"""

import os

# Build paths inside the project like this: os.path.join(BASE_DIR, ...)
BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))


# Quick-start development settings - unsuitable for production
# See https://docs.djangoproject.com/en/3.0/howto/deployment/checklist/

# SECURITY WARNING: keep the secret key used in production secret!
SECRET_KEY = '+8^8@0w0p1)6yr-g)!3l04&qp-ggjwz@9f_-fvpm_qta0g&36r'

# SECURITY WARNING: don't run with debug turned on in production!
DEBUG = True

ALLOWED_HOSTS = ["127.0.0.1", "localhost", "api.villageapps.in", "8ee0edfb6206.ngrok.io", "159.65.144.72","86d9a981a121.ngrok.io" ] # ngrok http http://127.0.0.1:9999


AADHAAR_DIR = '/srv/data/aadhaar/'
DL_DIR = '/srv/data/dl/'
GOOGLE_APPLICATION_CREDENTIALS="/srv/zp/server/google-cloud/MyFirstProject.json"
GOOGLE_MAPS_KEY = "AIzaSyBMFQPIDHSPfdiQqjX2ieF4l-qQB2tB61c"
GOOGLE_PLACES_KEY = "AIzaSyATYNx4G8xXuwss9a_W4owqGgFTeRLiZX8"
DB_FILE_PATH  = '/srv/data/db'
ADMIN_AUTH    = '437468756c68752066687461676e'
M_PER_DEG_LNG = 97220.765  # These values are specific to fleet location
M_PER_DEG_LAT = 110839.613
DSQUARE_THRESH = 2500 # 50*50

RIDE_RQ_TIMEOUT = 180 # 3 minutes
RIDE_AS_TIMEOUT = 300 # 5 mintues
LOG_DIR = '/srv/data/logs/'
#DRIVER_OF_TIMEOUT = 240

RENT_RQ_TIMEOUT = 1800  # 30 mins
RENT_AS_TIMEOUT = 3600  # 60 minutes

DEL_SC_TIMEOUT = 900   # 15 minutes
DEL_AS_TIMEOUT = 1800  # 30 minutes
# Application definition

INSTALLED_APPS = [
    'zp.apps.ZpConfig',
    'django.contrib.admin',
    'django.contrib.auth',
    'django.contrib.contenttypes',
    'django.contrib.sessions',
    'django.contrib.messages',
    'django.contrib.staticfiles',
    'corsheaders'   
]

MIDDLEWARE = [
    'corsheaders.middleware.CorsMiddleware',
    'django.middleware.security.SecurityMiddleware',
    'django.contrib.sessions.middleware.SessionMiddleware',
    'django.middleware.common.CommonMiddleware',
    'django.middleware.csrf.CsrfViewMiddleware',
    'django.contrib.auth.middleware.AuthenticationMiddleware',
    'django.contrib.messages.middleware.MessageMiddleware',
    'django.middleware.clickjacking.XFrameOptionsMiddleware',
]

ROOT_URLCONF = 'api.urls'

TEMPLATES = [
    {
        'BACKEND': 'django.template.backends.django.DjangoTemplates',
        'DIRS': [],
        'APP_DIRS': True,
        'OPTIONS': {
            'context_processors': [
                'django.template.context_processors.debug',
                'django.template.context_processors.request',
                'django.contrib.auth.context_processors.auth',
                'django.contrib.messages.context_processors.messages',
            ],
        },
    },
]

WSGI_APPLICATION = 'api.wsgi.application'

#CORS variables
CORS_ORIGIN_ALLOW_ALL = True

# Database
# https://docs.djangoproject.com/en/3.0/ref/settings/#databases

DATABASES = {
#    'sqlite': {
#        'ENGINE': 'django.db.backends.sqlite3',
#        'NAME': os.path.join(DB_FILE_PATH, 'zp.sqlite3'),
#    },
    # Setup mysql doin
    'default': {
        'ENGINE': 'django.db.backends.mysql',
        'NAME': os.environ.get('ZP_DB_NAME'),
        'USER': os.environ.get('ZP_DB_USER'),
        'PASSWORD': os.environ.get('ZP_DB_PASSWD'),
        'HOST': 'localhost', #os.environ.get('ZP_DB_HOST'),
        'PORT': '3306' #os.environ.get('ZP_DB_PORT')
    }
}

# Needed for SQLite concurrency
DATABASE_OPTIONS = {'timeout': 60}

# Password validation
# https://docs.djangoproject.com/en/3.0/ref/settings/#auth-password-validators

AUTH_PASSWORD_VALIDATORS = [
    {
        'NAME': 'django.contrib.auth.password_validation.UserAttributeSimilarityValidator',
    },
    {
        'NAME': 'django.contrib.auth.password_validation.MinimumLengthValidator',
    },
    {
        'NAME': 'django.contrib.auth.password_validation.CommonPasswordValidator',
    },
    {
        'NAME': 'django.contrib.auth.password_validation.NumericPasswordValidator',
    },
]


# Internationalization
# https://docs.djangoproject.com/en/3.0/topics/i18n/

LANGUAGE_CODE = 'en-us'
TIME_ZONE = 'UTC' #Asia/Kolkata
USE_I18N = True
USE_L10N = True
USE_TZ = True

# Static files (CSS, JavaScript, Images)
# https://docs.djangoproject.com/en/3.0/howto/static-files/

STATIC_URL = '/static/'

LOGGING = {
    'version': 1,
    'disable_existing_loggers': True,
    'filters': {
        'require_debug_false': {
            '()': 'django.utils.log.RequireDebugFalse'
        }
    },
    'handlers': {
        'console': {
            'level': 'DEBUG',
            'class': 'logging.StreamHandler',
        },
    },
    'loggers': {
        'django.request': {
            'handlers': ['console'],
            'level': 'DEBUG',
            'propagate': False,
        },
        'zp': {
            'handlers': ['console'],
            'level': 'DEBUG',
            'propagate': False,
        },
    }
}
