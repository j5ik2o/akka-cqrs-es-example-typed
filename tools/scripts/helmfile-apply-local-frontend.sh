#!/usr/bin/env bash

set -eu

# shellcheck disable=SC2046
cd $(dirname "$0") || exit

if [[ ! -e ../../env.sh ]]; then
    echo "env.sh is not found."
    exit 1
fi

OUTPUT_ENV=1

source ../../env.sh

export AWS_DEFAULT_REGION=$AWS_REGION
# shellcheck disable=SC2155
export ECR_DOCKER_PASSWORD=$(aws --profile "$AWS_PROFILE_TERRAFORM" ecr get-login-password --region "$AWS_REGION")

pushd ../helmfile.d

helmfile --namespace adceet --selector role=frontend --selector group=regcred -e om2eep1k-adceet-local apply

popd