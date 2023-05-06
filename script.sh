#!/bin/bash

servers=$3

gnome-terminal --tab --title="Gateway" -e "java -jar gateway/build/libs/gateway-1.0-SNAPSHOT.jar" &
sleep 5

for ((i=0; i<=servers; i++)); do
  gnome-terminal --tab --title="Server $i" -e "java -jar server/build/libs/server-1.0-SNAPSHOT.jar" &
done

sleep 5
gnome-terminal --tab --title="Client" -e "java -jar client/build/libs/client-1.0-SNAPSHOT.jar" &
