#!/bin/bash
# shellcheck disable=SC2181
# shellcheck source-path=SCRIPTDIR/bin/lib/common.sh

set -u

declare QUICKSTART_ENV_FILE
declare DEV_ENV_FILE
declare APITEST_ENV_FILE

# These values are also in application.properties. Providing these env vars to this script will
# override these local dev defaults. These are in no way suitable for any level of actual deployment
declare -r OSMT_STACK_NAME="${OSMT_STACK_NAME:-osmt_dev}"
declare -r MYSQL_HOST="${MYSQL_HOST:-0.0.0.0}"
declare -r MYSQL_PORT="${MYSQL_PORT:-3306}"
declare -r MYSQL_NAME="${MYSQL_NAME:-osmt_db}"
declare -r MYSQL_USER="${MYSQL_USER:-osmt_db_user}"
declare -r MYSQL_PASSWORD="${MYSQL_PASSWORD:-password}"
declare -r ELASTICSEARCH_HTTP_PORT="${ELASTICSEARCH_HTTP_PORT:-9200}"
declare -r ELASTICSEARCH_TRANSPORT_PORT="${ELASTICSEARCH_TRANSPORT_PORT:-9300}"
declare -r OSMT_SECURITY_PROFILE="${OSMT_SECURITY_PROFILE:-}"



init_osmt_env_files() {
  _init_osmt_env_file "Quickstart" "${QUICKSTART_ENV_FILE}" || return 1
  _init_osmt_env_file "Development" "${DEV_ENV_FILE}" || return 1
  _init_osmt_env_file "API Tests" "${APITEST_ENV_FILE}" || return 1
}

validate_osmt_dev_environment() {
  echo
  echo_info "Validating software and SDKs available for OSMT on this machine..."
  local -i is_environment_valid=0
  _report_os
  _validate_git || is_environment_valid+=1
  _validate_docker_version || is_environment_valid+=1
  _validate_osmt_dev_dependencies || is_environment_valid+=1
  _validate_mysql_client || is_environment_valid+=1

  echo
  echo_info "Checking environment files used in local OSMT instances..."
  _validate_env_file "${QUICKSTART_ENV_FILE}" || is_environment_valid+=1
  _validate_env_file "${DEV_ENV_FILE}" || is_environment_valid+=1
  _validate_env_file "${APITEST_ENV_FILE}" || is_environment_valid+=1

  if [[ "${is_environment_valid}" -ne 0 ]]; then
    echo
    echo_err "Your environment has not passed validation. Please inspect the ERROR messages above."
    return 1
  fi
}

start_osmt_quickstart() {
  local -i rc
  _validate_git || return 1
  _validate_docker_version
  rc=$?
  if [[ $rc -ne 0 ]]; then
    echo_err "Aborting OSMT Quickstart. Exiting..."
    return 1
  fi

  _cd_osmt_project_dir || return 1
  _validate_env_file "${QUICKSTART_ENV_FILE}"
  rc=$?
  if [[ $rc -ne 0 ]]; then
    echo_err "Aborting OSMT Quickstart. Exiting..."
    return 1
  fi

  echo
  echo_info "Starting OSMT Quickstart with docker-compose using osmt-quickstart.env"
  docker-compose --file docker-compose.quickstart.yml --env-file "${QUICKSTART_ENV_FILE}" --project-name osmt_quickstart up
}

import_osmt_metadata() {
  local stack_name="${1}"
  echo
  local -i rc
  start_osmt_docker_stack "${stack_name}"
  _validate_osmt_docker_stack "${stack_name}"
  rc=$?
  if [[ $rc -ne 0 ]]; then
    echo_err "There was an issue validating your backend ${stack_name} Docker stack. Exiting..."
    return 1
  fi

  source_env_file_unless_provided_oauth "${DEV_ENV_FILE}" || return 1
  _cd_osmt_project_dir || return 1
  cd api || return 1

  echo
  echo_info "Importing BLS metadata via Maven Spring Boot plug-in (Maven log output suppressed)..."
  mvn -q -Dspring-boot.run.profiles=dev,import \
    -Dspring-boot.run.arguments="--import-type=bls,--csv=${PROJECT_DIR}/import/BLS-Import.csv" \
    spring-boot:run

  echo
  echo_info "Importing O*NET metadata via Maven Spring Boot plug-in (Maven log output suppressed)..."
  mvn -q -Dspring-boot.run.profiles=dev,import \
    -Dspring-boot.run.arguments="--import-type=onet,--csv=${PROJECT_DIR}/import/oNet-Import.csv" \
    spring-boot:run

  echo
  echo_info "Reindexing ElasticSearch via Maven Spring Boot plug-in (Maven log output suppressed)..."
  mvn -q -Dspring-boot.run.profiles=dev,reindex spring-boot:run

  echo_info "Exited OSMT import / reindex. Returning to project root (${PROJECT_DIR})"
  cd "${PROJECT_DIR}" || return 1
}

start_osmt_spring_app() {
  local stack_name="${1}"
  echo
  local -i rc
  start_osmt_docker_stack "${stack_name}"
  _validate_osmt_docker_stack "${stack_name}"
  rc=$?
  if [[ $rc -ne 0 ]]; then
    echo_err "There was an issue validating your backend ${stack_name} Docker stack. Exiting..."
    return 1
  fi

  source_env_file_unless_provided_oauth "${DEV_ENV_FILE}" || return 1

  _cd_osmt_project_dir || return 1
  cd api || return 1

  local security_profile="${OSMT_SECURITY_PROFILE:-oauth2-okta}"
  echo
  echo_info "Starting OSMT via Maven Spring Boot plug-in with ${security_profile} security profile (Maven log output suppressed)..."
  mvn -q -Dspring-boot.run.profiles=dev,apiserver,"${security_profile}" spring-boot:run
  rc=$?
  if [[ $rc -ne 0 ]]; then
    echo_err "Error running OSMT Spring app with Maven. Returning to project root (${PROJECT_DIR})"
    return 1
  fi
  echo_info "Exited OSMT Spring app with Maven. Returning to project root (${PROJECT_DIR})"
  cd "${PROJECT_DIR}" || return 1
}

load_static_ci_dataset(){
  local stack_name="${1}"
  local -i rc
  echo
  echo_info "Loading static CI dataset into MySQL database ${MYSQL_NAME} at ${MYSQL_HOST}:${MYSQL_PORT}"
  echo
  _validate_mysql_client || return 1

  local -i db_container_count=0;
  # some installations of Docker delimit container names with hyphens. Others use underscores.
#
  echo
  docker ps --filter name="${stack_name}"
  echo
#
  db_container_count+="$(docker ps -q --filter name="${stack_name}"_db_1 | wc -l)"
  db_container_count+="$(docker ps -q --filter name="${stack_name}"-db-1 | wc -l)"
  echo_debug "MySQL container count: ${db_container_count}"

  if [[ "${db_container_count}" -ne 1 ]]; then
    echo_err "Development Docker stack MySQL container is not running as expected."
    echo_err "(${db_container_count} ${stack_name} DB containers running. Should be 1). Exiting..."
    return 1
  fi

  local sql_file="${PROJECT_DIR}/test/sql/fixed_ci_dataset.sql"

  echo_debug_env

  mysql \
    --host="${MYSQL_HOST}" \
    --port="${MYSQL_PORT}" \
    --database="${MYSQL_NAME}" \
    --user="${MYSQL_USER}" \
    --password="${MYSQL_PASSWORD}" < "${sql_file}"

  rc=$?
  echo
  if [[ $rc -ne 0 ]]; then
    echo_err "Error loading static CI dataset (${sql_file})"
    echo_err "Before loading the static CI dataset into your local MySQL instance, your database must have the current database schema."
    echo_err "Locally, this is done by Flyway when the Spring application starts. You may need to start the Spring application first."
    echo_err "Exiting..."
    return 1
  fi
  echo_info "Successfully loaded static CI data set to MySQL (found in ${sql_file})."
  echo_info "You must reindex ElasticSearch to use this data in OSMT. You can do this with $(basename "${0}") -r"
}

start_osmt_dev_spring_app_reindex() {
  local stack_name="${1}"
  echo
  local -i rc
  start_osmt_docker_stack "${stack_name}"
  _validate_osmt_docker_stack "${stack_name}"
  rc=$?
  if [[ $rc -ne 0 ]]; then
    echo_err "There was an issue validating your backend ${stack_name} Docker stack. Exiting..."
    return 1
  fi

  _cd_osmt_project_dir || return 1
  source_env_file_unless_provided_oauth "${DEV_ENV_FILE}" || return 1
  echo
  echo_info "Starting OSMT via Maven Spring Boot plug-in to reindex ElasticSearch..."
  cd api || return 1
  mvn -Dspring-boot.run.profiles=dev,reindex spring-boot:run
  rc=$?
  if [[ $rc -ne 0 ]]; then
    echo_err "Error running OSMT Spring app with Maven. Returning to project root (${PROJECT_DIR})"
    return 1
  fi
  echo_info "Exited OSMT Spring app with Maven. Returning to project root (${PROJECT_DIR})"
  cd "${PROJECT_DIR}" || return 1
}

start_osmt_api_tests() {
  local osmt_jar_file; osmt_jar_file="${PROJECT_DIR}/api/target/osmt-api-*.jar"

  if [[ -n "$(find . -maxdepth 1 -name "${osmt_jar_file}" -print -quit)" ]]; then
    echo_err "Can not access ${osmt_jar_file}. Try running 'mvn clean install' first. Exiting..."
    echo
    return 1
  fi

  echo
  echo_info "Starting OSMT API Tests..."
  mvn verify -pl test -P run-api-tests
}

_remove_osmt_docker_artifacts() {
  remove_osmt_docker_artifacts_for_stack "osmt_dev"
  remove_osmt_docker_artifacts_for_stack "osmt_api_test"
  remove_osmt_docker_artifacts_for_stack "osmt_quickstart"
}

cleanup_osmt_docker_artifacts() {
  local prompt_msg; prompt_msg="$(cat <<-EOF
${INDENT}Do you want to clean up OSMT-related Docker images and volumes?
${INDENT}This step will delete data from local OSMT Quickstart and Development configurations.

${INDENT}Please answer 'y' to proceed?
EOF
  )"
  while true; do
    echo
      echo_warn "Attention:"
      read -p "${prompt_msg}" yn
      case $yn in
          [Yy]* ) _remove_osmt_docker_artifacts; break;;
          * ) echo_info "Cancelling..."; exit;;
      esac
  done
}

usage() {
  local help_msg; help_msg="$(cat <<-'EOF'

A command line utility to simplify onboarding with OSMT development instances. This utility:
- creates starter environment files
- validates software and SDK dependencies for a local instance
- provides convenience commands for starting / stopping / cleaning up development stacks

Usage:
  osmt_cli.sh [accepts a single option]

  -i   Initialize environment files for Quickstart and Development configurations.
  -v   Validate local environment and dependencies for development.
  -q   Start the Quickstart configuration. Application and services are containerized and
       managed by docker-compose. The docker-compose stack will attach to the console, with containers
       named "osmt_quickstart".
  -d   Start the backend Development Docker stack (MySQL, ElasticSearch, Redis). docker-compose stack will
       be detached, with containers named "osmt_dev". You can review status with 'docker ps'.
  -e   Stop the detached backend Development Docker stack (MySQL, ElasticSearch, Redis).
  -s   Start the local Spring app, as built from source code. This also sources the api/osmt-dev-stack.env file
       for OAUTH2-related environment variables.
  -l   Load the static CI dataset into the local MySQL instance. This will delete all data from the MySQL database.
       This action requires the database schema to be present. Locally, this is done by Flyway when the Spring
       application starts. You may need to start the Spring application first, and you will need to reindex
       ElasticSearch to make this DB refresh available to OSMT (see below).
  -r   Start the local Spring app to reindex ElasticSearch.
  -a   Start the local API tests for OSMT. This requires a valid OSMT jar file (from a 'mvn package')
  -m   Import default BLS and O*NET metadata into local Development instance.
  -c   Surgically clean up OSMT-related Docker images and data volumes. This step will delete data from local OSMT
       Quickstart and Development configurations. It does not remove the mysql/redis/elasticsearch images, as
       those may be available locally for other purposes.
  -h   Show this help message.

EOF
  )"
  echo "${help_msg}"
  echo
}

declare script_dir; script_dir=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
source "${script_dir}/bin/lib/common.sh" || exit 135

QUICKSTART_ENV_FILE="${PROJECT_DIR}/osmt-quickstart.env"
DEV_ENV_FILE="${PROJECT_DIR}/api/osmt-dev-stack.env"
APITEST_ENV_FILE="${PROJECT_DIR}/test/osmt-apitest.env"

cat <<-EOF
   ___  ___ __  __ _____   ___ _    ___
  / _ \/ __|  \/  |_   _| / __| |  |_ _|
 | (_) \__ \ |\/| | | |  | (__| |__ | |
  \___/|___/_|  |_| |_|   \___|____|___|
EOF

## all functions return. We only exit from the opts in getopts
if [[ $# == 0 ]]; then
  echo_err "$(basename "${0}") requires 1 argument"
  echo
  usage 1>&2
  echo_err "Exiting..."
  exit 135
fi

while getopts "ivqdelrasmch" flag; do
  case "${flag}" in
    i)
      init_osmt_env_files || exit 135
      exit 0
      ;;
    v)
      validate_osmt_dev_environment || exit 135
      exit 0
      ;;
    q)
      start_osmt_quickstart || exit 135
      exit 0
      ;;
    d)
      start_osmt_docker_stack "${OSMT_STACK_NAME}" || exit 135
      exit 0
      ;;
    e)
      stop_osmt_docker_stack "${OSMT_STACK_NAME}" || exit 135
      exit 0
      ;;
    s)
      start_osmt_spring_app "${OSMT_STACK_NAME}" || exit 135
      exit 0
      ;;
    l)
      load_static_ci_dataset "${OSMT_STACK_NAME}" || exit 135
      exit 0
      ;;
    r)
      start_osmt_dev_spring_app_reindex "${OSMT_STACK_NAME}" || exit 135
      exit 0
      ;;
    a)
      start_osmt_api_tests || exit 135
      exit 0
      ;;
    m)
      import_osmt_metadata "${OSMT_STACK_NAME}" || exit 135
      exit 0
      ;;
    c)
      cleanup_osmt_docker_artifacts || exit 135
      exit 0
      ;;
    h)
      usage
      exit 0
      ;;
    *)
      echo_err "Invalid argument"
      usage 1>&2
      echo_err "Exiting..."
      exit 135
      ;;
  esac
done
shift $((OPTIND-1))
