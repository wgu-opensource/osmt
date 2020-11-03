#!/bin/bash

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

if [[ -z "${MYSQL_DB_URI}" ]]; then
  MISSING_ARGS=$((MISSING_ARGS + 1))
  echo "Missing environment 'MYSQL_DB_URI'"
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
  MISSING_ARGS=$((MISSING_ARGS + 1))
  echo "Missing environment 'MIGRATIONS_ENABLED'"
  echo "Valid arguments are: MIGRATIONS_ENABLED=true | MIGRATIONS_ENABLED=false"
fi

if [[ ${MISSING_ARGS} != 0 ]]; then
  echo "Missing ${MISSING_ARGS} shell variable(s), exiting.."
  exit 128
else

  JAVA_CMD="/bin/java
              -Dspring.profiles.active=${ENVIRONMENT}
              -Dapp.baseDomain=${BASE_DOMAIN}
              -Dredis.uri=${REDIS_URI}
              -Ddb.uri=${MYSQL_DB_URI}
              -Des.uri=${ELASTICSEARCH_URI}
              -Dokta.oauth2.issuer=${OAUTH_ISSUER}
              -Dokta.oauth2.clientId=${OAUTH_CLIENTID}
              -Dokta.oauth2.clientSecret=${OAUTH_CLIENTSECRET}
              -Dokta.oauth2.audience=${OAUTH_AUDIENCE}
              -Dspring.flyway.enabled=${MIGRATIONS_ENABLED}
              -jar ${BASE_DIR}/bin/osmt.jar"

  echo "Starting OSMT..."
  ${JAVA_CMD}
fi
