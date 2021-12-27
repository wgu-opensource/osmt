#!/bin/bash

set -eu

declare IMAGE_VERSION
declare DOCKERHUB_USER_ID
declare DOCKERHUB_PASSWORD

function prepare() {
  local project_dir; project_dir="$(git rev-parse --show-toplevel)"
  if [[ -z "${project_dir}" ]]; then
    echo_err "Pushing the base Docker images should be run from the directory context of the osmt git repo. Exiting..."
    exit 135
  fi

  local script_dir; script_dir="${project_dir}/docker/base-images"
  if [[ ! -d "${script_dir}" ||  ! -r "${script_dir}" ]]; then
    echo_err "Can not change directory to ${script_dir}. Exiting..."
    exit 135
  fi

  echo_info "Changing directory to ${script_dir}."
  cd "${script_dir}"

  echo_info "Sourcing .env file"
  source image_version.env
  if [[ -z "${IMAGE_VERSION}" ]]; then
    echo_err "Pushing the base Docker images requires a IMAGE_VERSION env var in the image_version.env file. Exiting..."
    exit 135
  fi
  echo_info "IMAGE_VERSION is ${IMAGE_VERSION}"
}

function authenticate() {
  local rc

  if [[ -z "${DOCKERHUB_USER_ID}" || -z "${DOCKERHUB_PASSWORD}" ]]; then
    echo_err "Pushing Docker images to DockerHub requires environment variables for DOCKERHUB_USER_ID and DOCKERHUB_PASSWORD. Exiting..."
    exit 135
  fi

  echo_info "Authenticating to DockerHub"
  docker login -u "${DOCKERHUB_USER_ID}" -p "${DOCKERHUB_PASSWORD}"

  rc=$?
  if [[ $rc -ne 0 ]]; then
    echo_err "Error authenticating to DockerHub. Exiting..."
    exit 135
  fi
}

function push() {
  local image_name; image_name="${1}"
  if [[ -z "${image_name}" ]]; then
    echo_err "push() requires an image_name argument. Exiting..."
    exit 135
  fi

 push_image "wguopensource/${image_name}:${IMAGE_VERSION}"
 push_image "wguopensource/${image_name}:latest"

}

function push_image() {
  local rc
  local tag; tag="${1}"
  echo_info "Pushing image for ${tag} to DockerHub"

  docker push "${tag}"
  rc=$?
  if [[ $rc -ne 0 ]]; then
    echo_err "Error pushing ${tag} to DockerHub. Exiting..."
    exit 135
  else
    echo_info "${tag} successfully pushed to DockerHub."
  fi
}

function logout() {
  docker logout
}

function echo_info() {
  echo "INFO: $*"
}

function echo_err() {
  echo "ERROR: $*" 1>&2;
}

function main() {
  echo_info "Starting the build for OSMT base Docker images"
  prepare
  authenticate
  push "osmt-base"
  push "osmt-build"
  logout
}

trap logout ERR

main
