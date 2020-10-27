#!/bin/bash

echo "Enter the Trip id"
read tid

echo "\n"
echo "Enter the OTP"
read otp

touch otp.$tid
echo '{"otp":'$otp'}' >  otp.$tid


echo "\n"
echo "Enter the MONEY"
read money

touch money.$tid
echo '{"payment":'$money'}' >  money.$tid

