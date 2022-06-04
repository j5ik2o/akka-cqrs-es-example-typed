#!/usr/bin/env bash

set -eu

# shellcheck disable=SC2046
cd $(dirname "$0") || exit

if [[ ! -e ../../env.sh ]]; then
    echo "terraform-env.sh is not found."
    exit 1
fi

source ../../env.sh

aws --profile "$AWS_PROFILE" eks --region "$AWS_REGION" update-kubeconfig --name "${PREFIX}-eks-${APPLICATION_NAME}"
