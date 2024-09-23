#!/bin/bash

set -exo pipefail
source ./scripts/common.sh


mvnFlags="-DskipTests"

validate_path generator
validate_or_replace_env_variable "GENERATOR_IMAGE" "generator:latest"
cd ./generator

buildCmd="mvn clean package $mvnFlags"
dockerCmd="docker build -t $GENERATOR_IMAGE . -f DockerFile --platform linux/amd64"

eval $buildCmd
eval $dockerCmd




