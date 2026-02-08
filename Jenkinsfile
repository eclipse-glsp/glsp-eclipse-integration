def kubernetes_config = """
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: ci
    image: eclipseglsp/ci:alpine-v7.1
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
        MAVEN_VERSION = "3.9.12"

    }
    
    stages {
        stage('Download & Setup Maven') {
            steps {
                container('ci') {
                    sh '''
                        curl -o maven.tar.gz -L https://downloads.apache.org/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz
                        tar -xzf maven.tar.gz -C ${WORKSPACE}
                        rm -f maven.tar.gz
                    '''
                    // Set MAVEN_HOME manually
                    script {
                        env.MAVEN_HOME = "${env.WORKSPACE}/apache-maven-${env.MAVEN_VERSION}"
                        env.PATH = "${env.MAVEN_HOME}/bin:${env.PATH}"
                    }
                    sh "echo 'MAVEN_HOME set to ${env.MAVEN_HOME}'"
                }
            }
        }
        stage('Prepare Build') {
            steps {
                container('ci') {
                    // Remove .gitignore from diagram folder to ensure artifacts are not ignored during deployment
                    sh 'rm -f server/example/org.eclipse.glsp.ide.workflow.editor/diagram/.gitignore'
                }
            }
        }
        stage('Build client') {
            steps {
                container('ci') {
                    timeout(30){
                        dir('client') {
                            sh "yarn install"
                            script {
                                // Fail the step if there are uncommited changes to the yarn.lock file
                                if (sh(returnStatus: true, script: 'git diff --name-only | grep -q "^client/yarn.lock"') == 0) {
                                    echo 'The yarn.lock file has uncommited changes!'
                                    error 'The yarn.lock file has uncommited changes!'
                                }
                            }
                        }
                        archiveArtifacts artifacts: 'client/examples/workflow-webapp/app/**', fingerprint: true
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
                }
            }
        }

        stage('Deploy (master only)') {
            // TODO: Re-enable when condition after testing
            // when {
            //     allOf {
            //         branch 'master'
            //         expression {
            //             sh(returnStatus: true, script: 'git diff --name-only HEAD^ | grep -q "^server\\|^client"') == 0
            //         }
            //     }
            // }
            steps {
                build job: 'deploy-ide-p2-nightly',
                      wait: false,
                      parameters: [
                          string(name: 'ARTIFACT_URL', value: "${env.BUILD_URL}artifact/client/examples/workflow-webapp/app/*zip*/app.zip")
                      ]
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
