#!/usr/bin/env bash

set -eu

# shellcheck disable=SC2046
cd $(dirname "$0") || exit

F_OPTION="-f docker-compose-api.yml"

while getopts d OPT; do
  # shellcheck disable=SC2220
  case ${OPT} in
  "d") F_OPTION="" ;;
  esac
done

if [[ ! -e ../../env.sh ]]; then
    echo "env.sh is not found."
    exit 1
fi

# shellcheck disable=SC2034
OUTPUT_ENV=1

source ../../env.sh

./docker-compose-down.sh && \
  docker-compose \
  -f docker-compose.yml $F_OPTION \
  up --force-recreate --remove-orphans --renew-anon-volumes "$@"
