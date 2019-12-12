#!/bin/bash -x

pushd $WORKSPACE/hackathon/pipelines/build-base-docker-image/base_docker
echo $SUDO_PASSWORD |sudo -S docker image build -t $DOCKERHUB_USER/hackathon:latest .
echo $SUDO_PASSWORD |sudo -S docker login -u $DOCKERHUB_USER -p $DOCKERHUB_PASSWD
echo $SUDO_PASSWORD |sudo -S docker push $DOCKERHUB_USER/hackathon:latest
popd

pushd $WORKSPACE
echo $SUDO_PASSWORD |sudo -S docker save -o hackathon_pipeline_docker.tar $DOCKERHUB_USER/hackathon:latest
echo $SUDO_PASSWORD |sudo -S chown $USER:$USER hackathon_pipeline_docker.tar
popd
