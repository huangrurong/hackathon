import groovy.transform.Field;

@Field def TEST_TYPE = "manifest"

def generateTestBranches(function_test){
    def test_branches = [:]
    node{
        deleteDir()
        checkout scm
        def shareMethod = load("jobs/ShareMethod.groovy")
        def ALL_TESTS = function_test.getAllTests()
        def used_resources = function_test.getUsedResources()
        def TESTS = "${env.TESTS}"
        if(TESTS == "null" || TESTS == "" || TESTS == null){
            print "no test need to run"
            return null
            
        }

        List tests_group = Arrays.asList(TESTS.split(','))
        for(int i=0; i<tests_group.size(); i++){
            def test_name = tests_group[i]
            def label_name=ALL_TESTS[test_name]["label"]
            def test_group = ALL_TESTS[test_name]["TEST_GROUP"]
            test_branches["manifest $test_name"] = {
                String node_name = ""
                try{
                    lock(label: label_name,quantity:1){
                        // Occupy an avaliable resource which contains the label
                        node_name = shareMethod.occupyAvailableLockedResource(label_name, used_resources)
                        node(node_name){
                            withEnv([
                                "SKIP_PREP_DEP=false",
                                "stash_manifest_name=${env.stash_manifest_name}",
                                "stash_manifest_path=${env.stash_manifest_path}",
                                "TEST_TYPE=${TEST_TYPE}"])
                            {
                                withCredentials([string(credentialsId: 'PULLER_GITHUB_TOKEN_POOL',
                                                         variable: 'PULLER_GITHUB_TOKEN_POOL')])
                                    {
                                    deleteDir()
                                    dir("hackathon"){
                                        checkout scm
                                    }
                                    env.BUILD_CONFIG_DIR = "hackathon"
                                    // Get the manifest file
                                    unstash "$stash_manifest_name"
                                    env.MANIFEST_FILE="$stash_manifest_path"

                                    sh './hackathon/jobs/function-test/prepare_common.sh'

                                    step ([$class: 'CopyArtifact',
                                           projectName: 'build-base-docker-image',
                                           target: "$WORKSPACE"]);
                                    retry(3){
                                        // This scipts can be separated into manifest_src_prepare and common_prepare
                                        sh './hackathon/jobs/test-source-code/prepare_src_test.sh'
                                    }

                                    function_test.functionTest(test_name, TEST_TYPE, test_group)
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
    if(!(test_branches == null)){
        try{
            parallel test_branches
        } finally{
            archiveArtifacts(function_test)
        }
    }
}

def archiveArtifacts(function_test){
    def TESTS = "${env.TESTS}"
    function_test.archiveArtifactsToTarget("function-test", TESTS, TEST_TYPE)
}

return this

