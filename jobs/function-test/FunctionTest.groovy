import groovy.transform.Field;

// The default test config: ALL_TESTS (a global variable)
@Field def ALL_TESTS = [:]
ALL_TESTS["SMOKE_TEST"]=["TEST_GROUP":"smoke","label":"TEST"]
ALL_TESTS["PRESSURE_TEST"]=["TEST_GROUP":"pressure","label":"TEST"]

@Field ArrayList<String> used_resources = []

def getAllTests(){
    return ALL_TESTS
}

def getUsedResources(){
    return used_resources
}

def functionTest(String test_name, String test_type, String TEST_GROUP){
    withEnv([
        "TEST_GROUP=$TEST_GROUP",
        "KEEP_FAILURE_ENV=${env.KEEP_FAILURE_ENV}",
        "KEEP_MINUTES=${env.KEEP_MINUTES}"])
    {
        withCredentials([string(credentialsId: 'PULLER_GITHUB_TOKEN_POOL',
                        variable: 'PULLER_GITHUB_TOKEN_POOL')])
        {
            try{
                timeout(90){
                    // run test script
                    sh '''#!/bin/bash -x
                    ./hackathon-function-test/test.sh
                    '''
                }
            } finally{
                test_name = "$test_type $test_name"
                def result = "FAILURE"
                def artifact_dir = test_name.replaceAll(' ', '-') + "[$NODE_NAME]"
                try{
                    sh '''#!/bin/bash -x
                    set +e 
                    find hackathon-function-test/ -maxdepth 1 -name "*.xml" > files.txt
                    files=$( paste -s -d ' ' files.txt )
                    if [ -n "$files" ];then
                        ./hackathon/tools/app/parse_test_results.py \
                        --test-result-file "$files"  \
                        --parameters-file downstream_file
                    else
                        echo "No any test report generated"
                        echo "tests=0" > downstream_file
                    fi
                    '''
                    int failure_count = 0
                    int error_count = 0
                    int tests_count = 0
                    if(fileExists ("downstream_file")) {
                        def props = readProperties file: "downstream_file"
                        tests_count = "${props.tests}".toInteger()
                        if (tests_count > 0){
                            junit 'hackathon-function-test/*.xml'
                            failure_count = "${props.failures}".toInteger()
                            error_count = "${props.errors}".toInteger()
                            if (failure_count == 0 && error_count == 0){
                                result = "SUCCESS"
                            }
                        }
                    }
                    if(result == "FAILURE"){
                        if(KEEP_FAILURE_ENV == "true"){
                            int sleep_mins = Integer.valueOf(KEEP_MINUTES)
                            def message = "Job Name: ${env.JOB_NAME} \n" + "Build Full URL: ${env.BUILD_URL} \n" + "Status: FAILURE \n" + "Stage: $test_name \n" + "Node Name: $node_name \n" + "Reserve Duration: $sleep_mins minutes \n"
                            echo "$message"
                            slackSend "$message"
                            sleep time: sleep_mins, unit: 'MINUTES'
                        }
                    }
                }finally{
                    // Clean up test stack
                    sh '''#!/bin/bash -x
                    ./hackathon/jobs/function-test/cleanup.sh
                    '''
                    // The test_name is an argument of the method,
                    // It comes from the member variable: TESTS, for example: PRESSURE_TEST, PRESSURE_TEST.
                    // The function archiveArtifactsToTarget() will unstash the stashed files
                    // according to the member variable: TESTS
                    stash name: "$test_name", includes: "$artifact_dir/*.*, $artifact_dir/**/*.*"
                    if(result == "FAILURE"){
                        error("there are failed test cases")
                    }
                } 
            }
        }
    }
}

def archiveArtifactsToTarget(target, TESTS, test_type){
    // The function will archive artifacts to the target
    // 1. Create a directory with name target and go to it
    // 2. Unstash files according to the member variable: TESTS, for example: SMOKE_TEST, PRESSURE_TEST.
    //    The function functionTest() will stash log files after run test specified in the TESTS
    // 3. Archive the directory target
     if(TESTS == "null" || TESTS == "" || TESTS == null){
        print "No function test run, skip archiveArtifacts"
        return
    }
    List tests = Arrays.asList(TESTS.split(','))
    if(tests.size() > 0){
        dir("$target"){
            for(int i=0;i<tests.size();i++){
                try{
                    test = tests[i]
                    def test_name = "$test_type $test"
                    unstash "$test_name"
                } catch(error){
                    echo "[WARNING]Caught error during archive artifact of function test: ${error}"
                }
            }
        }
        archiveArtifacts "${target}/*.*, ${target}/**/*.*"
    }
}

return this
