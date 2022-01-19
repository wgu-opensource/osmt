#!/bin/bash
# shellcheck disable=SC2181

set -u

declare debug=${DEBUG:-0}
declare project_dir
declare quickstart_env_file
declare dev_env_file
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

_source_osmt_dev_env_file() {
  if [[ ! -f "${dev_env_file}" ||  ! -r "${dev_env_file}" ]]; then
    echo_err "Can not access ${dev_env_file}. You can initialize the environment files by running $(basename "${0}") -i$"
    return 1
  fi

  _validate_osmt_dev_env_file || return 1

  echo_info "Sourcing ${dev_env_file}"
  set -o allexport
  # shellcheck source=/dev/null
  source "${dev_env_file}"
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
  # OSMT requires Java 11
  local -i req_java_major=11
  local -i req_java_minor=0
  local -i req_java_patch=0

  local det_java_version
  local -i det_java_major
  local -i det_java_minor
  local -i det_java_patch

  which java &> /dev/null
  if [[ $? -eq 0 ]]; then
    echo_info "Java detected at $(which java)"
  else
    echo_err "Java not found on path. Use 'which java' to confirm."
    return 1
  fi

  echo_info "Checking Java JDK for version 11 or greater..."
  det_java_version="$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)"
  det_java_version="${det_java_version#[vV]}"
    echo_debug "1 - ${det_java_version}"
  det_java_major="${det_java_version%%\.*}"
    echo_debug "2 - ${det_java_major}"
  local tmp_minor="${det_java_version#*.}"
    echo_debug "3 - ${tmp_minor}"
  det_java_minor="${tmp_minor%.*}"
    echo_debug "4 - ${det_java_minor}"
  det_java_patch="${det_java_version##*.}"
    echo_debug "5 - ${det_java_patch}"

  if [[ "${det_java_major}" -gt "${req_java_major}" || \
      ("${det_java_major}" -eq "${req_java_major}" && "${det_java_minor}" -ge "${req_java_minor}") || \
      ("${det_java_major}" -eq "${req_java_major}" && "${det_java_minor}" -eq "${req_java_minor}" && "${det_java_patch}" -ge "${req_java_patch}") \
    ]]; then
    echo_info "Java version ${det_java_version} is >= ${req_java_major}.${req_java_minor}.${req_java_patch}"
    return 0
  else
    echo_err "Java version must be >= ${req_java_major}.${req_java_minor}.${req_java_patch}. Current version here is ${det_java_version}."
    return 1
  fi
  
}

_validate_osmt_dev_dependencies() {
  local -i is_dependency_valid=0
  _validate_java_version || is_dependency_valid+=1
  echo
  echo_info "Maven version: $(mvn --version)"

  echo
  echo_info "OSMT development recommends NodeJS version v16.13.0 or greater. Maven uses an embedded copy of NodeJS v16.13.0."
  echo_info "NodeJS version: $(node --version)"
  echo
  echo_info "OSMT development recommends npm version 8.1.0 or greater. Maven uses an embedded copy of npm 8.1.0."
  echo_info "npm version: $(npm --version)"
  if [[ "${is_dependency_valid}" -ne 0 ]]; then
    echo
    echo_err "Development dependencies have not passed validation. Please inspect the ERROR messages above."
    return 1
  fi
  return "${is_dependency_valid}"
}

_validate_osmt_quickstart_env_file() {
  _validate_env_file "Quickstart" "${quickstart_env_file}" || return 1
}

_validate_osmt_dev_env_file() {
  _validate_env_file "Development" "${dev_env_file}" || return 1
}

_validate_env_file() {
  local env_name="${1}"
  local env_file="${2}"

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

_validate_osmt_dev_docker_stack() {
  local -i dev_container_count; dev_container_count="$(docker ps -q --filter name='osmt_cli*' | wc -l)"
  if [[ "${dev_container_count}" -ne 3 ]]; then
    echo_err "Development Docker stack containers are not running."
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

  echo
  echo_info "Checking environment files used in local OSMT instances..."
  _validate_osmt_quickstart_env_file || is_environment_valid+=1
  _validate_osmt_dev_env_file || is_environment_valid+=1

  if [[ "${is_environment_valid}" -ne 0 ]]; then
    echo
    echo_err "Your environment has not passed validation. Please inspect the ERROR messages above."
    return 1
  fi
}

start_osmt_dev_docker_stack() {
  local -i rc
  echo
  echo_info "Starting OSMT Development Docker stack. You can stop it with $(basename "${0}") -e"
  cd "${project_dir}/docker" || return 1
  docker-compose --file dev-stack.yml -p osmt_cli up --detach
  rc=$?
  if [[ $rc -ne 0 ]]; then
    echo_err "Starting OSMT Development Docker stack failed. Exiting..."
    return 1
  fi
}

stop_osmt_dev_docker_stack() {
  local -i rc
  echo
  echo_info "Stopping OSMT Development Docker stack"
  cd "${project_dir}/docker" || return 1
  docker-compose --file dev-stack.yml -p osmt_cli down
  rc=$?
  if [[ $rc -ne 0 ]]; then
    echo_err "Stopping OSMT Development Docker stack failed. Exiting..."
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
  _validate_osmt_quickstart_env_file
  rc=$?
  if [[ $rc -ne 0 ]]; then
    echo_err "Aborting OSMT Quickstart. Exiting..."
    return 1
  fi

  echo
  echo_info "Starting OSMT Quickstart with docker-compose using osmt-quickstart.env"
  docker-compose --env-file "${quickstart_env_file}" up
  rc=$?
  if [[ $rc -ne 0 ]]; then
    echo_err "Starting OSMT Quickstart Docker stack failed. Exiting..."
    return 1
  fi
}

import_osmt_dev_metadata() {
  echo
  local -i rc
  _validate_osmt_dev_docker_stack
  rc=$?
  if [[ $rc -ne 0 ]]; then
    echo_err "Importing metadata requires the backend Development Docker stack. First, run $(basename "${0}") -d."
    return 1
  fi

  _cd_osmt_project_dir || return 1
  _source_osmt_dev_env_file || return 1

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
  echo
  local -i rc
  _validate_osmt_dev_docker_stack
  rc=$?
  if [[ $rc -ne 0 ]]; then
    echo_err "Starting OSMT Spring app requires the backend Development Docker stack. First, run $(basename "${0}") -d."
    return 1
  fi

  _cd_osmt_project_dir || return 1
  _source_osmt_dev_env_file || return 1
  echo
  echo_info "Starting OSMT via Maven Spring Boot plug-in (Maven log output suppressed)..."
  cd api || return 1
  mvn -q -Dspring-boot.run.profiles=dev,apiserver,oauth2-okta spring-boot:run
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
  stop_osmt_dev_docker_stack

  echo_info "Removing OSMT-related Docker images..."
  docker images -q --filter=reference='osmt_*' | xargs docker rmi

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

while getopts "ivqdesmch" flag; do
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
      start_osmt_dev_docker_stack || exit 135
      exit 0
      ;;
    e)
      stop_osmt_dev_docker_stack || exit 135
      exit 0
      ;;
    s)
      start_osmt_dev_spring_app || exit 135
      exit 0
      ;;
    m)
      import_osmt_dev_metadata || exit 135
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
