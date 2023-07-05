#!/bin/bash

# set -eu
set -eEu

export OSMT_STACK_NAME=osmt_api_test

declare apitest_env_file
declare test_dir
declare bearer_token

declare OKTA_URL
declare OKTA_USERNAME
declare OKTA_PASSWORD
declare -i APP_START_CHECK_RETRY_LIMIT="${APP_START_CHECK_RETRY_LIMIT:-12}"

_get_osmt_project_dir() {
  local project_dir; project_dir="$(git rev-parse --show-toplevel 2> /dev/null)"
  echo "$project_dir"
}

function source_osmt_apitest_env_file() {
  # Checks to see if osmt-apitest.env file exists
  if [[ -f "${apitest_env_file}" || -r "${apitest_env_file}" ]]; then
    echo "Sourcing $apitest_env_file"
    set -o allexport
    # shellcheck source="test/osmt-apitest.env"
    source "$apitest_env_file"
    set +o allexport
  fi
}

curl_with_retry() {
  local log_file; log_file="${project_dir}/test/target/osmt_spring_app.log"
  local -i rc=-1
  local -i retry_limit=${APP_START_CHECK_RETRY_LIMIT}
  until [ ${rc} -eq 0 ] && [ ${retry_limit} -eq 0 ]; do
      echo_info "Attempting to request the index page of the OSMT Spring app with curl..."
      echo_info "${BASE_URL}"
      curl -s "${BASE_URL}" 1>/dev/null 2>/dev/null
      rc=$?
      if [[ ${rc} -eq 0 ]]; then
        echo_info "Index page loaded. Proceeding..."
        return 0
      fi
      if [[ ${retry_limit} -eq 0 ]]; then
        echo
        echo_info "osmt_spring_app log file below..."
        echo
        echo_err "Could not load the index page."
        cat "${log_file}"
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

function get_bearer_token() {
  declare auth_env; auth_env="$test_dir/postman/osmt-auth.environment.json"

	# Running postman collections
	echo "Getting access token from OKTA ..."
  npx "$test_dir/node_modules/.bin/newman" \
    run "$test_dir/postman/osmt-auth.postman_collection.json" \
      --env-var oktaUsername="$OKTA_USERNAME" \
      --env-var oktaPassword="$OKTA_PASSWORD" \
      --env-var oktaUrl="$OKTA_URL" \
      --env-var baseUrl="$BASE_URL" \
      --ignore-redirects \
      --export-environment "$auth_env"

  bearer_token="$( node "$test_dir/postman/getToken.js")"
  echo "Retrieved token:"
  echo "$bearer_token"

  echo "bearerToken=$bearer_token" > "$test_dir/postman/token.env"
  echo
}

run_api_tests() {
  local apiVersion; apiVersion=${1}
  echo "Running postman collection ..."
  npx "$test_dir/node_modules/.bin/newman" \
    run "$test_dir/postman/osmt-testing-api-${apiVersion}.postman_collection.json" \
      --env-var baseUrl="$BASE_URL" \
      --env-var bearerToken="$bearer_token"
}

run_shutdown_script() {
  printf "\n"
  echo "Running Shutdown script..."
  "${test_dir}/bin/stop_osmt_app.sh"
}

error_handler() {
  printf "\n"
  echo "Trapping at error_handler. Exiting"
  # clean up API test Docker resources
  "${test_dir}/bin/stop_osmt_app.sh"
  printf "\n"
}

test_dir="$(_get_osmt_project_dir)/test" || exit 135
apitest_env_file="$test_dir/osmt-apitest.env"

trap error_handler ERR SIGINT SIGTERM

# Sourcing common lib file
source "$(_get_osmt_project_dir)/bin/lib/common.sh"

# Sourcing API test env file
source_env_file apitest_env_file

# Clean up, stop docker-compose stack and prune API-test related images and volumes
remove_osmt_docker_artifacts_for_stack OSMT_STACK_NAME

# Start OSMT app
"$(_get_osmt_project_dir)/osmt_cli.sh" -s

# Check to make sure OSMT app is up and running
curl_with_retry

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
run_api_tests "v3"

# Shut down OSMT app
run_shutdown_script

# Need to refresh CI data between versioned tests
remove_osmt_docker_artifacts_for_stack OSMT_STACK_NAME

# Start OSMT app
"$(_get_osmt_project_dir)/osmt_cli.sh" -s

# Check to make sure OSMT app is up and running
curl_with_retry

if [[ "${LOAD_CI_DATASET}" -eq 0  ]]; then
  echo_info "Loading Static CI Dataset And Reindexing..."
  # load CI static dataset
  "$(_get_osmt_project_dir)/osmt_cli.sh" -l
  # Reindex Elasticsearch
  "$(_get_osmt_project_dir)/osmt_cli.sh" -r
fi

# Run V2 API tests
run_api_tests "v2"
run_shutdown_script
remove_osmt_docker_artifacts_for_stack OSMT_STACK_NAME
