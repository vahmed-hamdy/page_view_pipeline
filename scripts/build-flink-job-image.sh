#!/bin/bash

set -exo pipefail
source ./scripts/common.sh


mvnFlags="-DskipTests"

validate_path generator
validate_or_replace_env_variable "FLINK_JOB_IMAGE" "flink-processor:latest"
cd ./flink-application

buildCmd="mvn clean package $mvnFlags"
dockerCmd="docker build -t $FLINK_JOB_IMAGE . -f DockerFile --platform linux/amd64"

eval $buildCmd
eval $dockerCmd




