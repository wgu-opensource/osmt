#!/bin/bash

set -eu

clean_docker_stack() {
  local project_dir; project_dir="$(_get_osmt_project_dir)" || exit 135
  echo_info "Stopping OSMT ${OSMT_STACK_NAME} Docker stack"
  cd "${project_dir}/docker" || return 1
  docker-compose --file dev-stack.yml --project-name "${OSMT_STACK_NAME}" down
  rc=$?
  if [[ $rc -ne 0 ]]; then
    echo_err "Stopping OSMT ${OSMT_STACK_NAME} Docker stack failed. Exiting..."
    return 1
  fi

  echo
  echo_info "Removing OSMT-related Docker containers..."
  docker ps -aq --filter=name='osmt_api*' | xargs docker rm

  echo
  echo_info "Removing OSMT-related Docker images..."
  docker images -q --filter=reference='osmt_api*' | xargs docker rmi

  echo
  echo_info "Removing OSMT-related Docker volumes..."
  docker volume ls -q -f name=osmt_api | xargs docker volume rm -f {} &> /dev/null
}
