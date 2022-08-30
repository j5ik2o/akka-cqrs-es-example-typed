#!/bin/sh

API_ENDPOINT=${API_ENDPOINT:-localhost:30031}

ab -t 360 -p ./create-thread.json -T "application/json; charset=utf-8" "http://$API_ENDPOINT/threads"
