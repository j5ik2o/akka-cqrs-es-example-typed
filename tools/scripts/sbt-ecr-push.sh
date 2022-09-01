#!/usr/bin/env bash

export TARGET_AMD64=1

set -eu

# shellcheck disable=SC2046
cd $(dirname "$0") || exit

if [[ ! -e ../../env.sh ]]; then
    echo "env.sh is not found."
    exit 1
fi

# shellcheck disable=SC2034
OUTPUT_ENV=0

source ../../env.sh

export AWS_DEFAULT_PROFILE=$AWS_PROFILE

pushd ../../

sbt clean "write-api-server-${MODE}/ecr:push"

popd
