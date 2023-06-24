#!/bin/bash

# shellcheck disable=SC2181
# -- https://www.shellcheck.net/wiki/SC2181

set -eu

declare DEBUG=${DEBUG:-0}
declare PROJECT_DIR; PROJECT_DIR="$(git rev-parse --show-toplevel 2> /dev/null)" || \
    (echo_err "$(basename "${0}") commands use git to set directory context. Exiting..." && exit 135)
if [[ -z "${PROJECT_DIR}" ]]; then
  echo_err "$(basename "${0}") commands should be run from the directory context of the OSMT git repo. Exiting..."
  exit 135
fi

# new line formatted to indent with echo_err / echo_info etc
declare -r INDENT="       "
declare -r NL="\n${INDENT}"

source_osmt_env_file() {
  local env_file="${1}"

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

start_osmt_docker_stack() {
  local stack_name="${1}"
  local -i rc
  echo
  echo_info "Starting OSMT ${stack_name} Docker stack. You can stop it with $(basename "${0}") -e"
  cd "${PROJECT_DIR}/docker" || return 1
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
  cd "${PROJECT_DIR}/docker" || return 1
  docker-compose --file dev-stack.yml --project-name "${stack_name}" down
  rc=$?
  if [[ $rc -ne 0 ]]; then
    echo_err "Stopping OSMT ${stack_name} Docker stack failed. Exiting..."
    return 1
  fi
}

remove_osmt_docker_artifacts_for_stack() {
  local stack_name="${1}"

  stop_osmt_docker_stack "${stack_name}"

  echo_info "Removing Docker containers for ${stack_name}..."
  docker ps -aq --filter=name="${stack_name}" | xargs docker rm

  echo_info "Removing Docker images for ${stack_name}..."
  docker images -q --filter=reference="${stack_name}" | xargs docker rmi

  echo_info "Removing Docker volumes for ${stack_name}..."
  docker volume ls -q -f name="${stack_name}" | xargs docker volume rm -f {} &> /dev/null

  echo_info "Removing Docker networks for ${stack_name}..."
  docker network ls -q -f name="${stack_name}" | xargs docker network rm -f {} &> /dev/null
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
  [[ "${DEBUG}" -ne 0 ]] && echo -e "DEBUG: $*"
}


#start_osmt_spring_app_via_maven() {
#  echo
#}
#
#start_osmt_spring_app_via_java() {
#  echo
#}

_validate_env_file() {
  local env_file="${1}"

  echo
  if [[ ! -f "${env_file}" ]]; then
    echo_err "Can not access ${env_file}. You can initialize OSMT local environment files by running $(basename "${0}") -i"
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

_cd_osmt_project_dir() {
  local sub_dir="${1:-}"
  local target_dir="${PROJECT_DIR}/${sub_dir}"
  if [[ ! -d "${target_dir}" ||  ! -r "${target_dir}" ]]; then
    echo_err "Can not change directory to ${target_dir}."
    return 1
  else
    echo_info "Changing directory to ${target_dir}"
    cd "${target_dir}" || return 1
  fi
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

_validate_osmt_docker_stack() {
  local stack_name="${1}"
  local -i container_count; container_count="$(docker ps -q --filter name="${stack_name}"* | wc -l)"
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
    echo_err "Env file ${env_file} already exists.${NL}Please remove if you want to retry. Make a note of any OAUTH2 values before deleting the file."
    return 1
  else
    cp "${env_file}.example" "${env_file}"
    echo_info "Created ${env_file}.${NL}This file is ignored by git, and will not be added to git commits. Replace the 'xxxxxx' values in ${NL}${env_file} with your OAUTH2/OIDC values, shown below.${NL}Follow guidance in the 'OAuth2 and Okta Configuration' section of the project README.md."
    echo
    grep xxxxxx "${env_file}"
  fi
}

