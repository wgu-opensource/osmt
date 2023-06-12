#!/bin/bash

set -eu

launch_osmt() {
  local project_dir; project_dir="$(git rev-parse --show-toplevel 2> /dev/null)"
  local log_file; log_file="${project_dir}/api/target/osmt_spring_app.log"

  # start the API test Docker compose stack and Spring app server, detached. Send log files to 'osmt_spring_app.log'
  echo_info "Starting OSMT Spring app for ${OSMT_STACK_NAME}. Output is suppressed, because console is detached."
  echo_info "See 'osmt_spring_app.log' for console output. Proceeding..."

  "${project_dir}/osmt_cli.sh" -s  1>"$log_file" 2>"$log_file" & disown  || exit 135

  # curl the Spring app and retry for 2 minutes
  curl_with_retry || exit 135
}

curl_with_retry() {
  local -i rc=-1
  local -i retry_limit=12
  until [ ${rc} -eq 0 ] && [ ${retry_limit} -eq 0 ]; do
      echo_info "Attempting to request the index page of the OSMT Spring app with curl..."
      curl -s "${BASE_URL}" 1>/dev/null 2>/dev/null
      rc=$?
      if [[ ${rc} -eq 0 ]]; then
        echo_info "Index page loaded. Proceeding..."
        return 0
      fi
      if [[ ${retry_limit} -eq 0 ]]; then
        echo_err "Could not load the index page."
        return 1
      fi
      if [[ ${rc} -ne 0 ]]; then
        echo_info "Could not load the index page. Will retry ${retry_limit} more times. Retrying in 10 seconds..."
      fi
      # shell check SC2219
      ((retry_limit--)) || true
      sleep 10
  done
}
