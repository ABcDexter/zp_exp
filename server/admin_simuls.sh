#!/bin/sh
#source ~/.bash_profile
#comm1= "sudo service apache2 restart"
#comm2= "sudo /etc/init.d/mysql restart"
echo Running all adminSimuls infinitely
while true
do
  echo Running as $(whoami)
  echo command ran at $(date)

  echo Running RIDE admin
  sudo nohup python3 /srv/zp/server/rideSimul/adminSim.py 10 > nohup-zp_new.out 2>&1 &
  sleep 300
  
  echo Running RENT admin 
  sudo nohup python3 /srv/zp/server/rentSimul/adminSim.py 20 > nohup-zp_new.out 2>&1 &
  sleep 300
  
  echo Running DELIVERY admin
  sudo nohup python3 /srv/zp/server/rentSimul/adminSim.py 30 > nohup-zp_new.out 2>&1 & 
  sleep 300
done

# run via nohup as above using bash
