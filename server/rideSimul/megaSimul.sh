#!/bin/bash
echo "USAGE :  bash megaSimul.sh output_file_name N_number_of_drivers M_number_of_users O_number_of_vehicles sleep_time"
# here N is the number of users, M is the number of users

##############################

args=( "$@" )
echo "${args[@]}"
printf '%s\n' "${args[@]}"
len=${#args[@]}
#url='http://127.0.0.1:9999/' # python3 manage.py runserver 9999 #Local server
url='https://api.villageapps.in:8090/'
log_file=${args[0]}          # log file
user_count=${args[1]}        # Number of User for DB
driver_count=${args[2]}      # Number of Driver for DB
vehicle_count=${args[3]}     # Number of Vehicle for DB
time_step=${args[4]}         # for sleep

#############################

ZP_DB_USER='zpadmin'
ZP_DB_NAME='zp'
ZP_DB_PASSWD='appleelppa'

##############################

touch "${log_file}"
echo "len of cmd line args: $len, log_file: $log_file,  user count: $user_count, driver count : $driver_count "
echo "Enter the Matrix!" | tee "${log_file}"
# pkill -f python3 # Noob

export ZP_DB_PASSWD=$ZP_DB_PASSWD
python3 /home/dex/work/zp/server/manage.py runserver 9999 &
sleep 10

####################################

echo "#################### DOING ####################" | tee -a "$log_file"
echo "deleting users and drivers..."
#echo "delete from user; "| mysql --user=$ZP_DB_USER --password=$ZP_DB_PASSWD $ZP_DB_NAME
#echo "delete from driver; "| mysql --user=$ZP_DB_USER --password=$ZP_DB_PASSWD $ZP_DB_NAME
#echo "delete from vehicle;" | mysql --user=$ZP_DB_USER --password=$ZP_DB_PASSWD $ZP_DB_NAME
echo "no need as already inserted!"

####################################

echo "creating $user_count users..."
#user_iter=1
#while [ $user_iter -le $user_count ]
#  do
#    echo "INSERT INTO user(an, pn, auth, tid, pid, age, gdr, name, hs, dl, did, mark) VALUES('$user_iter', '97000000$user_iter', 'auth$user_iter', -1, FLOOR(RAND()*(10-5+1)+1),  25, 'MALE', 'John $user_iter',  'UK', 'UK-1234567890$user_iter', '-1', '0.0');"
#    ((user_iter++))
#  done | mysql --user=$ZP_DB_USER --password=$ZP_DB_PASSWD $ZP_DB_NAME
echo "already created $user_count users."

####################################

echo "creating $driver_count drivers..."
#driver_iter=1
#while [ $driver_iter -le $driver_count ]
#  do
#    echo "INSERT INTO driver (an, pn, auth, dl, mode, tid, pid, name, age, gdr, hs, mark, van) VALUES('$driver_iter', '98000000$driver_iter', 'dauth$driver_iter', 'UK01234567891$driver_iter', 'OF', -1,FLOOR(RAND()*(10-5+1)+1), 'driver0$driver_iter', 30, 'm', 'uk', '0.0', '-1');"
#    ((driver_iter++))
#  done | mysql --user=$ZP_DB_USER --password=$ZP_DB_PASSWD $ZP_DB_NAME
echo "already created $driver_count drivers."

#####################################

echo "creating $vehicle_count vechicles..."
#vehicle_iter=1
#while [ $vehicle_iter -le $vehicle_count ]
#  do
#    echo "INSERT INTO vehicle(regn, dist, hrs, pid, vtype, an, tid, dan, mark) VALUES('reg$vehicle_iter', '0', '0', FLOOR( 1 + RAND() * 7 ),  FLOOR( 1 + RAND() * 3 ), '20$vehicle_iter',  '-1' '-1', '0.0');"
#    ((vehicle_iter++))
#  done | mysql --user=$ZP_DB_USER --password=$ZP_DB_PASSWD $ZP_DB_NAME
echo "already created $vehicle_count vehicles."

#####################################

#parallel -j0 ::: a.sh b.sh || echo one or both of them failed

echo "Running admin..."
#nohup bash polling.sh 1 180 admin-refresh &
python3 adminSim.py 10 &

#####################################

echo "Running $driver_count drivers..."
driver_iter=1
while [ $driver_iter -le $driver_count ]
  do
    python3 driverSim2.py $(( $driver_iter -1 )) $((1 + RANDOM % 6)) | tee -a "$log_file" &
    sleep 5
    ((driver_iter++))
  done
echo "RAN $driver_count drivers."

#####################################

echo "Running $user_count drivers..."
user_iter=1
while [ $user_iter -le $user_count ]
  do
    python3 userSim2.py $(( $user_iter -1 )) $((1 + RANDOM % 6)) $time_step | tee -a "$log_file" &
    sleep 5
    ((user_iter++))
  done
echo "RAN $user_count users."

echo "#################### DONE #####################" | tee -a "$log_file"
echo " To kill all run \"pkill -f python3\"... Sayonara"
