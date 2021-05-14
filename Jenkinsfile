def kubernetes_config = """
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: ci
    image: eclipseglsp/ci:0.0.4
    tty: true
    resources:
      limits:
        memory: "4Gi"
        cpu: "2"
      requests:
        memory: "4Gi"
        cpu: "2"
    command:
    - cat
    volumeMounts:
    - mountPath: "/home/jenkins"
      name: "jenkins-home"
      readOnly: false
    - mountPath: "/.yarn"
      name: "yarn-global"
      readOnly: false
  volumes:
  - name: "jenkins-home"
    emptyDir: {}
  - name: "yarn-global"
    emptyDir: {}
"""
pipeline {
    agent {
        kubernetes {
            label 'glsp-agent-pod'
            yaml kubernetes_config
        }
    }
    options {
        buildDiscarder logRotator(numToKeepStr: '15')
    }

    environment {
        YARN_CACHE_FOLDER = "${env.WORKSPACE}/yarn-cache"
        SPAWN_WRAP_SHIM_ROOT = "${env.WORKSPACE}"
    }
    
    stages {
        stage('Build client') {
            steps {
                container('ci') {
                    timeout(30){
                        dir('client') {
                            sh 'yarn  install --ignore-engines'
                        }
                    }
                }
            }
        }

        stage('Build Server'){
            steps{
                container('ci'){
                    timeout(30){
                        dir('server'){
                            sh "mvn clean verify -B -Dmaven.repo.local=${env.WORKSPACE}/.m2"
                        }
                    }
                }
            }
        }

        stage('Deploy (master only)') {
            when { branch 'master'}
            steps {
                build job: 'deploy-p2-ide-integration', wait: false
                build job: 'deploy-npm-ide-integration', wait: false
            }
        }
    }
}