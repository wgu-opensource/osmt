#!/bin/bash

set -u

declare IMAGE_VERSION

function prepare() {
  local project_dir; project_dir="$(git rev-parse --show-toplevel)"
  if [[ -z "${project_dir}" ]]; then
    echo_err "Building the base Docker images should be run from the directory context of the osmt git repo. Exiting..."
    exit 135
  fi

  local script_dir; script_dir="${project_dir}/docker/base-images"
  if [[ ! -d "${script_dir}" ||  ! -r "${script_dir}" ]]; then
    echo_err "Can not change directory to ${script_dir}. Exiting..."
    exit 135
  fi

  echo_info "Changing directory to ${script_dir}."
  # shellcheck disable=SC2164
  cd "${script_dir}"

  echo_info "Sourcing image_version.env file"
  source image_version.env
  if [[ -z "${IMAGE_VERSION}" ]]; then
    echo_err "Building the base Docker images requires a IMAGE_VERSION env var in the image_version.env file. Exiting..."
    exit 135
  fi
  echo_info "IMAGE_VERSION is ${IMAGE_VERSION}"
}

function build() {
  local rc
  local image_name; image_name="${1}"
  if [[ -z "${image_name}" ]]; then
    echo_err "build() requires an image_name argument. Exiting..."
    exit 135
  fi

  local dockerfile_name; dockerfile_name="Dockerfile.${image_name}"
  if [[ ! -s "${dockerfile_name}" ]]; then
    echo_err "Could not locate ${dockerfile_name}. Exiting..."
    exit 135
  fi


  echo_info "Building image for ${dockerfile_name} with version ${IMAGE_VERSION}"
  docker build --file "${dockerfile_name}" --tag "wguopensource/${image_name}:${IMAGE_VERSION}" --tag "wguopensource/${image_name}:latest" .

  rc=$?
  if [[ $rc -ne 0 ]]; then
    echo_err "Error returned from docker build. Exiting..."
    exit 135
  fi
}

function echo_info() {
  echo "INFO: $@"
}

function echo_err() {
  echo "ERROR: $@" 1>&2;
}

function main() {
  echo_info "Starting the build for OSMT base Docker images"
  prepare
  build "osmt-base"
  build "osmt-build"
}

main
