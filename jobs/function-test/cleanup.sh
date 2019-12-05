#!/bin/bash -x

clean_up_docker_image(){
    # parameter : the images keyword to be delete
    keyword=$1
    images=`docker images | grep ${keyword} | awk '{print $3}' | sort | uniq`
    if [ -n "$images" ]; then
        docker rmi -f $images
    fi
}

clean_running_containers() {
    local containers=$(docker ps -a -q)
    if [ "$containers" != "" ]; then
        echo "Clean Up containers : " ${containers}
        docker stop ${containers}
        docker rm  ${containers}
    fi
}

cleanupDocker(){
  # Clean UP. (was in Jenkins job post-build, avoid failure impacts build status.)
  set +e
  set -x

  echo "Show local docker images"
  docker ps
  docker images

  echo "Stop & rm all docker running containers " 
  clean_running_containers

  echo "Clean Up all docker images in local repo"
  clean_up_docker_image none

  # clean images by order, hackathon-lib should be last one because others depends on it
  clean_up_docker_image hackathon-wind
  clean_up_docker_image hackathon-fire
  clean_up_docker_image hackathon-water
  clean_up_docker_image hackathon-earth
  clean_up_docker_image hackathon-lib
}

cleanupDocker
