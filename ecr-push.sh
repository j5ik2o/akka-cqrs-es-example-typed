#!/usr/bin/env bash

export TARGET_AMD64=1

./sbt.sh write-api-server-scala/ecr:push
