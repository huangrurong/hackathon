#!/bin/bash -ex
curl --user $BINTRAY_USERNAME:$BINTRAY_API_KEY -L "$MANIFEST_FILE_URL" -o hackathon-manifest
echo "Create tag to the latest commit of repositories under the build directory"
./hackathon/tools/ENV-BUILD hackathon/tools/app/reprove.py \
--manifest hackathon-manifest \
--builddir d \
--tag-name $tag_name \
--git-credential https://github.com,JENKINSRHD_GITHUB_CREDS \
--jobs 8 \
--force \
checkout \
tag
