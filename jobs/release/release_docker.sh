#!/bin/bash
set -e
# Environmental requirement:
# docker service running and docker have already logged with Hackathon Dockerhub ID,
#     cmd 'docker login', if not logged then can't push images to dockerhub
cd DOCKER
docker load -i $DOCKER_STASH_PATH
docker login -u $DOCKERHUB_USER -p $DOCKERHUB_PASS

while read -r LINE; do
        echo $LINE
        for repo_tag in $LINE; do
                echo "Pushing hackathon/$repo_tag"
        docker push $repo_tag
        if [ $? != 0 ]; then
                echo "Failed to push hackathon/$repo_tag"
            exit 1
        fi
    done
done < $DOCKER_RECORD_STASH_PATH

# Clean UP. (was in Jenkins job post-build, avoid failure impacts build status.)
set +e
set -x

clean_up(){
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

docker logout
echo "docker logout."

exit 0 # this is a workaround. to avoid the cleanup failure makes whole workflow fail.don't worry, the set -e will ensure failure captured for necessary steps(those lines before set +e)
