#!/bin/bash

# shellcheck disable=SC2181
# -- https://www.shellcheck.net/wiki/SC2181

set -eu

declare -i DEBUG=${DEBUG:-0}

# These variables are used in GitHub Runners to provide environment variable secrets.
# For local builds, these would be in .env files, but .env files are gitignored.
# For an automated build, these values must be in the environment, as secrets.
declare OAUTH_ISSUER="${OAUTH_ISSUER:-}"
declare OAUTH_CLIENTID="${OAUTH_CLIENTID:-}"
declare OAUTH_CLIENTSECRET="${OAUTH_CLIENTSECRET:-}"
declare OAUTH_AUDIENCE="${OAUTH_AUDIENCE:-}"
declare OAUTH_GOOGLE_CLIENT_ID="${OAUTH_GOOGLE_CLIENT_ID:-}"
declare OAUTH_GOOGLE_CLIENT_SECRET="${OAUTH_GOOGLE_CLIENT_SECRET:-}"

declare OKTA_URL="${OKTA_URL:-}"
declare OKTA_USERNAME="${OKTA_USERNAME:-}"
declare OKTA_PASSWORD="${OKTA_PASSWORD:-}"

declare PROJECT_DIR; PROJECT_DIR="$(git rev-parse --show-toplevel 2> /dev/null)" || \
    (echo_err "$(basename "${0}") commands use git to set directory context. Exiting..." && exit 135)
if [[ -z "${PROJECT_DIR}" ]]; then
  echo_err "$(basename "${0}") commands should be run from the directory context of the OSMT git repo. Exiting..."
  exit 135
fi

# new line formatted to indent with echo_err / echo_info etc
declare INDENT="       "
declare NL="\n${INDENT}"

source_env_file() {
  local env_file="${1}"

  if [[ ! -f "${env_file}" ||  ! -r "${env_file}" ]]; then
    echo_err "Can not access ${env_file}. You can initialize the environment files by running './osmt_cli.sh -i'"
    return 1
  fi

  _validate_env_file "${env_file}" || return 1

  echo_info "Sourcing ${env_file}"
  set -o allexport
  # shellcheck source=/dev/null
  source "${env_file}"
  set +o allexport

  echo_debug_env
}

source_env_file_unless_provided_oauth() {
  local env_file="${1}"

 # gracefully bypass sourcing env file if these 4 OAUTH_ values are provided, i.e. as secrets
 # via build automation
  if [[ \
      -n "${OAUTH_ISSUER}" && \
      -n "${OAUTH_CLIENTID}" && \
      -n "${OAUTH_CLIENTSECRET}" && \
      -n "${OAUTH_AUDIENCE}" \
    ]]; then
      echo_info "OAUTH values are provided by environment variables. Not sourcing ${env_file} env file."
      return 0
    fi

  echo_info "OAUTH_ values are not provided by environment variables. Sourcing ${env_file} env file."
  source_env_file "${env_file}"
}

source_env_file_unless_provided_okta() {
  local env_file="${1}"

 # gracefully bypass sourcing env file if these 3 OKTA_ values are provided, i.e. as secrets
 # via build automation
  if [[ \
      -n "${OKTA_URL}" && \
      -n "${OKTA_USERNAME}" && \
      -n "${OKTA_PASSWORD}" \
    ]]; then
      echo_info "OKTA_ values are provided by environment variables. Not sourcing ${env_file} env file."
      return 0
    fi

  echo_info "Okta values are not provided by environment variables. Sourcing ${env_file} env file."
  source_env_file "${env_file}"
}

start_osmt_docker_stack() {
  local stack_name="${1}"
  local -i rc
  local docker_cmd
  echo
  echo_info "Starting OSMT ${stack_name} Docker stack. You can stop it with $(basename "${0}") -e"
  cd "${PROJECT_DIR}" || return 1
  
  # Detect docker-compose command (docker compose for newer versions, docker-compose for older)
  if command -v docker-compose &> /dev/null; then
    docker_cmd="docker-compose"
  elif docker compose version &> /dev/null; then
    docker_cmd="docker compose"
  else
    echo_err "docker-compose or 'docker compose' command not found. Please install Docker Compose."
    return 1
  fi
  
  # Docker stack should receive the service port variables sourced from the shell environment
  # Use docker-compose.yml with 'all' profile to start MySQL, Redis, and Elasticsearch services
  ${docker_cmd} --profile all --project-name "${stack_name}" up --detach db-mysql db-redis db-elasticsearch
  rc=$?
  if [[ $rc -ne 0 ]]; then
    echo_err "Starting OSMT ${stack_name} Docker stack failed. Exiting..."
    return 1
  fi

  docker ps
}

stop_osmt_docker_stack() {
  local stack_name="${1}"
  local -i rc
  local docker_cmd
  echo
  echo_info "Stopping OSMT ${stack_name} Docker stack"
  cd "${PROJECT_DIR}" || return 1
  
  # Detect docker-compose command (docker compose for newer versions, docker-compose for older)
  if command -v docker-compose &> /dev/null; then
    docker_cmd="docker-compose"
  elif docker compose version &> /dev/null; then
    docker_cmd="docker compose"
  else
    echo_err "docker-compose or 'docker compose' command not found. Please install Docker Compose."
    return 1
  fi
  
  ${docker_cmd} --profile all --project-name "${stack_name}" down
  rc=$?
  if [[ $rc -ne 0 ]]; then
    echo_err "Stopping OSMT ${stack_name} Docker stack failed. Exiting..."
    return 1
  fi
}

remove_osmt_docker_artifacts_for_stack() {
  local stack_name="${1}"
  local resource

  stop_osmt_docker_stack "${stack_name}"

  echo_info "Removing Docker containers for ${stack_name}..."
  docker ps -aq --filter=name="${stack_name}" | xargs docker rm

  echo_info "Removing Docker images for ${stack_name}..."
  docker images -q --filter=reference="${stack_name}" | xargs docker rmi

  echo_info "Removing Docker volumes for ${stack_name}..."
  docker volume ls -q -f name="${stack_name}" | xargs docker volume rm -f {} &> /dev/null

  echo_info "Removing Docker networks for ${stack_name}..."
  resource="$(docker network ls | grep "${stack_name}" | awk '{print $1}')"
  if [[ ! -z "${resource}" ]]; then
    docker network rm -f "${resource}" &> /dev/null
  fi
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
  if [[ 0 -ne "${DEBUG}" ]]; then
    echo -e "DEBUG: $*"
  fi
}

echo_debug_env() {
  echo
  echo_debug "################################################################################################################################"
  echo_debug "Current shell environment variables:"
  echo_debug "$(env | sort)"
  echo_debug "################################################################################################################################"
  echo
}

# Detect which security profile to use based on OAuth credentials
# This function provides a single source of truth for profile detection
# Used by all scripts that need to determine security profile
# Returns: "oauth2" or "single-auth"
detect_security_profile() {
  # Priority 1: If OSMT_SECURITY_PROFILE is explicitly set, use it
  if [[ -n "${OSMT_SECURITY_PROFILE:-}" ]]; then
    echo "${OSMT_SECURITY_PROFILE}"
    return 0
  fi

  # Priority 2: Check for OAuth credentials (Okta or Google)
  if [[ -n "${OAUTH_ISSUER:-}" ]] && [[ -n "${OAUTH_CLIENTID:-}" ]] &&
    [[ -n "${OAUTH_CLIENTSECRET:-}" ]] && [[ -n "${OAUTH_AUDIENCE:-}" ]]; then
    echo "oauth2"
    return 0
  fi
  if [[ -n "${OAUTH_GOOGLE_CLIENT_ID:-}" ]] &&
    [[ -n "${OAUTH_GOOGLE_CLIENT_SECRET:-}" ]]; then
    echo "oauth2"
    return 0
  fi

  # Priority 3: Default to single-auth when OAuth credentials are missing
  echo "single-auth"
}

_validate_env_file() {
  local env_file="${1}"

  echo
  if [[ ! -f "${env_file}" ]]; then
    echo_err "Can not access ${env_file}. You can initialize OSMT local environment files by running $(basename "${0}") -i"
    return 1
  fi

  echo_info "Checking ${env_file} for invalid default values (xxxxxx)"
  if grep -q "xxxxxx" "${env_file}"; then
    local security_profile; security_profile="$(detect_security_profile)"
    if [[ "${security_profile}" == "single-auth" ]]; then
      echo_warn "${env_file} contains 'xxxxxx' values, but single-auth profile will be used. OAuth values are optional."
      echo_info "No invalid default values found for single-auth mode in ${env_file}"
    else
      echo_err "${env_file} should be updated with valid OAUTH2 values"
      return 1
    fi
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
  local -i req_java_major=17
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
  if [[ -f "${PROJECT_DIR}/.sdkmanrc" ]]; then
    echo_info "Found .sdkmanrc file. To use SDKMAN for version management, run 'sdk env install' or 'sdk env'."
  fi

  echo
  echo_info "OSMT development recommends NodeJS version v18.18.2 or greater. Maven uses an embedded copy of NodeJS v18.18.2 via frontend-maven-plugin."
  echo_info "NodeJS version: $(node --version)"
  if [[ -f "${PROJECT_DIR}/.nvmrc" ]]; then
    echo_info "Found .nvmrc file. To use nvm for version management, run 'nvm install' or 'nvm use'."
  fi
  echo
  echo_info "OSMT development recommends npm version 9.8.1 or greater. Maven uses an embedded copy of npm 9.8.1 via frontend-maven-plugin."
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
    echo_warn "Env file ${env_file} already exists.${NL}Please remove if you want to recreate it. Make a note of any OAUTH2 values before deleting the file."
  else
    cp "${env_file}.example" "${env_file}"
    echo_info "Created ${env_file}.${NL}This file is ignored by git, and will not be added to git commits."
    echo_info "OAUTH2/OIDC values are optional for local development. If you leave 'xxxxxx' values,${NL}OSMT will use the 'single-auth' profile for local development.${NL}To use OAuth2, replace the 'xxxxxx' values in ${env_file} with your OAUTH2/OIDC values.${NL}Follow guidance in the 'OAuth2 and Okta Configuration' section of the project README.md."
    echo
    if grep -q xxxxxx "${env_file}"; then
      echo_info "OAuth values found in file (optional):"
      grep xxxxxx "${env_file}" || true
    fi
  fi
}

