#!/bin/sh

# shellcheck disable=SC2046
cd $(dirname "$0") || exit

pushd ../flyway
make build
popd
pushd ../dynamodb-setup
make build
popd
pushd $(pwd)
./helmfile-apply-local-dynamodb.sh
popd
sleep 5
pushd $(pwd)
./helmfile-apply-local-dynamodb-setup.sh
popd
pushd $(pwd)
./helmfile-apply-local-localstack.sh
popd
pushd $(pwd)
./helmfile-apply-local-mysql.sh
popd
pushd $(pwd)
./helmfile-apply-local-flyway.sh
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

