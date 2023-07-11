#!/bin/bash

set -eEu

# shellcheck source-path=SCRIPTDIR/../../bin/lib/common.sh
# Sourcing common lib file
source "$(git rev-parse --show-toplevel 2> /dev/null)/bin/lib/common.sh" || exit 135

declare OSMT_STACK_NAME="${OSMT_STACK_NAME:-}"
declare BASE_URL="${BASE_URL:-}"

declare TEST_DIR
declare APITEST_ENV_FILE
declare OKTA_ENV_FILE
declare LOG_FILE

declare BEARER_TOKEN
declare OKTA_URL
declare OKTA_USERNAME
declare OKTA_PASSWORD
declare -i APP_START_CHECK_RETRY_LIMIT="${APP_START_CHECK_RETRY_LIMIT:-12}"

curl_with_retry() {
  local -i rc=-1
  local -i retry_limit="${APP_START_CHECK_RETRY_LIMIT}"
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
        cat "${LOG_FILE}"
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

get_bearer_token() {
  declare auth_env; auth_env="${TEST_DIR}/postman/osmt-auth.environment.json"

	# Running postman collections
	echo_info "Getting access token from OKTA ..."
  npx "${TEST_DIR}/node_modules/.bin/newman" \
    run "${TEST_DIR}/postman/osmt-auth.postman_collection.json" \
      --env-var oktaUsername="$OKTA_USERNAME" \
      --env-var oktaPassword="$OKTA_PASSWORD" \
      --env-var oktaUrl="$OKTA_URL" \
      --env-var baseUrl="$BASE_URL" \
      --ignore-redirects \
      --export-environment "$auth_env"

  BEARER_TOKEN="$(node "${TEST_DIR}/postman/getToken.js")"
  echo_info "Bearer token retrieved."
  echo_debug "${BEARER_TOKEN}"

  echo "bearerToken=${BEARER_TOKEN}" > "${TEST_DIR}/postman/token.env"
}

run_api_tests() {
  local apiVersion; apiVersion=${1}
  echo_info "Running postman collection ..."
  npx "${TEST_DIR}/node_modules/.bin/newman" \
    run "${TEST_DIR}/postman/osmt-testing-api-${apiVersion}.postman_collection.json" \
      --env-var baseUrl="${BASE_URL}" \
      --env-var bearerToken="${BEARER_TOKEN}"
}

run_shutdown_script() {
  echo
  echo_info "Running Shutdown script..."
  "${TEST_DIR}/bin/stop_osmt_app.sh"
}

error_handler() {
  echo
  echo_warn "Trapping at error_handler. Cleaning up and then Exiting..."

  run_shutdown_script
  remove_api_test_docker_resources "${OSMT_STACK_NAME}"

  echo
}

remove_api_test_docker_resources() {
  local stack_name; stack_name="${1}"
  # Clean up, stop docker-compose stack and prune API-test related images and volumes
  echo_info "Stopping and removing docker stack..."

  # Disable error handling around docker cleanup.
  set +eE
  remove_osmt_docker_artifacts_for_stack "${stack_name}"
  # Re-enable error trapping
  set -eE
}

init_osmt_and_run_api_tests() {
  local apiVersion; apiVersion="${1}"

  run_shutdown_script

  remove_api_test_docker_resources "${OSMT_STACK_NAME}"

  # Start the API test Docker compose stack and Spring app server, detached. Send log files to 'osmt_spring_app.log'
  echo
  echo_info "Starting OSMT Docker stack and Spring app for ${OSMT_STACK_NAME}."
  echo_info "Application console is detached, See 'osmt_spring_app.log' for console output. Proceeding..."
  echo
  "${PROJECT_DIR}/osmt_cli.sh" -s 1>"${LOG_FILE}" 2>"${LOG_FILE}" & disown  || exit 135

  # Check to see if app is up and running
  curl_with_retry || exit 135

  echo_info "Loading Static CI Dataset And Reindexing..."
  # load CI static dataset
  "${PROJECT_DIR}/osmt_cli.sh" -l
  # Reindex Elasticsearch
  "${PROJECT_DIR}/osmt_cli.sh" -r

  # Get auth token
  echo_info "Getting authentication token"
  get_bearer_token

  # Run V3 API tests
  echo_info "Running API ${apiVersion} tests..."
  run_api_tests "${apiVersion}"

  # Shut down OSMT app
  run_shutdown_script
}

main() {
  TEST_DIR="${PROJECT_DIR}/test" || exit 135
  LOG_FILE="${TEST_DIR}/target/osmt_spring_app.log"

  # Sourcing API test env files
  source_osmt_envs "${TEST_DIR}/osmt-apitest.env"
  source_osmt_envs "${TEST_DIR}/bin/osmt-apitest.rc"

  # Calling API V3 version tests
  init_osmt_and_run_api_tests "v3"

  # Calling API V2 version tests
  init_osmt_and_run_api_tests "v2"

  echo_info "Final cleanup of docker stack..."
  remove_api_test_docker_resources "${OSMT_STACK_NAME}"
}


trap error_handler ERR SIGINT SIGTERM

main
