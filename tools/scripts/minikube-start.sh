#!/usr/bin/env bash

DRIVER=docker
K8S_VERSION=v1.21.0
CPUS=max
MEMORY_SIZE=max
DISK_SIZE=10g
PORTS=30030:30030,30031:30031,31566:31566

minikube start \
  --driver=$DRIVER \
  --kubernetes-version $K8S_VERSION \
  --cpus=$CPUS \
  --memory=$MEMORY_SIZE \
  --disk-size=$DISK_SIZE \
  --ports=$PORTS
