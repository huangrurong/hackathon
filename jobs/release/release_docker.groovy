node(build_docker_node){
    dir("on-build-config"){
        checkout scm
    }
    dir("DOCKER"){
        unstash env.DOCKER_STASH_NAME
    }
    withCredentials([
        usernamePassword(credentialsId: 'hackathon-ci-docker-hub',
                         passwordVariable: 'DOCKERHUB_PASS', 
                         usernameVariable: 'DOCKERHUB_USER')]) {
        timeout(120){
            sh './on-build-config/jobs/release/release_docker.sh'
        }
    }
}

