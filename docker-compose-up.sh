#!/bin/sh

#if [ "$1" = "-b" ]; then
#  ./build-app-images.sh
#  shift
#fi

./docker-compose-down.sh && \
  docker-compose up --force-recreate --remove-orphans --renew-anon-volumes "$@"
