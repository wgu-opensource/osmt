#!/bin/bash

set -eu

main(){
  echo "Stopping spring app initialized..."

  local appPID="$(jps -l | grep edu.wgu.osmt.ApplicationKt | awk '{print $1}')"
  echo "Got app PID ${appPID}"

  kill -SIGINT ${appPID}
  echo "Spring app has been stopped"
  #Need to add an error catcher
}

main
