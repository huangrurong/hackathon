node{
    deleteDir()
    withEnv([
        "ghprbPullLink = ${env.ghprbPullLink}",
        "ghprbTargetBranch = ${env.ghprbTargetBranch}"
    ]) {
        dir("hackathon") {
            checkout scm
        }
        try{
            env.stash_manifest_path = "manifest"
            withCredentials([string(credentialsId: 'PULLER_GITHUB_TOKEN_POOL',
                                    variable: 'PULLER_GITHUB_TOKEN_POOL')]) {
                sh '''#!/bin/bash -ex
                ./hackathon/tools/ENV-BUILD ./hackathon/tools/app/pr_parser.py \
                --change-url $ghprbPullLink \
                --target-branch $ghprbTargetBranch \
                --puller-ghtoken-pool "${PULLER_GITHUB_TOKEN_POOL}" \
                --manifest-file-path "${stash_manifest_path}"
                '''
             }
        } finally {
            archiveArtifacts 'manifest'
            stash name: 'manifest', includes: 'manifest'
            env.stash_manifest_name = "manifest"
            env.stash_manifest_path = "manifest"
        }
    }
}
