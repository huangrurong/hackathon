#!/bin/bash -x

pushd $WORKSPACE/hackathon/jobs/BuildBaseImage/base_docker
echo $SUDO_PASSWORD |sudo -S docker build -t hackathon/pipeline .
echo $SUDO_PASSWORD |sudo -S docker login -u $DOCKERHUB_USER -p $DOCKERHUB_PASSWD
echo $SUDO_PASSWORD |sudo -S docker push hackathon/pipeline:latest
popd

pushd $WORKSPACE
echo $SUDO_PASSWORD |sudo -S docker save -o hackathon_pipeline_docker.tar hackathon/pipeline
echo $SUDO_PASSWORD |sudo -S chown $USER:$USER hackathon_pipeline_docker.tar
popd
