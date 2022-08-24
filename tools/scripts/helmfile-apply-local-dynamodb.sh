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

pushd ../helmfile.d

helmfile --namespace adceet --selector group=localstack -e "${PREFIX}-${APPLICATION_NAME}-local" apply

DYNAMODB_ENDPOINT=localhost:31566 \
JOURNAL_TABLE_NAME="${PREFIX}-Journal" \
JOURNAL_GSI_NAME="${PREFIX}-GetJournalRowsIndex" \
SNAPSHOT_TABLE_NAME="${PREFIX}-Snapshot" \
../dynamodb-setup/create-table.sh -e dev

# npm install -g dynamodb-admin
# DYNAMO_ENDPOINT=http://localhost:31566 dynamodb-admin

popd
