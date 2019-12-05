#!/bin/bash -e

#download manifest
curl --user $BINTRAY_CREDS -L "$MANIFEST_FILE_URL" -o hackathon-manifest
echo using artifactory : $ARTIFACTORY_URL

#clone
./hackathon/tools/ENV-BUILD ./hackathon/tools/app/reprove.py \
--manifest hackathon-manifest \
--builddir ./$CLONE_DIR \
--jobs 8 \
--force \
checkout \
packagerefs-commit

#docker images build
pushd hackathon/tools/script
./docker_build.sh $WORKSPACE/$CLONE_DIR
cp $WORKSPACE/$CLONE_DIR/build_record $WORKSPACE
popd

# save docker image to tar
image_list=`cat $WORKSPACE/$CLONE_DIR/build_record | xargs`

docker save -o hackathon_docker_images.tar $image_list

# copy build_record to current directory for stash
cp $WORKSPACE/$CLONE_DIR/build_record .
