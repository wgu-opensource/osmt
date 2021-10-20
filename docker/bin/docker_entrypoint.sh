#!/bin/bash

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

BASE_DIR=/opt/osmt

cd ${BASE_DIR} || exit

MISSING_ARGS=0

if [[ -z ${BASE_DOMAIN} ]]; then
  MISSING_ARGS=$((MISSING_ARGS + 1))
  echo "Missing environment 'BASE_DOMAIN'"
fi

if [[ -z ${ENVIRONMENT} ]]; then
  MISSING_ARGS=$((MISSING_ARGS + 1))
  echo "Missing environment 'ENVIRONMENT'"
fi

if [[ -z "${DB_URI}" ]]; then
  MISSING_ARGS=$((MISSING_ARGS + 1))
  echo "Missing environment 'DB_URI'"
fi

if [[ -z "${REDIS_URI}" ]]; then
  MISSING_ARGS=$((MISSING_ARGS + 1))
  echo "Missing environment 'REDIS_URI'"
fi

if [[ -z "${ELASTICSEARCH_URI}" ]]; then
  MISSING_ARGS=$((MISSING_ARGS + 1))
  echo "Missing environment 'ELASTICSEARCH_URI'"
fi

if [[ -z "${OAUTH_ISSUER}" ]]; then
  MISSING_ARGS=$((MISSING_ARGS + 1))
  echo "Missing environment 'OAUTH_ISSUER'"
fi

if [[ -z "${OAUTH_CLIENTID}" ]]; then
  MISSING_ARGS=$((MISSING_ARGS + 1))
  echo "Missing environment 'OAUTH_CLIENTID'"
fi

if [[ -z "${OAUTH_CLIENTSECRET}" ]]; then
  MISSING_ARGS=$((MISSING_ARGS + 1))
  echo "Missing environment 'OAUTH_CLIENTSECRET'"
fi

if [[ -z "${OAUTH_AUDIENCE}" ]]; then
  MISSING_ARGS=$((MISSING_ARGS + 1))
  echo "Missing environment 'OAUTH_AUDIENCE'"
fi

if [[ -z "${MIGRATIONS_ENABLED}" ]]; then
  MIGRATIONS_ENABLED=false
  echo "Missing environment 'MIGRATIONS_ENABLED'"
  echo "  Defaulting to MIGRATIONS_ENABLED=${MIGRATIONS_ENABLED}"
fi

if [[ -z "${REINDEX_ELASTICSEARCH}" ]]; then
  REINDEX_ELASTICSEARCH=false
  echo "Missing environment 'REINDEX_ELASTICSEARCH'"
  echo "  Defaulting to REINDEX_ELASTICSEARCH=${REINDEX_ELASTICSEARCH}"
fi

if [[ -z "${FRONTEND_URL}" ]]; then
  FRONTEND_URL="http://${BASE_DOMAIN}"
  echo "Missing environment 'FRONTEND_URL'"
  echo "  Defaulting to FRONTEND_URL=${FRONTEND_URL}"
fi

if [[ ${MISSING_ARGS} != 0 ]]; then
  echo "Missing ${MISSING_ARGS} shell variable(s), exiting.."
  exit 128
fi

# The containerized Spring app needs an initial ElasticSearch index, or it returns 500s.

# convert REINDEX_ELASTICSEARCH to lowercase and compare to "true"
if [[ $(echo "$REINDEX_ELASTICSEARCH" | awk '{print tolower($0)}') == "true" ]]; then
  declare REINDEX_SPRING_PROFILE="$(build_reindex_profile_string "${ENVIRONMENT}")"

  JAVA_CMD="/bin/java
    -Dspring.profiles.active=${REINDEX_SPRING_PROFILE}
    -Dredis.uri=${REDIS_URI}
    -Ddb.uri=${DB_URI}
    -Des.uri=${ELASTICSEARCH_URI}
    -Dspring.flyway.enabled=${MIGRATIONS_ENABLED}
    -jar ${BASE_DIR}/bin/osmt.jar"

  echo "---------------------------------------------------------------------------------------------------------------------------------------"
  echo "Building initial index in OSMT ElasticSearch using ${REINDEX_SPRING_PROFILE} Spring profiles..."
  declare return_code=1
  until [ ${return_code} -eq 0 ]; do
      ${JAVA_CMD}
      return_code=$?
      if [[ ${return_code} -ne 0 ]]; then
        echo "Retrying in 10 seconds..."
      fi
      sleep 10
  done
fi


JAVA_CMD="/bin/java
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
  -jar ${BASE_DIR}/bin/osmt.jar"

echo "---------------------------------------------------------------------------------------------------------------------------------------"
echo "Starting OSMT Spring Boot application using ${ENVIRONMENT} Spring profiles..."
return_code=1
until [ ${return_code} -eq 0 ]; do
    ${JAVA_CMD}
    return_code=$?
    if [[ ${return_code} -ne 0 ]]; then
      echo "Retrying in 10 seconds..."
    fi
    sleep 10
done
