import groovy.transform.Field;

@Field def TEST_TYPE = "docker"

def generateTestBranches(function_test){
    def test_branches = [:]
    node{
        deleteDir()
        checkout scm
        def shareMethod = load("jobs/ShareMethod.groovy")
        def ALL_TESTS = function_test.getAllTests()
        def used_resources= function_test.getUsedResources()

        def DOCKER_TESTS = "${env.DOCKER_POST_TESTS}"
        def docker_test_stack = "-stack docker"
        List docker_tests_group = Arrays.asList(DOCKER_TESTS.split(','))
        for(int i=0; i<docker_tests_group.size(); i++){
            def test_name = docker_tests_group[i]
            def label_name=ALL_TESTS[test_name]["label"]
            def test_group = ALL_TESTS[test_name]["TEST_GROUP"]
            test_branches["docker $test_name"] = {
                String node_name = ""
                try{
                    lock(label:label_name,quantity:1){
                        // Occupy an avaliable resource which contains the label
                        node_name = shareMethod.occupyAvailableLockedResource(label_name, used_resources)
                        node(node_name){
                            withEnv([
                                "DOCKER_STASH_NAME=${env.DOCKER_STASH_NAME}",
                                "DOCKER_RACKHD_IP=${env.DOCKER_RACKHD_IP}",
                                "stash_manifest_name=${env.stash_manifest_name}",
                                "stash_manifest_path=${env.stash_manifest_path}",
                                "SKIP_PREP_DEP=false",
                                "USE_VCOMPUTE=${env.USE_VCOMPUTE}",
                                "TEST_TYPE=${TEST_TYPE}"])
                            {
                                withCredentials([
                                    usernamePassword(credentialsId: 'ESXI_CREDS',
                                                    passwordVariable: 'ESXI_PASS',
                                                    usernameVariable: 'ESXI_USER'),
                                    usernamePassword(credentialsId: 'ff7ab8d2-e678-41ef-a46b-dd0e780030e1',
                                                    passwordVariable: 'SUDO_PASSWORD',
                                                    usernameVariable: 'SUDO_USER')])
                                {
                                    try{
                                        deleteDir()
                                        dir("hackathon"){
                                            checkout scm
                                        }
                                        env.BUILD_CONFIG_DIR = "hackathon"
                                        echo "Checkout hackathon-function-test for un-src test."
                                        def url = "https://github.com/changev/hackathon-function-test.git"
                                        def branch = "master"
                                        def targetDir = "hackathon-function-test"
                                        env.TEST_DIR = targetDir
                                        shareMethod.checkout(url, branch, targetDir)

                                        unstash "$DOCKER_STASH_NAME"
                                        env.DOCKER_PATH="${env.DOCKER_STASH_PATH}"
                                        env.DOCKER_RECORD_PATH="${env.DOCKER_RECORD_STASH_PATH}"

                                        sh '''#!/bin/bash
                                        ./hackathon/jobs/FunctionTest/prepare_common.sh
                                        ./hackathon/jobs/BuildDocker/prepare_docker_post_test.sh
                                        '''

                                    } catch(error){
                                        // Clean up test stack
                                        sh '''#!/bin/bash -x
                                        ./hackathon/jobs/FunctionTest/cleanup.sh
                                        '''
                                        echo "Caught: ${error}"
                                        error("Preparation of docker post test failed.")
                                    }
                                    // Start to run test
                                    function_test.functionTest(test_name, TEST_TYPE, test_group, docker_test_stack, extra_hw)
                                }
                            }
                        }
                    }
                } finally{
                    used_resources.remove(node_name)
                }
            }
        }
    }
    return test_branches
}

def runTests(function_test){
    def test_branches = generateTestBranches(function_test)
    if(test_branches.size() > 0){
        try{
            parallel test_branches
        } finally{
            archiveArtifacts(function_test)
        }
    }
}

def archiveArtifacts(function_test){
    def DOCKER_TESTS = "${env.DOCKER_POST_TESTS}"
    function_test.archiveArtifactsToTarget("DOCKER_POST_SMOKE_TEST", DOCKER_TESTS, TEST_TYPE)
}

return this
