#!/bin/bash
# shellcheck disable=SC2181

#set -x
# set -eu
set -eEu

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
declare -i APP_START_CHECK_RETRY_LIMIT="${APP_START_CHECK_RETRY_LIMIT:-12}"

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

clean_docker_stack() {
  local project_dir; project_dir="$(_get_osmt_project_dir)" || exit 135
  echo_info "Stopping OSMT ${OSMT_STACK_NAME} Docker stack"
  cd "${project_dir}/docker" || return 1
  docker-compose --file dev-stack.yml --project-name "${OSMT_STACK_NAME}" down
  rc=$?
  if [[ $rc -ne 0 ]]; then
    echo_err "Stopping OSMT ${OSMT_STACK_NAME} Docker stack failed. Exiting..."
    return 1
  fi

  echo
  echo_info "Removing OSMT-related Docker containers..."
  docker ps -aq --filter=name='osmt_api*' | xargs docker rm

  echo
  echo_info "Removing OSMT-related Docker images..."
  docker images -q --filter=reference='osmt_api*' | xargs docker rmi

  echo
  echo_info "Removing OSMT-related Docker volumes..."
  docker volume ls -q -f name=osmt_api | xargs docker volume rm -f {} &> /dev/null
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
  local log_file; log_file="${project_dir}/test/target/osmt_spring_app.log"

  "${project_dir}/test/bin/clean_docker_stack.sh"

  # Run NPM install
  echo_info "Installing NPM modules..."
  install_npm_modules || exit 135

  # curl the Spring app and retry for 2 minutes
  echo_info "Starting OSMT API Test stack..."
  "${project_dir}/test/bin/start_osmt_app.sh" || exit 135

  echo_info "Sleeping for 5 seconds..."
  sleep 5

  echo_info "Loading Static CI Dataset..."
  if [[ "${LOAD_CI_DATASET}" -eq 0  ]]; then
    # load CI static dataset and reindex ElasticSearch
    "${project_dir}/test/bin/load_and_reindex_ci_data.sh" || exit 135
  fi

  # Create postman collection
  echo_info "Creating Postman Collection for unversioned / v2 API..."
  create_postman_collection "v2"|| exit 135

  echo_info "Creating Postman Collection for v3 API..."
  create_postman_collection "v3"|| exit 135

  # Insert postman assertion tests
  echo_info "Injecting Postman tests for unversioned / v2 API..."
  inject_tests \
    "${project_dir}/test/postman/osmt-api-v2.postman_collection.json" \
    "${project_dir}/test/postman/osmt-testing-api-v2.postman_collection.json" \
    "v2"

  echo_info "Injecting Postman tests for v3 API..."
  inject_tests \
    "${project_dir}/test/postman/osmt-api-v3.postman_collection.json" \
    "${project_dir}/test/postman/osmt-testing-api-v3.postman_collection.json" \
    "v3"
}

trap error_handler ERR SIGINT SIGTERM

main
