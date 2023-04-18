#!/bin/bash

set -eu

main(){
  echo "Stopping spring app initialized..."

  local retry_limit=5
  local retry_count=0
  local appPID="$(jps -l | grep edu.wgu.osmt.ApplicationKt | awk '{print $1}')"

  if [[ -z "${appPID}" ]]; then
    echo "Application not running"
    exit 1
  else
    echo "Got app PID ${appPID}"
  fi

      kill -SIGINT ${appPID}
      sleep 1

      if [[ -z "$(jps -l | grep edu.wgu.osmt.ApplicationKt | awk '{print $1}')" ]]; then
        echo "Spring app has been stopped"
      else
        echo "App failed to stop"
        exit 1
      fi
}

main
