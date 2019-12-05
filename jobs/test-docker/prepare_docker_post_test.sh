#!/bin/bash 
set -x
set -e

hackathon_docker_images=`ls ${DOCKER_PATH}`
# load docker images
docker load -i $hackathon_docker_images | tee ${WORKSPACE}/docker_load_output

build_record=`ls ${DOCKER_RECORD_PATH}`
image_list=`head -n 1 $build_record`

# Edit config
# Edit the test config file to use the service url of docker containers.
# End edit config

pushd $WORKSPACE/hackathon/jobs/test_docker

# Edit compose file to use the right docker images.
for repo_tag in $image_list; do
    repo=${repo_tag%:*}
    sed -i "s#${repo}.*#${repo_tag}#g" docker-compose.yml
done

mkdir -p $WORKSPACE/build-log

docker-compose -f docker-compose.yml up > $WORKSPACE/build-log/hackathon.log &
popd