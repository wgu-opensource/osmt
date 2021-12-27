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
declare MIGRATIONS_ENABLED="${MIGRATIONS_ENABLED:-}"
#declare SKIP_METADATA_IMPORT="${SKIP_METADATA_IMPORT:-}"
declare REINDEX_ELASTICSEARCH="${REINDEX_ELASTICSEARCH:-}"
declare FRONTEND_URL="${FRONTEND_URL:-}"
declare OSMT_JAR


function validate() {
  echo_info "Validating required and optional environment variables"
  local -i missing_args=0

  local -a required_args; required_args=(
    "BASE_DOMAIN"
    "ENVIRONMENT"
    "DB_URI"
    "REDIS_URI"
    "ELASTICSEARCH_URI"
    "OAUTH_ISSUER"
    "OAUTH_CLIENTID"
    "OAUTH_CLIENTSECRET"
    "OAUTH_AUDIENCE"
  )

  for arg in "${required_args[@]}"
  do
    if [[ -z ${arg} ]]; then
      missing_args++
      echo_err "Missing environment ${arg}"
    fi
  done

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

#  if [[ -z "${SKIP_METADATA_IMPORT}" ]]; then
#    SKIP_METADATA_IMPORT=false
#    echo_info "Missing environment 'SKIP_METADATA_IMPORT'"
#    echo_info "  Defaulting to SKIP_METADATA_IMPORT=${REINDEX_ELASTICSEARCH}"
#  fi

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

function import_metadata() {
    echo_info "Importing BLS metadata"
    local java_cmd="/bin/java -jar
      -Dspring.profiles.active=dev,import
      -Ddb.uri=${DB_URI}
      -Dspring.flyway.enabled=${MIGRATIONS_ENABLED}
      /opt/osmt/bin/osmt.jar
      --csv=/opt/osmt/import/BLS-Import.csv
      --import-type=bls"

    run_cmd_with_retry "${java_cmd}"

    echo_info "Importing O*NET metadata"
    local java_cmd="/bin/java -jar
      -Dspring.profiles.active=dev,import
      -Ddb.uri=${DB_URI}
      -Dspring.flyway.enabled=${MIGRATIONS_ENABLED}
      /opt/osmt/bin/osmt.jar
      --csv=/opt/osmt/import/oNet-Import.csv
      --import-type=onet"

    run_cmd_with_retry "${java_cmd}"
}

function reindex_elasticsearch() {
    # The containerized Spring app needs an initial ElasticSearch index, or it returns 500s.
    # convert value of REINDEX_ELASTICSEARCH to lowercase and compare to "true"
    if [[ $(echo "$REINDEX_ELASTICSEARCH" | awk '{print tolower($0)}') == "true" ]]; then
      local reindex_profile_string; reindex_profile_string="$(build_reindex_profile_string "${ENVIRONMENT}")"

      echo_info "Building initial index in OSMT ElasticSearch using ${reindex_profile_string} Spring profiles..."
      java_cmd="/bin/java
        -Dspring.profiles.active=${reindex_profile_string}
        -Dredis.uri=${REDIS_URI}
        -Ddb.uri=${DB_URI}
        -Des.uri=${ELASTICSEARCH_URI}
        -Dspring.flyway.enabled=${MIGRATIONS_ENABLED}
        -jar ${OSMT_JAR}"

      run_cmd_with_retry "${java_cmd}"
    fi
}

function start_spring_app() {
    local java_cmd="/bin/java
      -Dspring.profiles.active=${ENVIRONMENT}
      -Dapp.baseDomain=${BASE_DOMAIN}
      -Dapp.frontendUrl=${FRONTEND_URL}
      -Dredis.uri=${REDIS_URI}
      -Ddb.uri=${DB_URI}
      -Des.uri=${ELASTICSEARCH_URI}
      -Dokta.oauth2.issuer=${OAUTH_ISSUER}
      -Dokta.oauth2.clientId=${OAUTH_CLIENTID}
      -Dokta.oauth2.clientSecret=${OAUTH_CLIENTSECRET}
      -Dokta.oauth2.audience=${OAUTH_AUDIENCE}
      -Dspring.flyway.enabled=${MIGRATIONS_ENABLED}
      -jar ${OSMT_JAR}"

    echo_info "Starting OSMT Spring Boot application using ${ENVIRONMENT} Spring profiles..."
    run_cmd_with_retry "${java_cmd}"
}

function run_cmd_with_retry() {
  local java_cmd="${1}"
  local return_code=-1
  set +e
  until [ ${return_code} -eq 0 ]; do
      ${java_cmd}
      return_code=$?
      if [[ ${return_code} -ne 0 ]]; then
        echo_info "Retrying in 10 seconds..."
      fi
      sleep 10
  done
  set -e
}

function build_reindex_profile_string() {
  # accept the $ENVIRONMENT env var, i.e. "test,apiserver,oauth2-okta"
  declare env_arg=${1}

  declare reindex_profile="reindex"

  # If $ENVIRONMENT contains the SDLC env from one of these Spring application profiles,
  # then append it to the reindex profile string
  declare -ar sdlc_env_list=("dev" "test" "review" "stage")

  for sdlc_env in "${sdlc_env_list[@]}"; do
    if grep -q "${sdlc_env}" <<<"${env_arg}"; then
      reindex_profile="${reindex_profile},${sdlc_env}"
    fi
  done

  echo "${reindex_profile}"
}

function echo_info() {
  echo "INFO: $*"
}

function echo_err() {
  echo "ERROR: $*" 1>&2;
}

function error_handler() {
  echo_err "Trapping at error_handler. Exiting"
}

function main() {
  local base_dir=/opt/osmt
  if [[ ! -d "${base_dir}" ||  ! -r "${base_dir}" ]]; then
    echo_err "Can not change directory to ${base_dir}. Exiting..."
    exit 135
  fi

  echo_info "Changing directory to ${base_dir}."
  cd "${base_dir}"

  OSMT_JAR="${base_dir}/bin/osmt.jar"

  validate
  import_metadata
  reindex_elasticsearch
  start_spring_app
}

trap error_handler ERR

main