#!/bin/bash

set -eu

# shellcheck disable=SC2181
# shellcheck source-path=SCRIPTDIR/../../bin/lib/common.sh
# Sourcing common lib file
source "$(git rev-parse --show-toplevel 2> /dev/null)/bin/lib/common.sh" || exit 135

declare OSMT_STACK_NAME="${OSMT_STACK_NAME:-}"
declare -r OSMT_APP_CLASS='edu.wgu.osmt.ApplicationKt'


get_running_osmt_app_pid(){
  local app_pid; app_pid="$(jps -l | grep ${OSMT_APP_CLASS} | awk '{print $1}')"

  # return string value to the caller from the subshell
  echo "${app_pid}"
}

check_pid_status_retry(){
  for retry_limit in {12..0}; do
    if [[ -z "$(get_running_osmt_app_pid)" ]]; then
      echo_info "Application ${OSMT_APP_CLASS} successfully stopped. Exiting..."
      return 0
    fi
    echo_info "Could not stop application ${OSMT_APP_CLASS}. Will retry ${retry_limit} more times. Retrying in 3 seconds..."
    local app_pid; app_pid="$(get_running_osmt_app_pid)"
    sleep 3
  done

  echo_error "Could not stop ${OSMT_APP_CLASS} after 12 retries. Exiting..." 1>&2;
  exit 135
}

shutdown_osmt_app(){
  echo_info "Attempting to stop application ${OSMT_APP_CLASS}."
  local app_pid; app_pid="$(get_running_osmt_app_pid)"

  if [[ -z "${app_pid}" ]]; then
    echo_warn "Application ${OSMT_APP_CLASS} not running" 1>&2;
    return 0
  fi

  echo_info "Application ${OSMT_APP_CLASS} found PID ${app_pid}. Stopping OSMT..."
  kill -TERM "${app_pid}"
  check_pid_status_retry
}

# stop the API test server
shutdown_osmt_app

# shutdown API test Docker resources
"${PROJECT_DIR}/osmt_cli.sh" -e || exit 135
