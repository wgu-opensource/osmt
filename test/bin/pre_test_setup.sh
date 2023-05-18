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

create_postman_collection() {
  local project_dir; project_dir="$(git rev-parse --show-toplevel 2> /dev/null)"
  npx "$project_dir/test/node_modules/.bin/openapi2postmanv2" \
    -s "${project_dir}/docs/int/openapi.yaml" \
    -o "${project_dir}/test/postman/osmt.postman_collection.json" --pretty
}

inject_tests() {
  local project_dir; project_dir="$(git rev-parse --show-toplevel 2> /dev/null)"
  node "${project_dir}/test/postman/test-injector.js"
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


echo_info() {
  echo "INFO: $*"
}

echo_err() {
  echo "ERROR: $*" 1>&2;
}

error_handler() {
  echo_err "Trapping at error_handler. Exiting"
  # clean up API test Docker resources
  "${project_dir}/osmt_cli.sh" -e || exit 135
}

main() {
  local project_dir; project_dir="$(_get_osmt_project_dir)" || exit 135
  local log_file; log_file="${project_dir}/api/target/osmt_spring_app.log"

  # start the API test Docker compose stack and Spring app server, detached. Send log files to 'osmt_spring_app.log'
  echo_info "Starting OSMT Spring app for ${OSMT_STACK_NAME}. Output is suppressed, because console is detached."
  echo_info "See 'osmt_spring_app.log' for console output. Proceeding..."

  touch "$log_file"
  "${project_dir}/osmt_cli.sh" -s  1>"$log_file" 2>"$log_file" & disown  || exit 135

  # curl the Spring app and retry for 2 minutes
  curl_with_retry || exit 135
  
  if [[ "${LOAD_CI_DATASET}" -eq 0  ]]; then
    # load CI static dataset
    "${project_dir}/osmt_cli.sh" -l || exit 135

    # reindex ElasticSearch
    "${project_dir}/osmt_cli.sh" -r || exit 135
  fi

  # Create postman collection
  create_postman_collection || exit 135

  # Insert postman assertion tests
  inject_tests || exit 135
}

trap error_handler ERR SIGINT SIGTERM

main
