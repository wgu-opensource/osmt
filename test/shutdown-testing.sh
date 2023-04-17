#!/bin/bash

set -eu

declare appPID

_get_app_pid() {
  appPID=($(jps | grep ApplicationKt))
}

_kill_spring_app() {
  kill -SIGINT ${appPID}
}

main(){
  echo "Stopping spring app initialized..."

  _get_app_pid
  echo "Got app PID ${appPID}"

  _kill_spring_app
  echo "Spring app has been stopped"
  #Need to add an error catcher
}

main
