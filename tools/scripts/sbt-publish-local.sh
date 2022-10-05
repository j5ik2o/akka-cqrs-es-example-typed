#!/usr/bin/env bash

set -eu

# shellcheck disable=SC2046
cd $(dirname "$0") || exit

if [[ ! -e ../../env.sh ]]; then
    echo "env.sh is not found."
    exit 1
fi

# shellcheck disable=SC2034
OUTPUT_ENV=1

source ../../env.sh

pushd ../../

sbt clean "write-api-server-${MODE}/docker:publishLocal"
sbt clean "read-model-updater-${MODE}/docker:publishLocal"

popd
