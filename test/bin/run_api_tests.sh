#!/bin/bash

set -eu

declare apitest_env_file

declare OKTA_URL
declare OKTA_USERNAME
declare OKTA_PASSWORD

_get_osmt_project_dir() {
  local project_dir; project_dir="$(git rev-parse --show-toplevel 2> /dev/null)"
  echo "${project_dir}"
}

function source_osmt_apitest_env_file() {

  echo "Sourcing ${apitest_env_file}"
  set -o allexport
  source "${apitest_env_file}"
  set +o allexport
}

function get_bearer_token() {
  declare bearer_token
  declare auth_env; auth_env="${project_dir}/test/postman/osmt-auth.environment.json"

	# Running postman collections
	echo "Getting access token from OKTA ..."
  newman run "${project_dir}/test/postman/osmt-auth.postman_collection.json" \
    --env-var oktaUsername="$OKTA_USERNAME" \
    --env-var oktaPassword="$OKTA_PASSWORD" \
    --env-var oktaUrl="$OKTA_URL" \
    --env-var baseUrl="$LOCAL_URL" \
    --ignore-redirects \
    --export-environment "$auth_env"

  bearer_token="$(jq '.["values"][] | select(.key=="bearerToken") | .value' "$auth_env")"
  echo "Retrieved token:"
  echo "$bearer_token"

  echo "bearerToken=$bearer_token" > "./token.env"
  echo
}

project_dir="$(_get_osmt_project_dir)" || exit 135
apitest_env_file="${project_dir}/test/osmt-apitest.env"

source_osmt_apitest_env_file
get_bearer_token