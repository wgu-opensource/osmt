#!/bin/bash

set -eu

declare BASE_DOMAIN="${BASE_DOMAIN:-}"
declare ENVIRONMENT="${ENVIRONMENT:-}"
declare DB_URI="${DB_URI:-}"
declare REDIS_URI="${REDIS_URI:-}"
declare ELASTICSEARCH_URI="${ELASTICSEARCH_URI:-}"
declare OAUTH_ISSUER="${OAUTH_ISSUER:-}"
declare OAUTH_CLIENTID="${OAUTH_CLIENTID:-}"
declare OAUTH_CLIENTSECRET="${OAUTH_CLIENTSECRET:-}"
declare OAUTH_AUDIENCE="${OAUTH_AUDIENCE:-}"
declare OAUTH_GOOGLE_CLIENT_ID="${OAUTH_GOOGLE_CLIENT_ID:-}"
declare OAUTH_GOOGLE_CLIENT_SECRET="${OAUTH_GOOGLE_CLIENT_SECRET:-}"
declare MIGRATIONS_ENABLED="${MIGRATIONS_ENABLED:-}"
declare SKIP_METADATA_IMPORT="${SKIP_METADATA_IMPORT:-}"
declare REINDEX_ELASTICSEARCH="${REINDEX_ELASTICSEARCH:-}"
declare FRONTEND_URL="${FRONTEND_URL:-}"
declare OSMT_JAR

function echo_info() {
  echo "[docker_entrypoint.sh]: $*"
}

function echo_err() {
  echo "[docker_entrypoint.sh] ERROR: $*" 1>&2
}

function echo_debug() {
  if [[ "${DEBUG:-}" == "1" ]] || [[ "${DEBUG:-}" == "true" ]]; then
    echo "[docker_entrypoint.sh] DEBUG: $*" >&2
  fi
}

function validate() {
  echo_info "Validating required and optional environment variables"
  echo_debug "ENVIRONMENT variable value: '${ENVIRONMENT}'"
  echo_debug "ENVIRONMENT variable length: ${#ENVIRONMENT}"
  echo_debug "SPRING_PROFILES_ACTIVE variable value: '${SPRING_PROFILES_ACTIVE:-<not set>}'"
  if [[ -n "${SPRING_PROFILES_ACTIVE:-}" ]]; then
    echo_debug "SPRING_PROFILES_ACTIVE variable length: ${#SPRING_PROFILES_ACTIVE}"
  else
    echo_debug "SPRING_PROFILES_ACTIVE variable length: 0 (not set)"
  fi
  
  local -i missing_args=0

  local -a required_args
  required_args=(
    "BASE_DOMAIN"
    "ENVIRONMENT"
    "DB_URI"
    "REDIS_URI"
    "ELASTICSEARCH_URI"
  )

  for arg in "${required_args[@]}"; do
    echo_debug "Checking required arg: ${arg}='${!arg:-<not set>}'"
    if [[ -z ${!arg} ]]; then
      missing_args++
      echo_err "Missing environment ${arg}"
    fi
  done

  # OAuth variables are optional - if missing, single-auth profile will be used
  # Note: This script runs in Docker and cannot source common.sh, so it implements
  # the same profile detection logic inline to ensure consistency.
  # IMPORTANT: This logic MUST be kept in sync with detect_security_profile() in bin/lib/common.sh
  local -i has_oauth_okta=0
  if [[ -n "${OAUTH_ISSUER:-}" ]] && [[ -n "${OAUTH_CLIENTID:-}" ]] &&
    [[ -n "${OAUTH_CLIENTSECRET:-}" ]] && [[ -n "${OAUTH_AUDIENCE:-}" ]]; then
    has_oauth_okta=1
  fi
  local -i has_oauth_google=0
  if [[ -n "${OAUTH_GOOGLE_CLIENT_ID:-}" ]] &&
    [[ -n "${OAUTH_GOOGLE_CLIENT_SECRET:-}" ]]; then
    has_oauth_google=1
  fi

  echo_debug "OAuth check: has_oauth_okta=${has_oauth_okta}, has_oauth_google=${has_oauth_google}"
  echo_debug "ENVIRONMENT before OAuth logic: '${ENVIRONMENT}'"

  if [[ ${has_oauth_okta} -eq 1 ]] || [[ ${has_oauth_google} -eq 1 ]]; then
    echo_info "OAuth credentials provided - will use oauth2 profile"
    if [[ "${ENVIRONMENT}" != *"oauth2"* ]]; then
      ENVIRONMENT="${ENVIRONMENT},oauth2"
      echo_debug "ENVIRONMENT after appending oauth2: '${ENVIRONMENT}'"
    fi
  fi
  # Add single-auth when no OAuth, or when ENABLE_SINGLE_AUTH=true (staging)
  if [[ ${has_oauth_okta} -eq 0 ]] && [[ ${has_oauth_google} -eq 0 ]]; then
    echo_info "OAuth credentials not provided - will use single-auth profile"
    if [[ "${ENVIRONMENT}" != *"single-auth"* ]]; then
      ENVIRONMENT="${ENVIRONMENT},single-auth"
      echo_info "Updated ENVIRONMENT to: ${ENVIRONMENT}"
    fi
  elif [[ "${ENABLE_SINGLE_AUTH:-}" == "true" ]] &&
    [[ "${ENVIRONMENT}" != *"single-auth"* ]]; then
    ENVIRONMENT="${ENVIRONMENT},single-auth"
    echo_info "ENABLE_SINGLE_AUTH=true - adding single-auth for staging"
  fi

  echo_debug "ENVIRONMENT after validate(): '${ENVIRONMENT}'"

  # optional args
  if [[ -z "${MIGRATIONS_ENABLED}" ]]; then
    MIGRATIONS_ENABLED=false
    echo_info "Missing environment 'MIGRATIONS_ENABLED'"
    echo_info "  Defaulting to MIGRATIONS_ENABLED=${MIGRATIONS_ENABLED}"
  fi

  if [[ -z "${REINDEX_ELASTICSEARCH}" ]]; then
    REINDEX_ELASTICSEARCH=false
    echo_info "Missing environment 'REINDEX_ELASTICSEARCH'"
    echo_info "  Defaulting to REINDEX_ELASTICSEARCH=${REINDEX_ELASTICSEARCH}"
  fi

  if [[ -z "${SKIP_METADATA_IMPORT}" ]]; then
    SKIP_METADATA_IMPORT=false
    echo_info "Missing environment 'SKIP_METADATA_IMPORT'"
    echo_info "  Defaulting to SKIP_METADATA_IMPORT=${SKIP_METADATA_IMPORT}"
  fi

  if [[ -z "${FRONTEND_URL}" ]]; then
    FRONTEND_URL="http://${BASE_DOMAIN}"
    echo_info "Missing environment 'FRONTEND_URL'"
    echo_info "  Defaulting to FRONTEND_URL=${FRONTEND_URL}"
  fi

  if [[ ${missing_args} != 0 ]]; then
    echo_err "Missing ${missing_args} shell variable(s), exiting.."
    exit 135
  fi
}

function build_reindex_profile_string() {
  # accept the $ENVIRONMENT env var, i.e. "test,apiserver,oauth2"
  declare env_arg=${1}

  echo "reindex,$(get_config_profile_from_env "${env_arg}" 2>/dev/null)"
}

function get_config_profile_from_env() {
  # accept the $ENVIRONMENT env var, i.e. "test,apiserver,oauth2"
  declare env_arg=${1}

  echo_debug "get_config_profile_from_env called with: '${env_arg}'"

  # If $ENVIRONMENT contains the config profile from one of these Spring application profiles,
  # then append it to the reindex profile string
  # Valid OSMT Spring Boot profiles: dev, test, review, staging
  declare -ar config_profile_list=("dev" "test" "review" "staging")

  for config_profile in "${config_profile_list[@]}"; do
    if grep -q "${config_profile}" <<<"${env_arg}"; then
      echo_debug "Found config profile: ${config_profile}"
      echo "${config_profile}"
      return
    fi
  done

  echo_debug "No config profile found in: '${env_arg}'"
}

function import_metadata() {
  if [[ "${SKIP_METADATA_IMPORT}" == "true" ]]; then
    echo_info "Skipping BLS and O*NET metadata import"
  else
    echo_info "Importing BLS metadata"
    local config_profile
    config_profile=$(get_config_profile_from_env "${ENVIRONMENT}" 2>/dev/null)
    echo_debug "Using config profile for import: '${config_profile}'"
    local java_cmd="/bin/java -jar
      -Dspring.profiles.active=${config_profile},import
      -Ddb.uri=${DB_URI}
      -Dspring.flyway.enabled=${MIGRATIONS_ENABLED}
      /opt/osmt/bin/osmt.jar
      --csv=/opt/osmt/import/BLS-Import.csv
      --import-type=bls"

    echo_debug "BLS import java command: ${java_cmd}"
    run_cmd_with_retry "${java_cmd}"

    echo_info "Importing O*NET metadata"
    config_profile=$(get_config_profile_from_env "${ENVIRONMENT}")
    echo_debug "Using config profile for import: '${config_profile}'"
    local java_cmd="/bin/java -jar
      -Dspring.profiles.active=${config_profile},import
      -Ddb.uri=${DB_URI}
      -Dspring.flyway.enabled=${MIGRATIONS_ENABLED}
      /opt/osmt/bin/osmt.jar
      --csv=/opt/osmt/import/oNet-Import.csv
      --import-type=onet"

    echo_debug "O*NET import java command: ${java_cmd}"
    run_cmd_with_retry "${java_cmd}"
  fi
}

function reindex_elasticsearch() {
  # The containerized Spring app needs an initial ElasticSearch index, or it returns 500s.
  if [[ "${REINDEX_ELASTICSEARCH}" == "true" ]]; then
    local reindex_profile_string
    reindex_profile_string="reindex,$(get_config_profile_from_env "${ENVIRONMENT}" 2>/dev/null)"
    echo_debug "Reindex profile string: '${reindex_profile_string}'"

    echo_info "Building initial index in OSMT ElasticSearch using ${reindex_profile_string} Spring profiles..."
    java_cmd="/bin/java
      -Dspring.profiles.active=${reindex_profile_string}
      -Dredis.uri=${REDIS_URI}
      -Ddb.uri=${DB_URI}
      -Des.uri=${ELASTICSEARCH_URI}"

    # Add Elasticsearch credentials if provided (for fine-grained access control)
    echo_debug "Checking Elasticsearch credentials for reindex: ELASTICSEARCH_USERNAME=${ELASTICSEARCH_USERNAME:+SET (length: ${#ELASTICSEARCH_USERNAME})}, ELASTICSEARCH_PWD=${ELASTICSEARCH_PWD:+SET (length: ${#ELASTICSEARCH_PWD})}"
    if [[ -n "${ELASTICSEARCH_USERNAME:-}" ]] && [[ -n "${ELASTICSEARCH_PWD:-}" ]]; then
      # Quote password to handle special characters (!, {, }, #, %, ;, etc.)
      # Java system properties don't require quotes, but we need to ensure bash doesn't interpret special chars
      java_cmd="${java_cmd}
      -Des.username=\"${ELASTICSEARCH_USERNAME}\"
      -Des.password=\"${ELASTICSEARCH_PWD}\""
      echo_debug "Added Elasticsearch authentication arguments to reindex command"
    else
      echo_debug "Elasticsearch credentials not provided for reindex - ELASTICSEARCH_USERNAME=${ELASTICSEARCH_USERNAME:-<not set>}, ELASTICSEARCH_PWD=${ELASTICSEARCH_PWD:-<not set>}"
    fi

    java_cmd="${java_cmd}
      -Dspring.flyway.enabled=${MIGRATIONS_ENABLED}
      -jar ${OSMT_JAR}"

    echo_debug "Reindex java command: ${java_cmd}"
    run_cmd_with_retry "${java_cmd}"
  fi
}

function start_spring_app() {
  echo_debug "start_spring_app() called"
  echo_debug "ENVIRONMENT at start of start_spring_app(): '${ENVIRONMENT}'"
  echo_debug "ENVIRONMENT length: ${#ENVIRONMENT}"
  echo_debug "SPRING_PROFILES_ACTIVE env var: '${SPRING_PROFILES_ACTIVE:-<not set>}'"
  
  # Log all environment variables that might affect Spring profiles
  echo_debug "All environment variables containing 'PROFILE' or 'ENVIRONMENT':"
  env | grep -iE "(PROFILE|ENVIRONMENT)" | while IFS= read -r line; do
    echo_debug "  ${line}"
  done || true

  local java_cmd="/bin/java
      -Dspring.profiles.active=${ENVIRONMENT}
      -Dapp.baseDomain=${BASE_DOMAIN}
      -Dapp.frontendUrl=${FRONTEND_URL}
      -Dredis.uri=${REDIS_URI}
      -Ddb.uri=${DB_URI}
      -Des.uri=${ELASTICSEARCH_URI}"

  echo_debug "Initial java command (before OAuth/auth checks): ${java_cmd}"

  # Add Elasticsearch credentials if provided (for fine-grained access control)
  echo_debug "Checking Elasticsearch credentials: ELASTICSEARCH_USERNAME=${ELASTICSEARCH_USERNAME:+SET (length: ${#ELASTICSEARCH_USERNAME})}, ELASTICSEARCH_PWD=${ELASTICSEARCH_PWD:+SET (length: ${#ELASTICSEARCH_PWD})}"
  if [[ -n "${ELASTICSEARCH_USERNAME:-}" ]] && [[ -n "${ELASTICSEARCH_PWD:-}" ]]; then
    # Quote password to handle special characters (!, {, }, #, %, ;, etc.)
    # Java system properties don't require quotes, but we need to ensure bash doesn't interpret special chars
    java_cmd="${java_cmd}
      -Des.username=\"${ELASTICSEARCH_USERNAME}\"
      -Des.password=\"${ELASTICSEARCH_PWD}\""
    echo_debug "Added Elasticsearch authentication arguments"
  else
    echo_debug "Elasticsearch credentials not provided - ELASTICSEARCH_USERNAME=${ELASTICSEARCH_USERNAME:-<not set>}, ELASTICSEARCH_PWD=${ELASTICSEARCH_PWD:-<not set>}"
  fi

  # OAuth config passed via env; Spring Boot reads spring.security.oauth2.* from env

  # Add admin auth variables if using single-auth profile
  if [[ "${ENVIRONMENT}" == *"single-auth"* ]]; then
    echo_debug "ENVIRONMENT contains single-auth, checking for test auth vars"
    if [[ -n "${TEST_ROLE:-}" ]]; then
      java_cmd="${java_cmd}
      -DTEST_ROLE=${TEST_ROLE}"
      echo_info "Using test role: ${TEST_ROLE}"
    fi
    if [[ -n "${TEST_USER_NAME:-}" ]]; then
      java_cmd="${java_cmd}
      -DTEST_USER_NAME=${TEST_USER_NAME}"
    fi
    if [[ -n "${TEST_USER_EMAIL:-}" ]]; then
      java_cmd="${java_cmd}
      -DTEST_USER_EMAIL=${TEST_USER_EMAIL}"
    fi
  fi

  java_cmd="${java_cmd}
      -Dspring.flyway.enabled=${MIGRATIONS_ENABLED}
      -jar ${OSMT_JAR}"

  echo_debug "Final java command: ${java_cmd}"
  echo_debug "ENVIRONMENT value being passed to Spring Boot: '${ENVIRONMENT}'"
  echo_debug "System property -Dspring.profiles.active will be: '${ENVIRONMENT}'"
  
  echo_info "Starting OSMT Spring Boot application using ${ENVIRONMENT} Spring profiles..."
  run_cmd_with_retry "${java_cmd}"
}

function run_cmd_with_retry() {
  local java_cmd="${1}"
  local return_code=-1
  set +e
  until [ ${return_code} -eq 0 ]; do
    echo_debug "Executing java command (attempt)"
    ${java_cmd}
    return_code=$?
    echo_debug "Java command exit code: ${return_code}"
    if [[ ${return_code} -ne 0 ]]; then
      echo_info "Retrying in 10 seconds..."
    fi
    sleep 10
  done
  set -e
}

function error_handler() {
  echo_err "Trapping at error_handler. Exiting"
  echo_debug "Error occurred - dumping environment state:"
  echo_debug "  ENVIRONMENT='${ENVIRONMENT}'"
  echo_debug "  SPRING_PROFILES_ACTIVE='${SPRING_PROFILES_ACTIVE:-<not set>}'"
}

function main() {
  echo_debug "=== docker_entrypoint.sh starting ==="
  echo_debug "Script arguments: $*"
  echo_debug "Current working directory: $(pwd)"
  echo_debug "ENVIRONMENT at script start: '${ENVIRONMENT}'"
  echo_debug "SPRING_PROFILES_ACTIVE at script start: '${SPRING_PROFILES_ACTIVE:-<not set>}'"
  
  local base_dir=/opt/osmt
  if [[ ! -d "${base_dir}" || ! -r "${base_dir}" ]]; then
    echo_err "Can not change directory to ${base_dir}. Exiting..."
    exit 135
  fi

  echo_info "Changing directory to ${base_dir}."
  cd "${base_dir}"
  echo_debug "Changed to directory: $(pwd)"

  OSMT_JAR="${base_dir}/bin/osmt.jar"
  echo_debug "OSMT_JAR path: ${OSMT_JAR}"
  echo_debug "OSMT_JAR exists: $([ -f "${OSMT_JAR}" ] && echo 'yes' || echo 'no')"

  validate
  echo_debug "After validate(), ENVIRONMENT='${ENVIRONMENT}'"
  
  import_metadata
  echo_debug "After import_metadata(), ENVIRONMENT='${ENVIRONMENT}'"
  
  reindex_elasticsearch
  echo_debug "After reindex_elasticsearch(), ENVIRONMENT='${ENVIRONMENT}'"
  
  start_spring_app
  echo_debug "After start_spring_app(), ENVIRONMENT='${ENVIRONMENT}'"
}

trap error_handler ERR

main
