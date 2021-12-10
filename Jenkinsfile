def kubernetes_config = """
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: ci
    image: eclipseglsp/ci:alpine
    tty: true
    resources:
      limits:
        memory: "4096Mi"
        cpu: "2000m"
      requests:
        memory: "4096Mi"
        cpu: "2000m"
    command:
    - cat
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
    }
    
    stages {
        stage('Build client') {
            steps {
                container('ci') {
                    timeout(30){
                        dir('client') {
                            sh 'yarn install --ignore-scripts'
                            sh 'yarn  build'
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
                            sh 'yarn lint -o eslint.xml -f checkstyle'
                        }
                    }
                }
            }
        }

        stage('Deploy (master only)') {
             when { 
                allOf {
                    branch 'master';
                    expression {  
                      /* Only trigger the deployment job if the changeset contains changes in 
                      the `server` or `client/packages/` directory */
                      sh(returnStatus: true, script: 'git diff --name-only HEAD^ | grep --quiet "^server\\|client/packages/"') == 0
                    }
                }
            }
            steps {
                build job: 'deploy-p2-ide-integration', wait: false
                build job: 'deploy-npm-ide-integration', wait: false
            }
        }
    }

    post{
        always{
            container('ci') {
               
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
}
