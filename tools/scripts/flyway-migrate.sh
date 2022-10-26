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

HOST_IP=host.docker.internal

docker run --rm \
  -v $(pwd)/../flyway/sql:/flyway/sql \
  -v $(pwd)/../flyway/drivers:/flyway/drivers \
  flyway/flyway \
  -url="jdbc:mysql://${HOST_IP}:30306/adceet?allowPublicKeyRetrieval=true&useSSL=false" -user=root -password=passwd repair -X

docker run --rm \
  -v $(pwd)/../flyway/sql:/flyway/sql \
  -v $(pwd)/../flyway/drivers:/flyway/drivers \
  flyway/flyway \
  -url="jdbc:mysql://${HOST_IP}:30306/adceet?allowPublicKeyRetrieval=true&useSSL=false" -user=root -password=passwd migrate -X