# imports 
import os
import sys
import time
import json
import urllib.request
from woocommerce import API
from pymysql.err import IntegrityError

sys.path.append(os.path.dirname(os.path.abspath(__file__)) + '/../' )
sys.path.append(os.path.dirname(os.path.abspath(__file__)) + '/../api' )

os.environ.setdefault('ZP_DB_NAME', 'zp')
os.environ.setdefault('ZP_DB_USER', 'zpadmin')
os.environ.setdefault('ZP_DB_PASSWD', 'appleelppa')
os.environ.setdefault('ZP_DB_HOST', 'localhost')
os.environ.setdefault('ZP_DB_PORT', '3306')

os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'api.settings')


def pros(m,n):
        wcapi = API(url="https://zippe.in", consumer_key="ck_97e691622c4bd5e13fb7b18cbb266c8277257372", consumer_secret="cs_63badebe75887e2f94142f9484d06f257194e2c3", version="wc/v3")
        ret = wcapi.get("products/?page="+ str(m) + "&per_page=" + str(n))
        print(ret.status_code)
        resp = {}
        for i in ret.json():
            print(i['id'], i['name'], i['sku'])
            resp[i['sku']] = i['id']
        return resp
        
        
USAGE = \
'''
Usage shopSync.py m n sleep 
    m : number of pages to go in woocommerce
    n : number of products to be retrieved per page
    sleep: optional number of seconds (float) to sleep between actions (default 1.0)
'''

def main():
    if len(sys.argv) < 2:
        print(USAGE)
        sys.exit(-1)

    delay = float(sys.argv[3])
    m = int(sys.argv[1])
    n = int(sys.argv[2])
    print (m, n, delay)
    ret = {}
    for i in range(1,m+1):
        resp = pros(str(i), str(n))
        ret.update(resp)
        time.sleep(delay)

    #print(ret)
    status = 'false'
    from django.db import connection
    cursor = connection.cursor()
    for i in ret:
        try :
        #qsNextHubs = Product.objects.raw('update product set id = %s where sku = %s;', [ret[i], i])
            cursor.execute('update product set id = %s where sku = %s;', [ret[i],i])
        except IntegrityError:
            print(' Product with sku : %s didn\'t get updated' % (i))
        status = 'true'

if __name__ == '__main__':
    main()
