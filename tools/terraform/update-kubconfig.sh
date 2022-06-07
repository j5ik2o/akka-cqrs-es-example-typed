#!/usr/bin/env bash

set -eu

# shellcheck disable=SC2046
cd $(dirname "$0") || exit

if [[ ! -e ../../env.sh ]]; then
    echo "env.sh is not found."
    exit 1
fi

OUTPUT_ENV=1

. ../../env.sh

# shellcheck disable=SC2046
aws --profile "$AWS_PROFILE" eks update-kubeconfig --name $(./terraform-output.sh -raw eks_cluster_name)
