def kubernetes_config = """
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: ci
    image: eclipseglsp/ci:alpine-v5.0
    resources:
      limits:
        memory: "2Gi"
        cpu: "1"
      requests:
        memory: "2Gi"
        cpu: "1"
    command:
    - cat
    tty: true
    env:
    - name: "MAVEN_OPTS"
      value: "-Duser.home=/home/jenkins"
    volumeMounts:
    - mountPath: "/home/jenkins"
      name: "jenkins-home"
      readOnly: false
    - mountPath: "/.yarn"
      name: "yarn-global"
      readOnly: false
    - name: settings-xml
      mountPath: /home/jenkins/.m2/settings.xml
      subPath: settings.xml
      readOnly: true
    - name: m2-repo
      mountPath: /home/jenkins/.m2/repository
    - name: volume-known-hosts
      mountPath: /home/jenkins/.ssh
  volumes:
  - name: "jenkins-home"
    emptyDir: {}
  - name: "yarn-global"
    emptyDir: {}
  - name: settings-xml
    secret:
      secretName: m2-secret-dir
      items:
      - key: settings.xml
        path: settings.xml
  - name: m2-repo
    emptyDir: {}
  - name: volume-known-hosts
    configMap:
      name: known-hosts
"""
pipeline {
    agent {
        kubernetes {
            label 'glsp-eclipse-agent-pod'
            yaml kubernetes_config
        }
    }
    options {
        buildDiscarder logRotator(numToKeepStr: '15')
    }

    environment {
        YARN_CACHE_FOLDER = "${env.WORKSPACE}/yarn-cache"
        SPAWN_WRAP_SHIM_ROOT = "${env.WORKSPACE}"
        EMAIL_TO= "glsp-build@eclipse.org"
    }
    
    stages {
        stage('Build client') {
            steps {
                container('ci') {
                    timeout(30){
                        dir('client') {
                            sh "yarn install"
                            script {
                                // Fail the step if there are uncommited changes to the yarn.lock file
                                if (sh(returnStatus: true, script: 'git diff --name-only | grep -q "^yarn.lock"') == 0) {
                                    echo 'The yarn.lock file has uncommited changes!'
                                    error 'The yarn.lock file has uncommited changes!'
                                } 
                            }
                        }
                    }
                }
            }
        }

        stage('Build Server') {
            steps{
                container('ci'){
                    timeout(30){
                        dir('server'){
                            sh "mvn clean verify -B -Dmaven.repo.local=${env.WORKSPACE}/.m2 -DskipTests -Dcheckstyle.skip"
                        }
                    }
                }
            }
        }

        stage('Codestyle') {
            steps{
                timeout(30){
                    container('ci') {
                        // Execute checkstyle checks
                        dir('server') {
                            sh 'mvn checkstyle:check -B'
                        }
                        // Execute eslint checks 
                        dir('client') {
                            sh 'yarn lint:ci'
                        }
                    }
                }
            }

            post{
                success{
                    // Record & publish checkstyle issues
                    recordIssues  enabledForFailure: true, publishAllIssues: true, aggregatingResults: true, 
                    tool: checkStyle(reportEncoding: 'UTF-8'),
                    qualityGates: [[threshold: 1, type: 'TOTAL', unstable: true]]

                    // Record maven,java warnings
                    recordIssues enabledForFailure: true, skipPublishingChecks:true, tools: [mavenConsole(), java()]    
                
                    // Record & publish esLint issues
                    recordIssues enabledForFailure: true, publishAllIssues: true, aggregatingResults: true, 
                    tools: [esLint(pattern: 'client/node_modules/**/*/eslint.xml')], 
                    qualityGates: [[threshold: 1, type: 'TOTAL', unstable: true]] 
                }
            }
        }

        stage('Deploy (master only)') {
            when { 
                allOf {
                    branch 'skipForRelease';
                    expression {  
                      /* Only trigger the deployment job if the changeset contains changes in 
                      the `server` or `client/packages/` directory */
                      sh(returnStatus: true, script: 'git diff --name-only HEAD^ | grep -q "^server\\|client/packages/"') == 0
                    }
                }
            }
            stages {
                stage('Deploy client (NPM)') {
                    steps { 
                        container('ci') {
                            timeout(30) {
                                dir('client') {
                                    withCredentials([string(credentialsId: 'npmjs-token', variable: 'NPM_AUTH_TOKEN')]) {
                                                sh 'printf "//registry.npmjs.org/:_authToken=${NPM_AUTH_TOKEN}\n" >> $WORKSPACE/client/.npmrc'
                                    }
                                    sh 'git config  user.email "eclipse-glsp-bot@eclipse.org"'
                                    sh 'git config  user.name "eclipse-glsp-bot"'
                                    sh 'yarn publish:next'  
                                }
                            }
                        }
                    }
                }
                stage('Deploy server (P2)') {
                    steps {
                        build job: 'deploy-ide-p2-nightly', wait: false
                    }
                
                }
            }
        }
    }

    post{
        failure {
            script {
                if (env.BRANCH_NAME == 'master') {
                    echo "Build result FAILURE: Send email notification to ${EMAIL_TO}"
                    emailext attachLog: true,
                    from: 'glsp-bot@eclipse.org',
                    body: 'Job: ${JOB_NAME}<br>Build Number: ${BUILD_NUMBER}<br>Build URL: ${BUILD_URL}',
                    mimeType: 'text/html', subject: 'Build ${JOB_NAME} (#${BUILD_NUMBER}) FAILURE', to: "${EMAIL_TO}"
                }
            }
        }
        unstable {
            script {
                if (env.BRANCH_NAME == 'master') {
                    echo "Build result UNSTABLE: Send email notification to ${EMAIL_TO}"
                    emailext attachLog: true,
                    from: 'glsp-bot@eclipse.org',
                    body: 'Job: ${JOB_NAME}<br>Build Number: ${BUILD_NUMBER}<br>Build URL: ${BUILD_URL}',
                    mimeType: 'text/html', subject: 'Build ${JOB_NAME} (#${BUILD_NUMBER}) UNSTABLE', to: "${EMAIL_TO}"
                }
            }
        }
        fixed {
            script {
                if (env.BRANCH_NAME == 'master') {
                    echo "Build back to normal: Send email notification to ${EMAIL_TO}"
                    emailext attachLog: false,
                    from: 'glsp-bot@eclipse.org',
                    body: 'Job: ${JOB_NAME}<br>Build Number: ${BUILD_NUMBER}<br>Build URL: ${BUILD_URL}',
                    mimeType: 'text/html', subject: 'Build ${JOB_NAME} back to normal (#${BUILD_NUMBER})', to: "${EMAIL_TO}"
                }
            }
        }
    }
}
