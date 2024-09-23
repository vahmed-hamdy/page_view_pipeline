#!/bin/bash

set -e
source ./scripts/common.sh


echo "Building Docker images..."

validate_bin java
validate_bin mvn
validate_bin docker
validate_env_variable JAVA_HOME


./scripts/build-generator-image.sh
./scripts/build-flink-job-image.sh





