def writeBackToGitHub(String library_dir, String manifest_path){
    try{
        // if previous steps all pass,  $currentBuild.result will be set to "SUCCESS" explictly in pipeline groovy code
        // if Junit plugin found test case error in previous step,  the plugin will set $currentBuild.result  to "Unstable"
        // if previous steps abort with error, the $currentBuild.result will not get chance to be set . so value is "null" here
        // ------
        //Jenkins currentBuild.result| github commit status(https://developer.github.com/v3/repos/statuses/ )
        // null                      | failure
        // failure                   | failure
        // unstable                  | failure
        // success                   | success
        if ("${currentBuild.result}" != "SUCCESS"){
            currentBuild.result = "FAILURE"
        }
        withCredentials([string(credentialsId: 'JENKINSRHD_GITHUB_TOKEN',
                variable: 'GITHUB_TOKEN')]) {
            sh """#!/bin/bash -ex
            pushd ${library_dir}
            ./jobs/write_back_github/write_back_github.sh ${library_dir} ${manifest_path} ${currentBuild.result}
            popd
            """
        }

    } catch(error){
        echo "Caught: ${error}"
    }
}
return this