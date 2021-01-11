import pymysql
pymysql.version_info = (1, 4, 2, "final", 0)
pymysql.install_as_MySQLdb()
try :
    pymysql.connect(db='zp', user='zpadmin', passwd='appleelppa', host='localhost', port=3306)
    print(pymysql)
except ValueError:
    print("DB Connecttion error")
