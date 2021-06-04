import pymysql
pymysql.version_info = (1, 3, 13, "final", 0)
pymysql.install_as_MySQLdb()
pymysql.connect(db='zp', user='zpadmin', passwd='appleelppa', host='localhost', port=3306)
