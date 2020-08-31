#!/bin/bash

BASE_DIR=/opt/osmt

cd ${BASE_DIR} || exit

MISSING_ARGS=0

if [[ -z ${ENVIRONMENT} ]]; then
  "${MISSING_ARGS} 'ENVIRONMENT'"
fi

if [[ -z ${MYSQL_DB_URI} ]]; then
  "${MISSING_ARGS} 'MYSQL_DB_URI'"
  exit 128
fi

if [[ -z ${REDIS_URL} ]]; then
  "${MISSING_ARGS} 'REDIS_URL'"
fi

if [[ -z ${ELASTICSEARCH_URI} ]]; then
  "${MISSING_ARGS} 'ELASTICSEARCH_URI'"
fi

if [[ ${MISSING_ARGS} != 0 ]]; then
  echo "Missing ${MISSING_ARGS} shell variable(s), exiting.."
  exit 128
else
  JAVA_CMD="/bin/java
              -Dspring.profiles.active=${ENVIRONMENT}
              -Dspring.redis.url=${REDIS_URL}
              -Ddb.uri=${MYSQL_DB_URI}
              -Des.uri=${ELASTICSEARCH_URI}
              -jar ${BASE_DIR}/bin/osmt.jar"

  echo "Starting OSMT..."
  ${JAVA_CMD}
fi