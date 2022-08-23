#!/usr/bin/env bash

kubectl run debug -it --image=ubuntu --rm --restart=Never -- /usr/bin/bash
