#!/bin/sh

# shellcheck disable=SC2046
cd $(dirname "$0") || exit

pushd $(pwd)
./helmfile-apply-local-dynamodb.sh
popd
pushd $(pwd)
./dynamodb-create-tables.sh -e dev
popd
pushd $(pwd)
./helmfile-apply-local-localstack.sh
popd
pushd $(pwd)
./helmfile-apply-local-mysql.sh
popd
pushd $(pwd)
./dynamodb-create-tables.sh -e dev
popd
# shellcheck disable=SC2039
pushd $(pwd)
./flyway-migrate.sh -e dev
popd
pushd $(pwd)
./helmfile-apply-local-backend.sh
popd
pushd $(pwd)
./helmfile-apply-local-frontend.sh
popd
pushd $(pwd)
./helmfile-apply-local-rmu.sh
popd
pushd $(pwd)
./helmfile-apply-local-read-api.sh
popd

