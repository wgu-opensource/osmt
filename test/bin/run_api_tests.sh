#!/bin/bash

# set -eu
set -eEu

export OSMT_STACK_NAME=osmt_api_test

declare apitest_env_file
declare test_dir
declare bearer_token
declare log_file

declare OKTA_URL
declare OKTA_USERNAME
declare OKTA_PASSWORD
declare -i APP_START_CHECK_RETRY_LIMIT="${APP_START_CHECK_RETRY_LIMIT:-12}"
declare -ri LOAD_CI_DATASET="${LOAD_CI_DATASET:-0}"

function _get_osmt_project_dir() {
  local project_dir; project_dir="$(git rev-parse --show-toplevel 2> /dev/null)"
  echo "$project_dir"
}

function curl_with_retry() {
  local log_file; log_file="${test_dir}/target/osmt_spring_app.log"
  local -i rc=-1
  local -i retry_limit=${APP_START_CHECK_RETRY_LIMIT}
  until [ ${rc} -eq 0 ] && [ ${retry_limit} -eq 0 ]; do
      echo_info "Attempting to request the index page of the OSMT Spring app with curl..."
      curl -s "${BASE_URL}" 1>/dev/null 2>/dev/null
      rc=$?
      if [[ ${rc} -eq 0 ]]; then
        echo_info "Index page loaded. Proceeding..."
        return 0
      fi
      if [[ ${retry_limit} -eq 0 ]]; then
        echo
        echo_info "Printing osmt_spring_app log file below..."
        echo
        echo_err "Could not load the index page."
        cat "${log_file}"
        return 1
      fi
      if [[ ${rc} -ne 0 ]]; then
        echo_info "Could not load the index page. Retrying in 10 seconds. Will retry ${retry_limit} more times..."
      fi
      # shell check SC2219
      ((retry_limit--)) || true
      sleep 10
  done
}

function get_bearer_token() {
  declare auth_env; auth_env="$test_dir/postman/osmt-auth.environment.json"

	# Running postman collections
	echo_info "Getting access token from OKTA ..."
  npx "$test_dir/node_modules/.bin/newman" \
    run "$test_dir/postman/osmt-auth.postman_collection.json" \
      --env-var oktaUsername="$OKTA_USERNAME" \
      --env-var oktaPassword="$OKTA_PASSWORD" \
      --env-var oktaUrl="$OKTA_URL" \
      --env-var baseUrl="$BASE_URL" \
      --ignore-redirects \
      --export-environment "$auth_env"

  bearer_token="$( node "$test_dir/postman/getToken.js")"
  echo_info "Retrieved token:"
  echo "$bearer_token"

  echo_info "bearerToken=$bearer_token" > "$test_dir/postman/token.env"
  echo
}

function run_api_tests() {
  local apiVersion; apiVersion=${1}
  echo_info "Running postman collection ..."
  npx "$test_dir/node_modules/.bin/newman" \
    run "$test_dir/postman/osmt-testing-api-${apiVersion}.postman_collection.json" \
      --env-var baseUrl="$BASE_URL" \
      --env-var bearerToken="$bearer_token"
}

 function run_shutdown_script() {
  printf "\n"
  echo_info "Running Shutdown script..."
  "${test_dir}/bin/stop_osmt_app.sh"
}

function error_handler() {
  printf "\n"
  echo_warn "Trapping at error_handler. Exiting"
  # clean up API test Docker resources
  "${test_dir}/bin/stop_osmt_app.sh"
  printf "\n"
}
function main() {
  # Sourcing common lib file
  source "$(_get_osmt_project_dir)/bin/lib/common.sh"

  # Sourcing API test env file
  parse_osmt_envs "${apitest_env_file}"

  # Clean up, stop docker-compose stack and prune API-test related images and volumes
  # Disable error handling around docker cleanup.
  set +eE
  remove_osmt_docker_artifacts_for_stack "${OSMT_STACK_NAME}"
  set -eE

  # Start OSMT app
  "$(_get_osmt_project_dir)/osmt_cli.sh" -s 1>"$log_file" 2>"$log_file" & disown  || exit 135

  # Check to see if app is up and running
  curl_with_retry || exit 135

  if [[ "${LOAD_CI_DATASET}" -eq 0  ]]; then
    echo_info "Loading Static CI Dataset And Reindexing..."
    # load CI static dataset
    "$(_get_osmt_project_dir)/osmt_cli.sh" -l
    # Reindex Elasticsearch
    "$(_get_osmt_project_dir)/osmt_cli.sh" -r
  fi

  # Get auth token
  get_bearer_token

  # Run V3 API tests
  echo_info "Running API V3 tests..."
  run_api_tests "v3"

  # Shut down OSMT app
  run_shutdown_script

  # Need to refresh CI data between versioned tests
  remove_osmt_docker_artifacts_for_stack "${OSMT_STACK_NAME}"

  # Start OSMT app
  "${test_dir}/bin/start_osmt_app.sh" "${OSMT_STACK_NAME}"

  if [[ "${LOAD_CI_DATASET}" -eq 0  ]]; then
    echo_info "Loading Static CI Dataset And Reindexing..."
    # load CI static dataset
    "$(_get_osmt_project_dir)/osmt_cli.sh" -l
    # Reindex Elasticsearch
    "$(_get_osmt_project_dir)/osmt_cli.sh" -r
  fi

  # Run V2 API tests
  echo_info "Running API V2 tests..."
  run_api_tests "v2"
  run_shutdown_script
  remove_osmt_docker_artifacts_for_stack "${OSMT_STACK_NAME}"
}

test_dir="$(_get_osmt_project_dir)/test" || exit 135
apitest_env_file="$test_dir/osmt-apitest.env"
log_file="${test_dir}/target/osmt_spring_app.log"

trap error_handler ERR SIGINT SIGTERM
main
