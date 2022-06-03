#!/usr/bin/env bash

set -eu

# shellcheck disable=SC2046
cd $(dirname "$0") || exit

if [[ ! -e ../../env.sh ]]; then
    echo "env.sh is not found."
    exit 1
fi

source ../../env.sh

export AWS_PAGER=""

BUCKET_NAME="${PREFIX}-${APPLICATION_NAME}-terraform"

aws --profile "${AWS_PROFILE}" \
  s3api create-bucket \
  --bucket "$BUCKET_NAME" \
  --region "$AWS_REGION" \
  --create-bucket-configuration LocationConstraint="$AWS_REGION"
