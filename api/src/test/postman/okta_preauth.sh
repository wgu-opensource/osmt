#!/bin/bash

declare project_dir;
project_dir="."
declare api_dev_test="${project_dir}/api_dev_test.env"

if [[ ! -f "${api_dev_test}" ||  ! -r "${api_dev_test}" ]]; then
  echo "Can not access ${api_dev_test}. Script terminating."
  exit 1
fi

echo "Sourcing ${api_dev_test}"
set -o allexport
source "${api_dev_test}"
set +o allexport

echo "Using Okta endpoint: \"${OKTA_URL}\""
echo "Using Okta username: \"${OKTA_USERNAME}\""
echo "Attempting authentication..."

declare response=$(curl -sSL -w "\n%{http_code}" \
--location "${OKTA_URL}/api/v1/authn" \
--header 'Accept: application/json' \
--header 'Content-Type: application/json' \
--data-raw '{
    "username": "'"${OKTA_USERNAME}"'",
    "password": "'"${OKTA_PASSWORD}"'",
    "options": {
        "multiOptionalFactorEnroll": false,
        "warnBeforePasswordExpired": true
    }
}')

echo
response=(${response[@]})
declare code=${response[-1]}
declare body=${response[@]::${#response[@]}-1}

echo "Status: ${code}"
echo "Response:"
echo "${body}"

if [[ "${code}" =~ ^2 ]]; then
  echo
  echo "Token:"
  declare token=$(echo ${body} | sed "s/{.*\"sessionToken\":\"\([^\"]*\).*}/\1/g")
  echo "${token}"
else
  echo "Failed to acquire Okta sesion token."
  exit 1
fi
