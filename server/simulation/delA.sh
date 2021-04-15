#!/bin/bash
echo "Looking for incoming delivery requests"
curl -X POST https://api.zippe.in:8090/agent-delivery-check -d '{"auth":"Uh5Lb0G4"}'

echo "\n"
echo "Enter the delivery id for assignment"
read did
echo 'did is: '$did


curl -X POST curl -X POST https://api.zippe.in:8090/agent-delivery-accept \ -d '{"auth":"Uh5Lb0G4", "did":"'$did'"}'


echo "\n"
echo "Agent is reaching"
curl -X POST curl -X POST https://api.zippe.in:8090/agent-delivery-reached \ -d '{"auth":"Uh5Lb0G4"}'

echo "\n"
echo "Enter the OTP"
read otp
echo 'otp is: '$otp
curl -X POST https://api.zippe.in:8090/agent-delivery-start -d '{"auth":"Uh5Lb0G4","did":"'$did'","otp":"'$otp'"}'


echo "Do you want to END the delivery(y/n)?"
read end

if [[ "$end" -eq 'y' ]]
	then
		curl -X POST https://api.zippe.in:8090/agent-delivery-done -d '{"auth":"Uh5Lb0G4","did":"'$did'"}'
fi