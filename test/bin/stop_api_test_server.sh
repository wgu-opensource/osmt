#!/bin/bash
# shellcheck disable=SC2181

set -eu

export DEBUG=1
export OSMT_STACK_NAME=osmt_api_test

declare project_dir

_get_osmt_project_dir() {
  local project_dir; project_dir="$(git rev-parse --show-toplevel 2> /dev/null)"
  if [[ -z "${project_dir}" ]]; then
    echo_err "$(basename "${0}") commands should be run from the directory context of the OSMT git repo."
    return 1
  fi

  echo "${project_dir}"
}

project_dir="$(_get_osmt_project_dir)" || exit 135

# stop the API test server
"${project_dir}/test/shutdown_osmt_app.sh"

# clean up API test Docker resources


