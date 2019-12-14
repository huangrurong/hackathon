#!/bin/bash -ex
################
# This script is to clone and init.sh according to Manifest
#
# Precondition:
#  $WORKSPACE : ENV Variable(Jenkins Build-in)
#  there should be on-hackathon already cloned in $WORKSPACE as folder name hackathon
#  $MANIFEST_FILE: ENV Variable. a manifest file
#  $SKIP_PREP_DEP    : $3 of this script( if preparasion function of this script to be skipped)
################

REPOS=("hackathon-wind", "hackathon-fire", "hackathon-water", "hackathon-earth")

MANIFEST_FILE="${MANIFEST_FILE}"

########################################

preparePackages() {
    pushd ${WORKSPACE}
    chmod -R 777 hackathon
    ./hackathon/tools/ENV-BUILD ./hackathon/tools/app/reprove.py \
    --manifest ${MANIFEST_FILE} \
    --builddir ${WORKSPACE}/build-deps \
    --jobs 8 \
    --force \
    checkout \
    packagerefs

    echo "[Info]Clone source code done, start to init.sh...."
    local pid_arr=()
    local cnt=0
    #### NPM Install Parallel ######
    for i in ${REPOS[@]}; do
        pushd ${WORKSPACE}/build-deps/${i}
        echo "[${i}]: running :  init.sh"
        ls -l
        ./init.sh &
        # run in background, save its PID into pid_array
        pid_arr[$cnt]=$!
        cnt=$(( $cnt + 1 ))
        popd
    done

    ## Wait for background init.sh to finish ###
    for index in $(seq 0 $(( ${#pid_arr[*]} -1 ))  );
    do
        wait ${pid_arr[$index]} # Wait for background running 'init.sh' process
        echo "[${REPOS[$index]}]: finished :  init.sh"
        if [ "$?" != "0" ] ; then
            echo "[Error] init.sh failed for repo:" ${REPOS[$index]} ", Abort !"
            exit 3
        fi
    done

    cp -r build-deps/hackathon-function-test .
    if [ -d "build-deps/hackathon" ]; then
        # hackathon from manifest has high priority
        cp -r build-deps/hackathon hackathon
    fi
    popd
}

dockerUp(){
    pushd $WORKSPACE
    echo $SUDO_PASSWORD |sudo -S docker load -i hackathon_pipeline_docker.tar
    popd

    pushd ${WORKSPACE}/hackathon/jobs/test_source_code
    cp -r ${WORKSPACE}/build-deps .
    echo $SUDO_PASSWORD |sudo -S docker build -t my/test .
    echo $SUDO_PASSWORD |sudo -S docker run --net=host -v /etc/localtime:/etc/localtime:ro -d -t my/test
    popd
}

preparePackages
dockerUp
