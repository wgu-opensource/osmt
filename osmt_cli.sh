#!/bin/bash
# shellcheck disable=SC2181

set -u

declare debug=${DEBUG:-0}
declare project_dir
declare quickstart_env_file
declare dev_env_file

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
declare OAUTH_ISSUER="${OAUTH_ISSUER:-}"
declare OAUTH_CLIENTID="${OAUTH_CLIENTID:-}"
declare OAUTH_CLIENTSECRET="${OAUTH_CLIENTSECRET:-}"
declare OAUTH_AUDIENCE="${OAUTH_AUDIENCE:-}"


# new line formatted to indent with echo_err / echo_info etc
declare -r indent="       "
declare -r nl="\n${indent}"

_get_osmt_project_dir() {
  local project_dir; project_dir="$(git rev-parse --show-toplevel 2> /dev/null)"
  if [[ -z "${project_dir}" ]]; then
    echo_err "$(basename "${0}") commands should be run from the directory context of the OSMT git repo."
    return 1
  fi

  echo "${project_dir}"
}

_cd_osmt_project_dir() {
  if [[ ! -d "${project_dir}" ||  ! -r "${project_dir}" ]]; then
    echo_err "Can not change directory to ${project_dir}."
    return 1
  else
    echo_info "Changing directory to OSMT project root (${project_dir})"
    cd "${project_dir}" || return 1
  fi
}

_source_osmt_env_file() {
  local env_file="${1}"

 # gracefully bypass sourcing env file if these 4 OAUTH values are provided
  if [[ \
      -n "${OAUTH_ISSUER}" && \
      -n "${OAUTH_CLIENTID}" &&\
      -n "${OAUTH_CLIENTSECRET}" && \
      -n "${OAUTH_AUDIENCE}" \
    ]]; then
      echo_info "Using OAUTH environment variables, not using ${env_file} env file."
      return 0
    fi

  if [[ ! -f "${env_file}" ||  ! -r "${env_file}" ]]; then
    echo_err "Can not access ${env_file}. You can initialize the environment files by running $(basename "${0}") -i$"
    return 1
  fi

  _validate_env_file "${env_file}" || return 1

  echo_info "Sourcing ${env_file}"
  set -o allexport
  # shellcheck source=/dev/null
  source "${env_file}"
  set +o allexport
}

_report_os() {
  echo_info "Reporting operating system information..."
  uname -a
}

_validate_git() {
  echo
  echo_info "Checking git..."

  # no specific version of git is required
  which git &> /dev/null
  if [[ $? -eq 0 ]]; then
    echo_info "Git detected at $(which git)"
    git --version
  else
    echo_err "git not found on path. Use 'which git' to confirm."
    echo_err "$(basename "${0}") requires git to run commands for the correct directories."
    return 1
  fi
}

_validate_docker_version() {
  echo
  echo_info "Checking Docker..."

  # docker-compose files v3.3 require Docker engine 17.06.0
  local -i req_docker_major=17
  local -i req_docker_minor=6
  local -i req_docker_patch=0

  local det_docker_version
  local -i det_docker_major
  local -i det_docker_minor
  local -i det_docker_patch

  which docker &> /dev/null
  if [[ $? -eq 0 ]]; then
    echo_info "Docker detected at $(which docker)"
  else
    echo_err "Docker not found on path. Use 'which docker' to confirm."
    return 1
  fi

  echo_info "Checking Docker version..."
  docker version
  det_docker_version="$(docker version --format '{{.Server.Version}}')"

  det_docker_version="${det_docker_version#[vV]}"
    echo_debug "1 - ${det_docker_version}"
  det_docker_major="${det_docker_version%%\.*}"
    echo_debug "2 - ${det_docker_major}"
  local tmp_minor="${det_docker_version#*.}"
    echo_debug "3 - ${tmp_minor}"
  det_docker_minor="${tmp_minor%.*}"
    echo_debug "4 - ${det_docker_minor}"
  det_docker_patch="${det_docker_version##*.}"
    echo_debug "5 - ${det_docker_patch}"

  if [[ "${det_docker_major}" -gt "${req_docker_major}" || \
      ("${det_docker_major}" -eq "${req_docker_major}" && "${det_docker_minor}" -ge "${req_docker_minor}") || \
      ("${det_docker_major}" -eq "${req_docker_major}" && "${det_docker_minor}" -eq "${req_docker_minor}" && "${det_docker_patch}" -ge "${req_docker_patch}") \
    ]]; then
    echo_info "Docker engine version ${det_docker_version} is >= ${req_docker_major}.${req_docker_minor}.${req_docker_patch}"
  else
    echo_err "Docker engine version must be >= ${req_docker_major}.${req_docker_minor}.${req_docker_patch}. Current version here is ${det_docker_version}."
    return 1
  fi
}

_validate_java_version() {
  echo
  echo_info "Checking Java..."
  # OSMT requires at least Java 11
  local -i req_java_major=11
  local det_java_version
  local -i det_java_major

  which java &> /dev/null
  if [[ $? -eq 0 ]]; then
    echo_info "Java detected at $(which java)"
  else
    echo_err "Java not found on path. Use 'which java' to confirm."
    return 1
  fi

  echo_info "Checking Java JDK for version 11 or greater..."
  det_java_version="$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)"
    echo_debug "1 - ${det_java_version}"
  det_java_version="${det_java_version#[vV]}"
    echo_debug "2 - ${det_java_version}"
  det_java_major="${det_java_version%%\.*}"
    echo_debug "3 - ${det_java_major}"

  if [[ "${det_java_major}" -ge "${req_java_major}" ]]; then
    echo_info "Java version ${det_java_version} is >= ${req_java_major}"
    return 0
  else
    echo_err "Java version must be >= ${req_java_major}. Current version detected is ${det_java_version}."
    return 1
  fi
}

_validate_mysql_client() {
  echo
  echo_info "Checking for mysql client..."

  # no specific version of mysql is required
  which mysql &> /dev/null
  if [[ $? -eq 0 ]]; then
    echo_info "MySQL client detected at $(which mysql)"
    mysql --version
  else
    echo_err "mysql not found on path. Use 'which mysql' to confirm."
    echo_err "$(basename "${0}") requires the mysql client to run commands for loading the static CI dataset."
    return 1
  fi
}

_validate_osmt_dev_dependencies() {
  local -i is_dependency_valid=0
  _validate_java_version || is_dependency_valid+=1
  echo
  echo_info "Maven version: $(mvn --version)"

  echo
  echo_info "OSMT development recommends NodeJS version v16.13.0 or greater. Maven uses an embedded copy of NodeJS v16.13.0 via frontend-maven-plugin."
  echo_info "NodeJS version: $(node --version)"
  echo
  echo_info "OSMT development recommends npm version 8.1.0 or greater. Maven uses an embedded copy of npm 8.1.0 via frontend-maven-plugin."
  echo_info "npm version: $(npm --version)"
  if [[ "${is_dependency_valid}" -ne 0 ]]; then
    echo
    echo_err "Development dependencies have not passed validation. Please inspect the ERROR messages above."
    return 1
  fi
  return "${is_dependency_valid}"
}

_validate_env_file() {
  local env_file="${1}"

  echo
  if [[ ! -f "${env_file}" ]]; then
    echo_err "Can not access ${dev_env_file}. You can initialize the environment files by running $(basename "${0}") -i$"
    return 1
  fi

  echo_info "Checking ${env_file} for invalid default values (xxxxxx)"
  if grep -q "xxxxxx" "${env_file}"; then
    echo_err "${env_file} should be updated with valid OAUTH2 values"
    return 1
  else
    echo_info "No invalid default values in ${env_file}"
  fi
}

_validate_osmt_docker_stack() {
  local stack_name="${1}"
  local -i container_count; container_count="$(docker ps -q --filter name=${stack_name}* | wc -l)"
  if [[ "${container_count}" -ne 3 ]]; then
    echo_err "OSMT ${stack_name} stack requires 3 Docker services for MySQL, ElasticSearch, and Redis. See docker ps for more information."
    return 1
  fi
}

_init_osmt_env_file() {
  local env_name="${1}"
  local env_file="${2}"
  echo
  echo_info "Initializing ${env_name} env file..."
  if [[ -f "${env_file}" ]]; then
    echo_err "Env file ${env_file} already exists.${nl}Please remove if you want to retry. Make a note of any OAUTH2 values before deleting the file."
    return 1
  else
    cp "${env_file}.example" "${env_file}"
    echo_info "Created ${env_file}.${nl}This file is ignored by git, and will not be added to git commits. Replace the 'xxxxxx' values in ${nl}${env_file} with your OAUTH2/OIDC values, shown below.${nl}Follow guidance in the 'OAuth2 and Okta Configuration' section of the project README.md."
    echo
    grep xxxxxx "${env_file}"
  fi
}

init_osmt_env_files() {
  _init_osmt_env_file "Quickstart" "${quickstart_env_file}" || return 1
  _init_osmt_env_file "Development" "${dev_env_file}" || return 1
}

validate_osmt_environment() {
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
  _validate_env_file "${quickstart_env_file}" || is_environment_valid+=1
  _validate_env_file "${dev_env_file}" || is_environment_valid+=1

  if [[ "${is_environment_valid}" -ne 0 ]]; then
    echo
    echo_err "Your environment has not passed validation. Please inspect the ERROR messages above."
    return 1
  fi
}

start_osmt_docker_stack() {
  local stack_name="${1}"
  local -i rc
  echo
  echo_info "Starting OSMT ${stack_name} Docker stack. You can stop it with $(basename "${0}") -e"
  cd "${project_dir}/docker" || return 1
  # Docker stack should receive the service port variables sourced from the shell environment
  docker-compose --file dev-stack.yml --project-name "${stack_name}" up --detach
  rc=$?
  if [[ $rc -ne 0 ]]; then
    echo_err "Starting OSMT ${stack_name} Docker stack failed. Exiting..."
    return 1
  fi
}

stop_osmt_docker_stack() {
  local stack_name="${1}"
  local -i rc
  echo
  echo_info "Stopping OSMT ${stack_name} Docker stack"
  cd "${project_dir}/docker" || return 1
  docker-compose --file dev-stack.yml --project-name "${stack_name}" down
  rc=$?
  if [[ $rc -ne 0 ]]; then
    echo_err "Stopping OSMT ${stack_name} Docker stack failed. Exiting..."
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
  _validate_env_file "${quickstart_env_file}"
  rc=$?
  if [[ $rc -ne 0 ]]; then
    echo_err "Aborting OSMT Quickstart. Exiting..."
    return 1
  fi

  echo
  echo_info "Starting OSMT Quickstart with docker-compose using osmt-quickstart.env"
  docker-compose --file docker-compose.quickstart.yml --env-file "${quickstart_env_file}" --project-name osmt_quickstart up
}

import_osmt_metadata() {
  local stack_name="${1}"
  echo
  local -i rc
  start_osmt_docker_stack "${stack_name}"
  _validate_osmt_docker_stack "${stack_name}"
  rc=$?
  if [[ $rc -ne 0 ]]; then
    echo_err "There was an issue validating your backend "${stack_name}" Docker stack. Exiting..."
    return 1
  fi

  _source_osmt_env_file "${dev_env_file}" || return 1
  _cd_osmt_project_dir || return 1
  cd api || return 1

  echo
  echo_info "Importing BLS metadata via Maven Spring Boot plug-in (Maven log output suppressed)..."
  mvn -q -Dspring-boot.run.profiles=dev,import \
    -Dspring-boot.run.arguments="--import-type=bls,--csv=${project_dir}/import/BLS-Import.csv" \
    spring-boot:run

  echo
  echo_info "Importing O*NET metadata via Maven Spring Boot plug-in (Maven log output suppressed)..."
  mvn -q -Dspring-boot.run.profiles=dev,import \
    -Dspring-boot.run.arguments="--import-type=onet,--csv=${project_dir}/import/oNet-Import.csv" \
    spring-boot:run

  echo
  echo_info "Reindexing ElasticSearch via Maven Spring Boot plug-in (Maven log output suppressed)..."
  mvn -q -Dspring-boot.run.profiles=dev,reindex spring-boot:run

  echo_info "Exited OSMT import / reindex. Returning to project root (${project_dir})"
  cd "${project_dir}" || return 1
}

start_osmt_dev_spring_app() {
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

  _source_osmt_env_file "${dev_env_file}" || return 1

  _cd_osmt_project_dir || return 1
  cd api || return 1

  local security_profile="${OSMT_SECURITY_PROFILE:-oauth2-okta}"
  echo
  echo_info "Starting OSMT via Maven Spring Boot plug-in with ${security_profile} security profile (Maven log output suppressed)..."
  mvn -q -Dspring-boot.run.profiles=dev,apiserver,"${security_profile}" spring-boot:run
  rc=$?
  if [[ $rc -ne 0 ]]; then
    echo_err "Error running OSMT Spring app with Maven. Returning to project root (${project_dir})"
    return 1
  fi
  echo_info "Exited OSMT Spring app with Maven. Returning to project root (${project_dir})"
  cd "${project_dir}" || return 1
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
  db_container_count+="$(docker ps -q --filter name=${stack_name}_db_1 | wc -l)"
  db_container_count+="$(docker ps -q --filter name=${stack_name}-db-1 | wc -l)"
  echo_debug $db_container_count
  if [[ "${db_container_count}" -ne 1 ]]; then
    echo_err "Development Docker stack MySQL container is not running as expected."
    echo_err "(${db_container_count} ${stack_name} DB containers running. Should be 1). Exiting..."
    return 1
  fi

  local sql_file="${project_dir}/test/sql/fixed_ci_dataset.sql"

  mysql \
    --host="${MYSQL_HOST}" \
    --port="${MYSQL_PORT}" \
    --database="${MYSQL_NAME}" \
    --user="${MYSQL_USER}" \
    --password="${MYSQL_PASSWORD}" < "${sql_file}"

  rc=$?
  echo
  if [[ $rc -ne 0 ]]; then
    echo_err "Error loading static CI dataset (found in ${sql_file})"
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
  _source_osmt_env_file "${dev_env_file}" || return 1
  echo
  echo_info "Starting OSMT via Maven Spring Boot plug-in to reindex ElasticSearch..."
  cd api || return 1
  mvn -Dspring-boot.run.profiles=dev,reindex spring-boot:run
  rc=$?
  if [[ $rc -ne 0 ]]; then
    echo_err "Error running OSMT Spring app with Maven. Returning to project root (${project_dir})"
    return 1
  fi
  echo_info "Exited OSMT Spring app with Maven. Returning to project root (${project_dir})"
  cd "${project_dir}" || return 1
}

_remove_osmt_docker_artifacts() {
  echo
  echo_info "Stopping OSMT-related Docker containers..."
  stop_osmt_docker_stack "osmt_dev"
  stop_osmt_docker_stack "osmt_api_test"

  echo
  echo_info "Removing OSMT-related Docker containers..."
  docker ps -aq --filter=name='osmt_*' | xargs docker rm

  echo
  echo_info "Removing OSMT-related Docker images..."
  docker images -q --filter=reference='osmt_*' | xargs docker rmi

  echo
  echo_info "Removing OSMT-related Docker volumes..."
  docker volume ls -q -f name=osmt_ | xargs docker volume rm -f {} &> /dev/null
}

cleanup_osmt_docker_artifacts() {
  local prompt_msg; prompt_msg="$(cat <<-EOF
${indent}Do you want to clean up OSMT-related Docker images and volumes?
${indent}This step will delete data from local OSMT Quickstart and Development configurations.

${indent}Please answer 'y' to proceed?
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

echo_info() {
  echo -e "INFO:  $*"
}

echo_warn() {
  echo -e "WARN:  $*"
}

echo_err() {
  echo -e "ERROR: $*" 1>&2;
}

echo_debug() {
  [[ "${debug}" -ne 0 ]] && echo -e "DEBUG: $*"
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
       named "osmt_".
  -d   Start the backend Development Docker stack (MySQL, ElasticSearch, Redis). docker-compose stack will
       be detached, with containers named "osmt_dev_". You can review status with 'docker ps'
  -e   Stop the detached backend Development Docker stack (MySQL, ElasticSearch, Redis).
  -s   Start the local Spring app, as built from source code. This also sources the api/osmt-dev-stack.env file
       for OAUTH2-related environment variables.
  -l   Load the static CI dataset into the local MySQL instance. This will delete all data from the MySQL database.
       This action requires the database schema to be present. Locally, this is done by Flyway when the Spring
       application starts. You may need to start the Spring application first, and you will need to reindex
       ElasticSearch to make this DB refresh available to OSMT (see below).
  -r   Start the local Spring app to reindex ElasticSearch.
  -m   Import default BLS and O*NET metadata into local Development instance
  -c   Surgically clean up OSMT-related Docker images and data volumes. This step will delete data from local OSMT
       Quickstart and Development configurations. It does not remove the mysql/redis/elasticsearch images, as
       those may be available locally for other purposes.
  -h   Show this help message.

EOF
  )"
  echo "${help_msg}"
  echo
}

project_dir="$(_get_osmt_project_dir)" || exit 135
quickstart_env_file="${project_dir}/osmt-quickstart.env"
dev_env_file="${project_dir}/api/osmt-dev-stack.env"

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

while getopts "ivqdelrsmch" flag; do
  case "${flag}" in
    i)
      init_osmt_env_files || exit 135
      exit 0
      ;;
    v)
      validate_osmt_environment || exit 135
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
      start_osmt_dev_spring_app "${OSMT_STACK_NAME}" || exit 135
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
