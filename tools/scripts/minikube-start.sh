#!/usr/bin/env bash

DRIVER=docker
K8S_VERSION=v1.21.0
CPUS=max
MEMORY_SIZE=max
DISK_SIZE=10g
PORTS=30031:30031,30131:30131,30132:30132,30033:30033,30306:30306,31566:31566

minikube start \
  --driver=$DRIVER \
  --kubernetes-version $K8S_VERSION \
  --cpus=$CPUS \
  --memory=$MEMORY_SIZE \
  --disk-size=$DISK_SIZE \
  --ports=$PORTS
