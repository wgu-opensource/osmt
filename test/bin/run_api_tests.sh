#!/bin/bash

set -eEu

# shellcheck source-path=SCRIPTDIR/../../bin/lib/common.sh
# Sourcing common lib file
source "$(git rev-parse --show-toplevel 2> /dev/null)/bin/lib/common.sh" || exit 135

declare OSMT_STACK_NAME="${OSMT_STACK_NAME:-}"
declare BASE_URL="${BASE_URL:-}"

declare TEST_DIR
declare LOG_FILE

declare BEARER_TOKEN
declare OKTA_URL
declare OKTA_USERNAME
declare OKTA_PASSWORD
declare TEST_ROLE="${TEST_ROLE:-ROLE_Osmt_Admin}"
declare TEST_USER_NAME="${TEST_USER_NAME:-}"
declare TEST_USER_EMAIL="${TEST_USER_EMAIL:-}"
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
  local auth_env; auth_env="${TEST_DIR}/postman/osmt-auth.environment.json"

  echo_debug "Curling ${BASE_URL}..."
  echo_debug "$(curl http://localhost:8080)"

  echo_debug "Curling ${OKTA_URL}..."
  echo_debug "$(curl "${OKTA_URL}")"

	echo_debug_env

	# Running postman collections
	echo_info "Getting bearer token from Okta..."

  npx "${TEST_DIR}/node_modules/.bin/newman" \
    run "${TEST_DIR}/postman/osmt-auth.postman_collection.json" \
      --env-var oktaUsername="${OKTA_USERNAME}" \
      --env-var oktaPassword="${OKTA_PASSWORD}" \
      --env-var oktaUrl="${OKTA_URL}" \
      --env-var baseUrl="${BASE_URL}" \
      --ignore-redirects \
      --export-environment "${auth_env}"

  BEARER_TOKEN="$(node "${TEST_DIR}/postman/getToken.js")"
  echo_info "Bearer token retrieved."
  echo_debug "${BEARER_TOKEN}"

  # bearer token is written to a file for easy access with local curls / Postman
  # this automated test suite only uses the BEARER_TOKEN variable
  echo "bearerToken=${BEARER_TOKEN}" > "${TEST_DIR}/postman/token.env"
}

run_api_tests() {
  local apiVersion; apiVersion=${1}
  local use_single_auth="${2:-false}"
  echo_info "Running postman collection ..."
  
  local newman_args=(
    "run" "${TEST_DIR}/postman/osmt-testing-api-${apiVersion}.postman_collection.json"
    "--env-var" "baseUrl=${BASE_URL}"
  )
  
  if [[ "${use_single_auth}" == "true" ]]; then
    echo_info "Using single-auth mode - setting admin credentials via environment"
    newman_args+=("--env-var" "testRole=${TEST_ROLE}")
    # Bearer token not needed in single-auth mode, but set empty to avoid errors
    newman_args+=("--env-var" "bearerToken=")
  else
    newman_args+=("--env-var" "bearerToken=${BEARER_TOKEN}")
  fi
  
  npx "${TEST_DIR}/node_modules/.bin/newman" "${newman_args[@]}"
}

run_shutdown_script() {
  echo
  echo_info "Running Shutdown script..."
  "${TEST_DIR}/bin/stop_osmt_app.sh"
}

error_handler() {
  echo
  echo_err "################################################################################################################################"
  echo_err "Trapping at error_handler. Cleaning up and then Exiting..."
  echo_err "################################################################################################################################"

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

  echo_info "Testing OSMT API version ${apiVersion}."
  run_shutdown_script

  remove_api_test_docker_resources "${OSMT_STACK_NAME}"

  # Detect security profile using shared function from common.sh
  # This ensures consistent profile detection across all scripts
  # Note: Profile detection is based on OAuth credentials, not Okta test credentials
  local security_profile; security_profile="$(detect_security_profile)"
  export OSMT_SECURITY_PROFILE="${security_profile}"
  echo_info "Detected security profile: ${security_profile}"

  # Determine if we should use single-auth mode for API tests
  # If OAuth credentials are missing, use single-auth (skip Okta token generation)
  local use_single_auth=false
  if [[ "${security_profile}" == "single-auth" ]]; then
    use_single_auth=true
    echo_info "Using single-auth mode - skipping OAuth token generation"
    echo_info "Using test role: ${TEST_ROLE:-ROLE_Osmt_Admin}"
  else
    # Check if Okta test credentials are available for token generation
    if [[ -z "${OKTA_URL:-}" ]] || [[ -z "${OKTA_USERNAME:-}" ]] || [[ -z "${OKTA_PASSWORD:-}" ]]; then
      echo_warn "OAuth credentials present but Okta test credentials missing"
      echo_warn "API tests may fail without valid Okta credentials for token generation"
    else
      echo_info "Okta test credentials available for token generation"
    fi
  fi

  # Start the API test Docker compose stack and Spring app server, detached. Send log files to 'osmt_spring_app.log'
  echo
  echo_info "Starting OSMT Docker stack and Spring app for ${OSMT_STACK_NAME}. Testing API version ${apiVersion}."
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

  # Get auth token (skip if using single-auth)
  if [[ "${use_single_auth}" == "false" ]]; then
    echo_info "Getting authentication token. Testing API version ${apiVersion}."
    get_bearer_token
  else
    echo_info "Skipping OAuth token generation (single-auth mode). Testing API version ${apiVersion}."
    BEARER_TOKEN=""
  fi

  # Run API tests
  echo_info "Running API ${apiVersion} tests..."
  run_api_tests "${apiVersion}" "${use_single_auth}"

  # Shut down OSMT app
  echo_info "Shutting down OSMT app. Testing API version ${apiVersion}."
  run_shutdown_script
}

main() {
  TEST_DIR="${PROJECT_DIR}/test" || exit 135
  LOG_FILE="${TEST_DIR}/target/osmt_spring_app.log"

  # Sourcing API test env files
  source_env_file_unless_provided_okta "${TEST_DIR}/osmt-apitest.env"
  source_env_file "${TEST_DIR}/bin/osmt-apitest.rc"

  # Calling API V3 version tests
  init_osmt_and_run_api_tests "v3"

  # Calling API V2 version tests
  init_osmt_and_run_api_tests "v2"

  echo_info "Final cleanup of docker stack..."
  remove_api_test_docker_resources "${OSMT_STACK_NAME}"
}


trap error_handler ERR SIGINT SIGTERM

main
