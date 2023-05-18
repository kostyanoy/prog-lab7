#!/bin/bash

servers=$1

gnome-terminal --tab --title="Gateway" -- java -jar gateway/build/libs/gateway-1.0-SNAPSHOT.jar &
sleep 1

for ((i=0; i<servers; i++)); do
  gnome-terminal --tab --title="Server $i" -- java -jar server/build/libs/server-1.0-SNAPSHOT.jar &
  sleep 1
done

sleep 1
gnome-terminal --tab --title="Client" -- java -jar client/build/libs/client-1.0-SNAPSHOT.jar &
