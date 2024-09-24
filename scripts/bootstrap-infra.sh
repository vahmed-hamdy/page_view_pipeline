#!/bin/bash

set -exo pipefail

source ./scripts/common.sh

validate_bin terraform
terraformVars=""

add_terraform_variables() {
    if [[ "$GENERATOR_IMAGE" ]]; then
        terraformVars="$terraformVars -var=generator_image=$GENERATOR_IMAGE"
    fi
    if [[ "$FLINK_JOB_IMAGE" ]]; then
        terraformVars="$terraformVars -var=flink_job_image=$FLINK_JOB_IMAGE"
    fi
    if [[ "$CHECKPOINT_DIR" ]]; then
        terraformVars="$terraformVars -var=checkpoint_dir=$CHECKPOINT_DIR"
    fi
    if [[ "$SAVEPOINT_DIR" ]]; then
        terraformVars="$terraformVars -var=savepoint_dir=$SAVEPOINT_DIR"
    fi
    if [[ "$GENERATOR_THROUPUT" ]]; then
        terraformVars="$terraformVars -var=generator_throughput=$GENERATOR_THROUPUT"
    fi
    if [[ "$PARALLELISM" ]]; then
        terraformVars="$terraformVars -var=parallelism=$PARALLELISM"
    fi
    if [[ "$FLINK_DASHBOARD_PORT" ]]; then
        terraformVars="$terraformVars -var=flink_dashboard_port=$FLINK_DASHBOARD_PORT"
    fi
}
cd ./infra

terraform init
if [[ "$1" == "destroy" ]]; then
    add_terraform_variables
    terraform destroy $terraformVars --auto-approve
    exit 0
fi

add_terraform_variables
terraform apply $terraformVars  --auto-approve