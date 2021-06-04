#!/bin/bash
echo "Looking for incoming rental requests"
curl -X POST https://api.zippe.in:8090/sup-rent-check -d '{"auth":"sauth01"}'

echo "\n"
echo "Enter the trip id for assignment"
read tid
echo 'tid is: '$tid

echo "Enter the vehicle adhar number for assignment"
read veh
echo 'van is: '$van

curl -X POST https://api.zippe.in:8090/sup-rent-vehicle-assign -d '{"auth":"sauth01","van":"'$veh'","tid":"'$tid'"}'

echo "\n"
echo "Enter the OTP"
read otp
echo 'otp is: '$otp
curl -X POST https://api.zippe.in:8090/sup-rent-start -d '{"auth":"sauth01","tid":"'$tid'","otp":"'$otp'"}'


echo "\n"
echo "Do you want to END the rental(y/n)?"
read end

if [[ "$end" -eq 'y' ]]
	then
		curl -X POST https://api.zippe.in:8090/sup-rent-end -d '{"auth":"sauth01","tid":"'$tid'"}'
fi

echo "\n"
echo "Do you want to RETIRE the rental(y/n)?"
read retire

if [[ "$retire" -eq 'y' ]]
	then
		curl -X POST https://api.zippe.in:8090/sup-rent-retire -d '{"auth":"sauth01","tid":"'$tid'"}'

fi

