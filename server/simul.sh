#!/bin/bash
echo "usage bash simulator.sh 5 10 simulation-007.out  num_users num_drivers"
#############################
args=( "$@" )
echo "${args[@]}"
printf '%s\n' "${args[@]}"
len=${#args[@]}
url='http://127.0.0.1:9999/'# python3 manage.py runserver 9080
file_name=${args[2]}        # all the data will be sent to this file
user_count=${args[3]}       # Number of User from DB
driver_count=${args[4]}     # Number of Driver from DB
#############################
echo "${file_name}"
touch "${file_name}"
echo "len of cmd line args: $len"
echo "Enter the Matrix!"  >> "${file_name}"
#############################

echo "echoing API NAMES" >> "$file_name"
echo "AUTH APIs" >> "$file_name"
one=$((1))
auth_update_location='auth-update-location'
echo "$one : $auth_update_location" >> "$file_name"
#'''
#THIS IS BEING POLLED BY USER AND DRIVER
#auth of User  or Driver
#---
#return prev1 prev2 curr next1 next2
#'''

echo "USER APIs" >> "$file_name"
two=$((2))
user_request_ride="user-request-ride"
echo "$two : $user_request_ride" >> "$file_name"
#'''
#auth of User
#npas [1-3]
#srcid [1-6]
#dstid [1-6]
#rtype [1-2]
#vtype [1-4]
#pmode [1-2]
#----
#returns [count] of driver
#'''

three=$((3))
user_trip_status="user-trip-status"
echo "$three : $user_trip_status" >> "$file_name"
#'''
#auth of User
#---
#returns status: otp, dname: driverName, dphone: driverPhone, vnum: trip.vid})
#'''

four=$((4))
user_cancel_ride="user-cancel-ride"
echo "$four : $user_cancel_ride" >> "$file_name"
#'''
#auth of User
#---
#returns {}
#'''

echo "DRIVER APIs" >> "$file_name"
five=$((5))
driver_check_trips="driver-check-trips"
echo "$five : $driver_check_trips"  >> "$file_name"
#'''
#auth of Driver
#---
#return status True False and tid
#'''

six=$((6))
driver_accept_ride="driver-accept-ride"
echo "$six : $driver_accept_ride"  >> "$file_name"
#'''
#auth of Driver
#tid trip id of Trip [1-tripsize]
#---
#return status True or False
#'''

seven=$((7))
driver_trip_status="driver-trip-status"
echo "$seven : $driver_trip_status"  >> "$file_name"
#'''
#auth of Driver
#---
#return
#status True or False
#'''

eight=$((8))
driver_cancel="driver-cancel"
echo "$eight : $driver_cancel"  >> "$file_name"
#'''
#auth of Driver
#---
#returns {}
#'''

nine=$((9))
driver_start_trip="driver-start-trip"
echo "$nine : $driver_start_trip"  >> "$file_name"
#'''
#auth Driver
#otp number
#---
#returns status True or False
#'''

ten=$((10))
driver_end_trip="driver-end-trip"
echo "$ten : $driver_end_trip"  >> "$file_name"
#'''
#auth Driver
#---
#returns status: status, price: actualPrice
#'''

echo "Those were the 10 APIs"  >> "$file_name"
#############################

iter=1
otp=1024
user_prefix="CcXYVOa"
driver_prefix="CcXYVOb"

time_step=${args[0]}       # for sleep
total_loops=${args[1]}     # for total number of time our loop will run
echo "#################### DOING ####################"
echo "#################### DOING ####################" >> "$file_name"
while [ $iter -lt $total_loops ]
do
    userOrDriver=$((1 + RANDOM % 2))
    echo "sleeping..."  >> "$file_name"
    sleep $time_step
    echo "waking up!"  >> "$file_name"
    if [ $userOrDriver -gt 1 ]
    then
        randUser=$((1 + RANDOM % 99))
        auth_key_user=${user_prefix}${randUser}
        echo "user sending Location api hit : #UPDATING LOCATION"  >> "$file_name"
			  api_name=$auth_update_location
				echo "calling : $api_name"  >> "$file_name"
				#response=$(eval python3 APIcaller.py $file_name $api_name $auth_key_user lat 29.5 lng 79.5)
        #curr=$(eval python3 -c "import sys, json; curr=json.loads($((response)))['curr']; print(curr)")
        curl  -X POST -H "Content-Type: application/json" -H "Accept-Charset: UTF-8" -d '{"auth":"'${auth_key_user}'","lat":"29.9","lng":"79.9"}' ${url}${api_name} > curl-user-${api_name}.json
        response=$(cat curl-user-${api_name}.json)
        echo "Location sent. response : $response"  >> "$file_name"
        sleep 1

      random=$((2 + RANDOM % 3))
      echo "User $random step going on..."  >> "$file_name"

      case "$random" in
        2)

        echo -n "api hit : $user_request_ride ... "  >> "$file_name"
        npas=$((1 + RANDOM % 3)) #[1-3]
        srcid=$((1 + RANDOM % 6)) #[1-6]
        dstid=$((1 + RANDOM % 6)) #[1-6]
        rtype=$((1 + RANDOM % 2)) #[1-2]
        vtype=$((1 + RANDOM % 4)) #[1-4]
        pmode=$((1 + RANDOM % 2)) #[1-2]

			  api_name=$user_request_ride
				echo "calling : $api_name"  >> "$file_name"
				curl  -X POST -H "Content-Type: application/json" -H "Accept-Charset: UTF-8" -d '{"auth":"'${auth_key_user}'","npas":"'${npas}'","srcid":"'${srcid}'","dstid":"'${dstid}'","rtype":"'${rtype}'","vtype":"'${vtype}'","pmode":"'${pmode}'"}' ${url}${api_name} > curl-${api_name}.json
        response=$(cat curl-${api_name}.json)
        echo "Call done. response : $response"  >> "$file_name"

        ;;

        3)

        echo -n " api hit :$user_trip_status ... "  >> "$file_name"
			  api_name=$user_trip_status
				echo "calling : $api_name"  >> "$file_name"
				curl  -X POST -H "Content-Type: application/json" -H "Accept-Charset: UTF-8" -d '{"auth":"'${auth_key_user}'"}' ${url}${api_name} | python3 -c "import sys, json; print(json.load(sys.stdin)['otp'])" > curl-${api_name}.json
        otp=$(cat curl-${api_name}.json)
        echo "OTP : $otp"  >> "$file_name"
        response=$(cat curl-${api_name}.json)
        echo "Call done. response : $response"  >> "$file_name"

        ;;

        4)

        echo -n " api hit : $user_cancel_ride ... "  >> "$file_name"
			  api_name=$user_cancel_ride
				echo "calling : $api_name"  >> "$file_name"
				curl  -X POST -H "Content-Type: application/json" -H "Accept-Charset: UTF-8" -d '{"auth":"'${auth_key_user}'"}' ${url}${api_name} > curl-${api_name}.json
        response=$(cat curl-${api_name}.json)
        echo "Call done. response : $response"  >> "$file_name"

        ;;

        *)
        echo "404 $random API not found"  >> "$file_name"
        ;;
      esac


    else
      random=$((4 + RANDOM % 7))
       echo "random : $random"  >> "$file_name"
       echo "Driver $random step going on..."  >> "$file_name"
        randDriver=$((1 + RANDOM % 7)) #$driver_count))
        auth_key_driver=${driver_prefix}${randDriver}


        echo "driver sending Location api hit : #UPDATING LOCATION"  >> "$file_name"
			  api_name=$auth_update_location
				echo "calling : $api_name"  >> "$file_name"
				curr=1 #$((
        curl  -X POST -H "Content-Type: application/json" -H "Accept-Charset: UTF-8" -d '{"auth":"'${auth_key_driver}'","lat":"29.9","lng":"79.9"}' ${url}${api_name} > curl-driver-${api_name}.json
        echo "CURR : $curr"  >> "$file_name"
        sleep 1
        response=$(cat curl-driver-${api_name}.json)
        echo "Location sent. response : $response"  >> "$file_name"

       case "$random" in
        5)

        echo -n "api hit : $driver_check_trips ... "  >> "$file_name"
        api_name=$driver_check_trips
				echo "calling : $api_name"  >> "$file_name"
			  curl  -X POST -H "Content-Type: application/json" -H "Accept-Charset: UTF-8" -d '{"auth":"'${auth_key_driver}'"}' ${url}${api_name} > curl-${api_name}.json
			  response=$(cat curl-${api_name}.json)
        echo "Call done. response : $response"  >> "$file_name"

        ;;

        6)

        echo -n " api hit :$driver_accept_ride ... "  >> "$file_name"
			  api_name=$driver_accept_ride
				echo "calling : $api_name"  >> "$file_name"
				randChance=$((1 + RANDOM % 2))  # 50 % probability of accepting ride
        if [ $randChance -lt 0 ]
        then
          tid=$((2048 + RANDOM % 9999))
        fi
        echo "trip by DRIVER : $tid"  >> "$file_name"
        curl  -X POST -H "Content-Type: application/json" -H "Accept-Charset: UTF-8" -d '{"auth":"'${auth_key_driver}'","tid":"'${tid}'"}' ${url}${api_name} > curl-${api_name}.json
        response=$(cat curl-${api_name}.json)
        echo "Call done. response : $response"  >> "$file_name"

        ;;

        7)

        echo -n " api hit : $driver_trip_status ... "  >> "$file_name"
			  api_name=$driver_trip_status
				echo "calling : $api_name"  >> "$file_name"
				curl  -X POST -H "Content-Type: application/json" -H "Accept-Charset: UTF-8" -d '{"auth":"'${auth_key_driver}'"}' ${url}${api_name} > curl-${api_name}.json
        response=$(cat curl-${api_name}.json)
        echo "Call done. response : $response"  >> "$file_name"

        ;;


        8)

        echo -n " api hit : $driver_cancel ... "  >> "$file_name"
			  api_name=$driver_cancel
				echo "calling : $api_name"  >> "$file_name"
				curl  -X POST -H "Content-Type: application/json" -H "Accept-Charset: UTF-8" -d '{"auth":"'${auth_key_driver}'"}' ${url}${api_name} > curl-${api_name}.json
        response=$(cat curl-${api_name}.json)
        echo "Call done. response : $response"  >> "$file_name"

        ;;

        9)

        echo -n " api hit : $driver_start_trip ... "  >> "$file_name"
			  api_name=$driver_start_trip
				echo "calling : $api_name"  >> "$file_name"
				randChance=$((1 + RANDOM % 3))  # 66.67% probability of typing right OTP
        if [ $randChance -lt 1 ]
        then
          otp=$((1000 + RANDOM % 8999))
        fi
        echo "OTP to DRIVER : $otp"  >> "$file_name"
        curl  -X POST -H "Content-Type: application/json" -H "Accept-Charset: UTF-8" -d '{"auth":"'${auth_key_driver}'","otp":"'${otp}'"}' ${url}${api_name} > curl-${api_name}.json
        response=$(cat curl-${api_name}.json)
        echo "Call done. response : $response"  >> "$file_name"

        ;;

        10)

        echo -n " api hit : $driver_end_trip ... "  >> "$file_name"
			  api_name=$driver_end_trip
				echo "calling : $api_name"  >> "$file_name"
				curl  -X POST -H "Content-Type: application/json" -H "Accept-Charset: UTF-8" -d '{"auth":"'${auth_key_driver}'"}' ${url}${api_name} > curl-${api_name}.json
        response=$(cat curl-${api_name}.json)
        echo "Call done. response : $response"  >> "$file_name"

        ;;
        *)
        echo "404 $random ...  API not found"  >> "$file_name"
        ;;
      esac

    echo "$iter seconds until next time..."  >> "$file_name"
    fi
    iter=$(( $iter + 1 ))
done
echo "#################### DONE ####################"  >> "$file_name"
echo "#################### DONE ####################"