#!/bin/bash

####################################################
# Run this without options to create a virtual
# environment for the current git branch
#
# Override the default virtual enviornment name through
# the single argument to script:
#
# mkenv.sh <env_name>
####################################################

set -e
PROG=${0}
echo "${PROG}"
