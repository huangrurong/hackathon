#!/bin/bash -ex

source ${WORKSPACE}/build-config/share_method.sh

setupVirtualEnv(){
  pushd ${WORKSPACE}/hackathon-function-test
  rm -rf .venv/hackathon
  ./mkenv.sh hackathon
  source myenv_hackathon
  popd
}

runTests() {
  set +e
  pushd ${WORKSPACE}/hackathon-function-test
    python run_tests.py ${TEST_GROUP}
  popd
  set -e
}

waitForAPI() {
  netstat -ntlp
  timeout=0
  maxto=60
  set +e
  # A url that can be used to check if the service is ready.
  url=http://localhost:9090/api/2.0/nodes
  while [ ${timeout} != ${maxto} ]; do
    wget --retry-connrefused --waitretry=1 --read-timeout=20 --timeout=15 -t 1 --continue ${url}
    if [ $? = 0 ]; then 
      break
    fi
    sleep 10
    timeout=`expr ${timeout} + 1`
  done
  set -e
  if [ ${timeout} == ${maxto} ]; then
    echo "Timed out waiting for API service (duration=`expr $maxto \* 10`s)."
    exit 1
  fi
}

setupVirtualEnv
waitForAPI
runTests
deactivate

