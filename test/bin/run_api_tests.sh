#!/bin/bash

set -eu

declare apitest_env_file
declare bearer_token

declare OKTA_URL
declare OKTA_USERNAME
declare OKTA_PASSWORD

_get_osmt_project_dir() {
  local project_dir; project_dir="$(git rev-parse --show-toplevel 2> /dev/null)"
  echo "$project_dir"
}

function source_osmt_apitest_env_file() {
  echo "Sourcing $apitest_env_file"
  set -o allexport
  # shellcheck source="test/osmt-apitest.env"
  source "$apitest_env_file"
  set +o allexport
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
  "${test_dir}/bin/stop_api_test_server.sh"
}

error_handler() {
  printf "\n"
  echo "Trapping at error_handler. Exiting"
  # clean up API test Docker resources
  "${test_dir}/bin/stop_api_test_server.sh"
  printf "\n"
}

test_dir="$(_get_osmt_project_dir)/test" || exit 135
apitest_env_file="$test_dir/osmt-apitest.env"

trap error_handler ERR SIGINT SIGTERM

source_osmt_apitest_env_file
get_bearer_token
run_api_tests "v2"
run_shutdown_script
# Need to refresh CI data between versioned tests
# clean_docker_stack
# Start_OSMT_APP (osmt_cli)
# LOAD_CI_DATASET AND REINDEX

#run_api_tests "v3"
#run_shutdown_script
