#!/bin/bash

set -eu

declare apitest_env_file

declare OKTA_URL
declare OKTA_USERNAME
declare OKTA_PASSWORD

function setup_environment() {
	clear

	#Setting up NodeJS
	echo "Checking if NodeJS is installed ..."
	if which node > /dev/null
		node -v
    then
        echo "node is installed, skipping installation..."
    else
        echo "Installing node ..."
        npm install n
        n lts
    fi

    #checking npm version
    	echo "Checking npm version ..."
    	npm --v


    #Setting up Newman
    echo "Checking if Newman is installed ..."
	if newman -v
    then
        echo "Newman is installed, skipping..."
    else
        echo "Installing Newman ..."
        npm install newman
        newman --version
        echo "Successfly installed Newman ... "
    fi
}

_get_osmt_project_dir() {
  local project_dir; project_dir="$(git rev-parse --show-toplevel 2> /dev/null)"
  echo "${project_dir}"
}

function source_osmt_apitest_env_file() {

  echo "Sourcing ${apitest_env_file}"
  set -o allexport
  # shellcheck source=/dev/null
  source "${apitest_env_file}"
  set +o allexport

}

function run_postman_tests() {

	#postman files sanity check

	#Running postman collections
	echo "Getting access token from OKTA ..."
		newman run Auth.postman_collection.json --env-var oktaUsername=$OKTA_USERNAME --env-var oktaPassword=$OKTA_PASSWORD --env-var oktaUrl=$OKTA_URL --env-var baseUrl=$LOCAL_URL --ignore-redirects

}

project_dir="$(_get_osmt_project_dir)" || exit 135
apitest_env_file="${project_dir}/test/osmt-apitest.env"

setup_environment
source_osmt_apitest_env_file
run_postman_tests