#!/bin/bash

set -eu
# Add INSTANCE_ID as ENV variable 
declare INSTANCE_ID="${INSTANCE_ID}"
declare project_dir="$(git rev-parse --show-toplevel 2> /dev/null)"


function stop_ec2_instance(){
  aws ec2 stop-instances --instance-ids $INSTANCE_ID
  aws ec2 wait instance-stopped --instance-ids $INSTANCE_ID
  rc=$?
  if [[ $rc -ne 0 ]]; then
    # better exit echo for logging
    echo "40 failed checks have been polled, exiting.."
    exit 135
  fi
}

function start_ec2_instance(){
  aws ec2 start-instances --instance-ids $INSTANCE_ID
  aws ec2 wait instance-running --instance-ids $INSTANCE_ID
  rc=$?
  if [[ $rc -ne 0 ]]; then
    # better exit echo for logging
    echo "40 failed checks have been polled, exiting.."
    exit 135
  fi
}

function start_reindex(){
  cd project_dir
  sh ./osmt_cli -r
}


function main() {
  stop_ec2_instance
  start_ec2_instance
  start_reindex
}
