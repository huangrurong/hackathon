# Usage
The folder includes all the scripts for the job [BuildDockerImage](http://placeholder). 
The job builds a base docker image which contains all the prerequisites for Hackathon.
Pipelines which run test with docker, such as PR Gate, download and load the base image

## Jenkinsfile
The entry point of the job "BuildDockerImage"

## base_docker
The folder includes Dockerfile for the base image

## build_base_image.sh
The script to build the image and save it as a tar file
