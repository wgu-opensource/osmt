#!/bin/bash

BASE_DIR=/opt/osmt

cd ${BASE_DIR} || exit

MISSING_ARGS=0

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

if [[ ${MISSING_ARGS} != 0 ]]; then
  echo "Missing ${MISSING_ARGS} shell variable(s), exiting.."
  exit 128
else
  JAVA_CMD="/bin/java
              -Dspring.profiles.active=${ENVIRONMENT}
              -Dapp.baseDomain=${ENVIRONMENT_DOMAIN_NAME}
              -Dredis.uri=${REDIS_URI}
              -Ddb.uri=${MYSQL_DB_URI}
              -Des.uri=${ELASTICSEARCH_URI}
              -jar ${BASE_DIR}/bin/osmt.jar"

  echo "Starting OSMT..."
  ${JAVA_CMD}
fi
