#!/bin/bash 
set -e

#
# This script is for build/push hackathon docker images.
# Parameters:
#   ${1}, WORKDIR, default: ../../, a absolute where all repos are cloned

#when run this script locally use default value
WORKDIR=${1}
BASEDIR=$(cd $(dirname "$0");pwd)
WORKDIR=${WORKDIR:=$(dirname $(dirname $BASEDIR))}

doBuild() {
    repos=$(ls)
    #Record all repo:tag for post-pushing
    repos_tags=""
    #Set an empty TAG before each build
    TAG=""

    for repo in $repos;do
        if [ ! -d $repo ]; then
            echo "Repo directory of $repo does not exist"
            popd > /dev/null 2>&1
            exit 1
        fi
        pushd $repo

            if [ ! -f "version_calculate.sh" ]; then
              echo "Repo directory of $repo has no version calculate scrpit"
              popd > /dev/null 2>&1
              exit 1
            fi
            tagCalculate() { ./version_calculate.sh; }
            TAG=:$(tagCalculate)

            echo "Building hackathon/$repo$TAG"
            # Record all tags
            repos_tags=$repos_tags"hackathon/"$repo$TAG" "

            docker build -t hackathon/$repo$TAG .

        popd
    done

    # write build list to a file for guiding image push. 
    pushd $WORKDIR
    echo "Imagename:tag list of this build is $repos_tags"
    echo $repos_tags >> build_record
    popd
}

# Build begins
pushd $WORKDIR
  #record all image:tag of each build
  if [ -f build_record ];then
      rm build_record
      touch build_record
  fi

  doBuild
  # Build ends
popd
