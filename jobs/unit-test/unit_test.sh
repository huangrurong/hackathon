#!/bin/bash -ex

prepare_deps(){
    pushd ${WORKSPACE}
    mkdir -p xunit-reports
    ./build-config/tools/ENV-BUILD ./build-config/tools/app/reprove.py \
    --manifest ${MANIFEST_FILE_PATH} \
    --builddir ${WORKSPACE}/build-deps \
    --jobs 8 \
    --force \
    checkout \
    packagerefs
    popd
}

unit_test(){
    echo "Run unit test under $1"

    set +e
    # every repo must provide a unit_test.sh script
    # it's report will be saved into ./test/repo_name.xml
    ./unit_test.sh
    set -e
}

prepare_deps $1
pushd ${WORKSPACE}/build-deps/$1
unit_test $1
cp test/$1.xml ${WORKSPACE}/xunit-reports
popd
