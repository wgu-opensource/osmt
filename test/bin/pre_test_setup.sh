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

error_handler() {
  local project_dir; project_dir="$(_get_osmt_project_dir)" || exit 135
  echo_err "Trapping at error_handler. Exiting"
  # clean up API test Docker resources
  "${project_dir}/osmt_cli.sh" -e || exit 135
}

main() {
  local project_dir; project_dir="$(_get_osmt_project_dir)" || exit 135

  # Sourcing the common lib file
  source "${project_dir}/bin/lib/common.sh"
  # Sourcing API test env file
  parse_osmt_envs "${project_dir}/test/osmt-apitest.env"

  # Run NPM install
  echo_info "Installing NPM modules..."
  install_npm_modules || exit 135

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
