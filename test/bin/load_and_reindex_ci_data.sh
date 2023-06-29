#!/bin/bash

set -eu

export OSMT_STACK_NAME=osmt_api_test

load_and_reindex_ci_data() {
  local project_dir; project_dir="$(git rev-parse --show-toplevel 2> /dev/null)"

  "${project_dir}/osmt_cli.sh" -l || exit 135

  # reindex ElasticSearch
  "${project_dir}/osmt_cli.sh" -r || exit 135
}

load_and_reindex_ci_data
