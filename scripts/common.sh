#!/bin/bash

set -e


validate_env_variable() {
    if [ -z "$(eval echo \$$1)" ]; then
        echo "Environment variable $1 is not defined or is null."
        exit 1
    fi
}

validate_or_replace_env_variable() {
    if [ -z "$(eval echo \$$1)" ]; then
        export $1=$2
    fi
}

validate_bin(){
    if ! [ -x "$(command -v ${1})" ]; then
        echo "Error: ${1} is not installed." >&2
        exit 1
    fi
}

validate_path(){
    if [ ! -d "${1}" ]; then
        echo "Error: ${1} is not a valid directory." >&2
        exit 1
    fi
}

