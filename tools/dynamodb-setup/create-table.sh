#!/bin/sh

# shellcheck disable=SC2046
cd $(dirname "$0") && pwd

# shellcheck disable=SC2039
if [[ $# == 0 ]]; then
  echo "Parameters are empty."
  exit 1
fi

while getopts e: OPT; do
  # shellcheck disable=SC2220
  case ${OPT} in
  "e") ENV_NAME="$OPTARG" ;;
  esac
done

export AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID:-x}
export AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY:-x}
export AWS_PAGER=""

JOURNAL_TABLE_NAME=${JOURNAL_TABLE_NAME:-Journal}
JOURNAL_GSI_NAME=${JOURNAL_GSI_NAME:-GetJournalRowsIndex}
SNAPSHOT_TABLE_NAME=${SNAPSHOT_TABLE_NAME:-Snapshot}
SNAPSHOT_GSI_NAME=${JOURNAL_GSI_NAME:-GetJournalRowsIndex}

# shellcheck disable=SC2039
if [[ $ENV_NAME = "prod" ]]; then

aws dynamodb create-table \
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
    --table-name "${SNAPSHOT_TABLE_NAME}" \
    --attribute-definitions \
      AttributeName=persistence-id,AttributeType=S \
      AttributeName=sequence-nr,AttributeType=N \
    --key-schema \
      AttributeName=persistence-id,KeyType=HASH \
      AttributeName=sequence-nr,KeyType=RANGE \
    --provisioned-throughput \
      ReadCapacityUnits=10,WriteCapacityUnits=10

#aws dynamodb create-table \
#  --cli-input-json file://./account-table.json

else

  DYNAMODB_ENDPOINT=${DYNAMODB_ENDPOINT:-localhost:8000}
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

#  aws dynamodb create-table \
#    --endpoint-url "http://$DYNAMODB_ENDPOINT" \
#    --cli-input-json file://./journal-table.json

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
    ]" \
#aws dynamodb create-table \
#  --endpoint-url "http://$DYNAMODB_ENDPOINT" \
#  --cli-input-json file://./account-table.json

fi
