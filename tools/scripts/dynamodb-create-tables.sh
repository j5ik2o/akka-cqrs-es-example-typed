#!/bin/sh

set -eu

# shellcheck disable=SC2046
cd $(dirname "$0") || exit

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

if [[ ! -e ../../env.sh ]]; then
    echo "env.sh is not found."
    exit 1
fi

# shellcheck disable=SC2034
OUTPUT_ENV=1

# shellcheck disable=SC2039
source ../../env.sh

# shellcheck disable=SC2039
source ../dynamodb-setup/create-tables.sh -e $ENV_NAME