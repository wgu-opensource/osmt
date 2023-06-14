#!/bin/bash
# shellcheck disable=SC2181

set -eu

export DEBUG=1
export OSMT_STACK_NAME=osmt_api_test

declare project_dir
declare -r OSMT_APP_CLASS='edu.wgu.osmt.ApplicationKt'

_get_osmt_project_dir() {
  local project_dir; project_dir="$(git rev-parse --show-toplevel 2> /dev/null)"
  if [[ -z "${project_dir}" ]]; then
    echo_err "$(basename "${0}") commands should be run from the directory context of the OSMT git repo."
    return 1
  fi

  echo "${project_dir}"
}


get_app_pid(){
  local app_pid; app_pid="$(jps -l | grep ${OSMT_APP_CLASS} | awk '{print $1}')"

  echo "${app_pid}"
}

check_pid_status_retry(){
  for retry_limit in {12..0}; do
    if [[ -z "$(get_app_pid)" ]]; then
      echo "INFO: Application ${OSMT_APP_CLASS} successfully stopped. Exiting..."
      exit 0
    fi
    echo "INFO: Could not stop application ${OSMT_APP_CLASS}. Will retry ${retry_limit} more times. Retrying in 3 seconds..."
    local app_pid; app_pid="$(jps -l | grep ${OSMT_APP_CLASS} | awk '{print $1}')"
    echo "${app_pid}"
    sleep 3
  done

  echo "ERROR: Could not stop ${OSMT_APP_CLASS} after 12 retries. Exiting..." 1>&2;
  exit 135
}

shutdown_osmt_app(){
  echo "INFO: Attempting to stop application ${OSMT_APP_CLASS}."
  local app_pid; app_pid="$(get_app_pid)"

  if [[ -z "${app_pid}" ]]; then
    echo "ERROR: Application ${OSMT_APP_CLASS} not running" 1>&2;
    exit 135
  else
    echo "INFO: Application ${OSMT_APP_CLASS} found PID ${app_pid}."
  fi

  kill -TERM "${app_pid}"

  check_pid_status_retry
}

project_dir="$(_get_osmt_project_dir)" || exit 135

# stop the API test server
shutdown_osmt_app

# shutdown API test Docker resources
"${project_dir}/osmt_cli.sh" -e || exit 135


