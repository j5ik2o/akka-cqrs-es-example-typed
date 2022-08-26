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

export AWS_DEFAULT_REGION=$AWS_REGION
export AWS_ACCESS_KEY_ID=x
export AWS_SECRET_ACCESS_KEY=x
export AWS_PAGER=""

DYNAMODB_ENDPOINT=localhost:31566

echo "ENDPOINT = ${DYNAMODB_ENDPOINT}"

aws dynamodb list-tables --endpoint-url "http://$DYNAMODB_ENDPOINT"