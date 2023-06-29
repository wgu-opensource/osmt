#!/bin/bash

set -eu

export OSMT_STACK_NAME=osmt_api_test

clean_docker_stack() {
  local project_dir; project_dir="$(git rev-parse --show-toplevel 2> /dev/null)"
  echo "INFO: Stopping OSMT ${OSMT_STACK_NAME} Docker stack"
  cd "${project_dir}/docker" || return 1

  set +eE
  docker-compose --file dev-stack.yml --project-name "${OSMT_STACK_NAME}" down
  rc=$?
  if [[ $rc -ne 0 ]]; then
    echo_err "Stopping OSMT ${OSMT_STACK_NAME} Docker stack failed. Exiting..."
    return 1
  fi

  echo
  echo "INFO: Removing OSMT-related Docker containers..."
  docker ps -aq --filter=name='osmt_api*' | xargs docker rm

  echo
  echo "INFO: Removing OSMT-related Docker images..."
  docker images -q --filter=reference='osmt_api*' | xargs docker rmi

  echo
  echo "INFO: Removing OSMT-related Docker volumes..."
  docker volume ls -q -f name=osmt_api | xargs docker volume rm -f {} &> /dev/null
  set -eE
}

clean_docker_stack

echo "INFO: listing active docker containers"
docker ps -a

echo "INFO: Listing OSMT-related Docker volumes..."
docker volume ls -q -f name=osmt

