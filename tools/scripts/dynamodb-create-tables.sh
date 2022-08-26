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
JOURNAL_TABLE_NAME="${PREFIX}-Journal"
JOURNAL_GSI_NAME="${PREFIX}-GetJournalRowsIndex"
SNAPSHOT_TABLE_NAME="${PREFIX}-Snapshot"
SNAPSHOT_GSI_NAME="GetSnapshotRowsIndex"

echo "ENDPOINT = ${DYNAMODB_ENDPOINT}"

aws dynamodb create-table \
  --endpoint-url "http://$DYNAMODB_ENDPOINT" \
  --table-name "${JOURNAL_TABLE_NAME}" \
  --attribute-definitions \
    AttributeName=pkey,AttributeType=S \
    AttributeName=skey,AttributeType=S \
    AttributeName=persistence-id,AttributeType=S \
    AttributeName=sequence-nr,AttributeType=N \
  --key-schema \
    AttributeName=pkey,KeyType=HASH \
    AttributeName=skey,KeyType=RANGE \
  --provisioned-throughput \
    ReadCapacityUnits=10,WriteCapacityUnits=10 \
  --global-secondary-indexes \
  "[
    {
      \"IndexName\": \"${JOURNAL_GSI_NAME}\",
      \"KeySchema\": [{\"AttributeName\":\"persistence-id\",\"KeyType\":\"HASH\"},
                      {\"AttributeName\":\"sequence-nr\",\"KeyType\":\"RANGE\"}],
      \"Projection\":{
        \"ProjectionType\":\"ALL\"
      },
      \"ProvisionedThroughput\": {
        \"ReadCapacityUnits\": 10,
        \"WriteCapacityUnits\": 10
      }
    }
  ]" \
  --stream-specification StreamEnabled=true,StreamViewType=NEW_IMAGE

aws dynamodb create-table \
  --endpoint-url "http://$DYNAMODB_ENDPOINT" \
  --table-name "${SNAPSHOT_TABLE_NAME}" \
  --attribute-definitions \
    AttributeName=pkey,AttributeType=S \
    AttributeName=skey,AttributeType=S \
    AttributeName=persistence-id,AttributeType=S \
    AttributeName=sequence-nr,AttributeType=N \
  --key-schema \
    AttributeName=pkey,KeyType=HASH \
    AttributeName=skey,KeyType=RANGE \
  --provisioned-throughput \
    ReadCapacityUnits=10,WriteCapacityUnits=10 \
  --global-secondary-indexes \
  "[
    {
      \"IndexName\": \"${SNAPSHOT_GSI_NAME}\",
      \"KeySchema\": [{\"AttributeName\":\"persistence-id\",\"KeyType\":\"HASH\"},
                      {\"AttributeName\":\"sequence-nr\",\"KeyType\":\"RANGE\"}],
      \"Projection\":{
        \"ProjectionType\":\"ALL\"
      },
      \"ProvisionedThroughput\": {
        \"ReadCapacityUnits\": 10,
        \"WriteCapacityUnits\": 10
      }
    }
  ]"