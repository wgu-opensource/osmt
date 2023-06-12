#!/bin/bash
# shellcheck disable=SC2181

#set -x
set -eu

export DEBUG=1
export OSMT_STACK_NAME=osmt_api_test

# API test Docker service ports are the well-known port numbers + 20k
export MYSQL_PORT="23306"
export DB_PORT="${MYSQL_PORT}"
export ELASTICSEARCH_HTTP_PORT="29200"
export ELASTICSEARCH_TRANSPORT_PORT="29300"
export ELASTICSEARCH_URI="localhost:${ELASTICSEARCH_HTTP_PORT}"
export REDIS_PORT="26379"
export REDIS_URI="localhost:${REDIS_PORT}"
export APP_PORT="8080"
export BASE_DOMAIN="localhost:${APP_PORT}"
export BASE_URL="http://${BASE_DOMAIN}"
export OSMT_FRONT_END_PORT="${APP_PORT}"

declare -ri LOAD_CI_DATASET="${LOAD_CI_DATASET:-0}"

_get_osmt_project_dir() {
  local project_dir; project_dir="$(git rev-parse --show-toplevel 2> /dev/null)"
  if [[ -z "${project_dir}" ]]; then
    echo_err "$(basename "${0}") commands should be run from the directory context of the OSMT git repo."
    return 1
  fi

  echo "${project_dir}"
}

install_npm_modules() {
  cd "${project_dir}/test"
  npm install
  cd "../"
}

create_postman_collection() {
  local version; version=${1}
  local project_dir; project_dir="$(_get_osmt_project_dir)" || exit 135
  npx "$project_dir/test/node_modules/.bin/openapi2postmanv2" \
    -s "${project_dir}/docs/int/openapi-${version}.yaml" \
    -o "${project_dir}/test/postman/osmt-api-${version}.postman_collection.json" --pretty
}

inject_tests() {
  local inputFile; inputFile=${1}
  local outputFile; outputFile=${2}
  local apiVersion; apiVersion=${3}
  local project_dir; project_dir="$(_get_osmt_project_dir)" || exit 135
  node "${project_dir}/test/postman/test-injector.js" "${inputFile}" "${outputFile}" "${apiVersion}"
}

echo_info() {
  echo "INFO: $*"
}

echo_err() {
  echo "ERROR: $*" 1>&2;
}

error_handler() {
  local project_dir; project_dir="$(_get_osmt_project_dir)" || exit 135
  echo_err "Trapping at error_handler. Exiting"
  # clean up API test Docker resources
  "${project_dir}/osmt_cli.sh" -e || exit 135
}

main() {
  local project_dir; project_dir="$(_get_osmt_project_dir)" || exit 135
  local log_file; log_file="${project_dir}/api/target/osmt_spring_app.log"

  # Adding docker clean up at the beginning to keep previous test state
  # in case we need to debug and cleaning up docker could remove logs.
  if [[ -n $(docker ps -aq --filter=name="osmt_api*") ]]; then
    "${project_dir}/test/bin/clean_docker_stack.sh"
  fi

  # Run NPM install
  echo_info "Installing NPM modules..."
  install_npm_modules || exit 135

  # start the API test Docker compose stack and Spring app server, detached. Send log files to 'osmt_spring_app.log'
  echo_info "Starting OSMT Spring app for ${OSMT_STACK_NAME}. Output is suppressed, because console is detached."
  echo_info "See 'osmt_spring_app.log' for console output. Proceeding..."

  # create log file
  touch "$log_file"

  # curl the Spring app and retry for 2 minutes
  "${project_dir}/test/bin/start_osmt_app.sh"

  if [[ "${LOAD_CI_DATASET}" -eq 0  ]]; then
    # load CI static dataset and reindex ElasticSearch
    "${project_dir}/test/bin/load_and_reindex_ci_data.sh" || exit 135
  fi

  # Create postman collection
  create_postman_collection "v2"|| exit 135
  create_postman_collection "v3"|| exit 135

  # Insert postman assertion tests
  inject_tests \
  "${project_dir}/test/postman/osmt-api-v2.postman_collection.json" \
  "${project_dir}/test/postman/osmt-testing-api-v2.postman_collection.json" \
  "v2"

  inject_tests \
  "${project_dir}/test/postman/osmt-api-v3.postman_collection.json" \
  "${project_dir}/test/postman/osmt-testing-api-v3.postman_collection.json" \
  "v3"
}

trap error_handler ERR SIGINT SIGTERM

main
