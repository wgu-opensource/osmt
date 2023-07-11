#!/bin/bash

set -eEu

# shellcheck disable=SC2181
# shellcheck source-path=SCRIPTDIR/../../bin/lib/common.sh
# Sourcing common lib file
source "$(git rev-parse --show-toplevel 2> /dev/null)/bin/lib/common.sh" || exit 135

install_npm_modules() {
  cd "${PROJECT_DIR}/test"
  npm install
  cd "../"
}

create_postman_collection() {
  local version; version=${1}

  npx "${PROJECT_DIR}/test/node_modules/.bin/openapi2postmanv2" \
    -s "${PROJECT_DIR}/docs/int/openapi-${version}.yaml" \
    -o "${PROJECT_DIR}/test/postman/osmt-api-${version}.postman_collection.json" --pretty
}

inject_tests() {
  local inputFile; inputFile=${1}
  local outputFile; outputFile=${2}
  local apiVersion; apiVersion=${3}

  node "${PROJECT_DIR}/test/postman/test-injector.js" "${inputFile}" "${outputFile}" "${apiVersion}"
}

error_handler() {
  echo_err "Trapping at error_handler. Exiting..."
  # clean up API test Docker resources
  "${PROJECT_DIR}/osmt_cli.sh" -e || exit 135
}

main() {
  # Sourcing API test env file
  source_env_file "${PROJECT_DIR}/test/osmt-apitest.env"
  source_env_file "${PROJECT_DIR}/test/bin/osmt-apitest.rc"

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
    "${PROJECT_DIR}/test/postman/osmt-api-v2.postman_collection.json" \
    "${PROJECT_DIR}/test/postman/osmt-testing-api-v2.postman_collection.json" \
    "v2"

  echo_info "Injecting Postman tests for v3 API..."
  inject_tests \
    "${PROJECT_DIR}/test/postman/osmt-api-v3.postman_collection.json" \
    "${PROJECT_DIR}/test/postman/osmt-testing-api-v3.postman_collection.json" \
    "v3"
}

trap error_handler ERR SIGINT SIGTERM

main
