// This is used to avoid the unnecessary unit test.
// It uses an instance of UnitTest to reuse function of UnitTest


String hackathon_workspace_dir
def unit_test

def setWorkspaceDir(hackathon_workspace_dir){
    this.hackathon_workspace_dir = hackathon_workspace_dir
}

def setUnitTest(){
    this.unit_test = load(hackathon_workspace_dir + "/jobs/unit-test/UnitTest.groovy")
}

def setTestRepos(){
    dir(hackathon_workspace_dir){
        unstash "$stash_manifest_name"
    }
    env.MANIFEST_FILE_PATH = hackathon_workspace_dir + "/${this.stash_manifest_path}"

    // Parse manifest to get the repositories which should run unit test
    // For a PR of lib,
    // the test_repos=["lib", "water", "wind", "earth", "fire"]
    // For an independent PR of water
    // the test_repos=["water"]
    sh '''#!/bin/bash
    pushd ''' + "$hackathon_workspace_dir" + '''
    ./tools/ENV-BUILD ./tools/app/parse_manifest.py \
    --manifest-file $MANIFEST_FILE_PATH \
    --parameters-file downstream_file
    '''
    def repos_need_unit_test = ""
    if(fileExists ('downstream_file')) {
        def props = readProperties file: 'downstream_file'
        if(props['REPOS_NEED_UNIT_TEST']) {
            repos_need_unit_test = "${props.REPOS_NEED_UNIT_TEST}"
        }
    }
    def test_repos = repos_need_unit_test.tokenize(',')
    unit_test.setTestRepos(test_repos)
}

def runTest(String manifest_name, String manifest_path, String hackathon_workspace_dir){
    setWorkspaceDir(hackathon_workspace_dir)
    setUnitTest()
    setTestRepos()
    unit_test.runTest(manifest_name, manifest_path, hackathon_workspace_dir)
}

return this
