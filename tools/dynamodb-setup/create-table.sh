#!/bin/sh

# shellcheck disable=SC2046
cd $(dirname "$0") && pwd

# shellcheck disable=SC2039
if [[ $# == 0 ]]; then
  echo "Parameters are empty."
  exit 1
fi

while getopts e: OPT
do
    # shellcheck disable=SC2220
    case ${OPT} in
        "e") ENV_NAME="$OPTARG" ;;
    esac
done

export AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID:-x}
export AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY:-x}

if [[ $ENV_NAME = "prod" ]]; then

AWS_PAGER="" \
  aws dynamodb create-table \
  --cli-input-json file://./journal-table.json

AWS_PAGER="" \
  aws dynamodb create-table \
  --cli-input-json file://./snapshot-table.json

#AWS_PAGER="" \
#  aws dynamodb create-table \
#  --cli-input-json file://./account-table.json

else

DYNAMODB_ENDPOINT=${DYNAMODB_ENDPOINT:-localhost:8000}
echo "ENDPOINT = $DYNAMODB_ENDPOINT"

AWS_PAGER="" \
  aws dynamodb create-table \
  --endpoint-url "http://$DYNAMODB_ENDPOINT" \
  --cli-input-json file://./journal-table.json

AWS_PAGER="" \
  aws dynamodb create-table \
  --endpoint-url "http://$DYNAMODB_ENDPOINT" \
  --cli-input-json file://./snapshot-table.json

#AWS_PAGER="" \
#  aws dynamodb create-table \
#  --endpoint-url "http://$DYNAMODB_ENDPOINT" \
#  --cli-input-json file://./account-table.json

fi


