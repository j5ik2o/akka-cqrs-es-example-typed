#!/usr/bin/env bash

export TARGET_AMD64=1

set -eu

# shellcheck disable=SC2046
cd $(dirname "$0") || exit

if [[ ! -e ./env.sh ]]; then
    echo "env.sh is not found."
    exit 1
fi

# shellcheck disable=SC2034
OUTPUT_ENV=1

. ./env.sh

./sbt.sh write-api-server-"${MODE}"/ecr:push
