#!/bin/bash
echo "Hello Duniya!"
echo " usage bash polling.sh 5 180 admin-refresh"
args=( "$@" )
echo "${args[@]}"
#printf '%s\n' "${args[@]}"
len=${#args[@]}
echo "len : $len"
#TODO make it generic(for simul), right now it only works for adminMethods
time_step=${args[0]}
num_retries=${args[1]}
api_name=${args[2]}
auth_key=${args[3]}
url='http://127.0.0.1:9999/'

iter=1
while [ $iter -lt $num_retries ]
do
	sleep $time_step
	clear
	echo "$iter seconds until next high :p"
	# python3 APIcaller.py $api_name $auth_key
  curl  -X POST -H "Content-Type: application/json" -H "Accept-Charset: UTF-8" -d '{"auth":"437468756c68752066687461676e"}' ${url}${api_name} # > polling-${iter}.out
	iter=$(( $iter + 1 ))
done
