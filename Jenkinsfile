pipeline { 
    agent any  
    tools { 
        maven 'apache-maven-latest'
        jdk 'openjdk-jdk11-latest'
    }
    stages {
        stage ('Build') {
            steps {
                timeout(30){
                    dir('server/') {
                        sh "mvn clean verify --batch-mode package"
                    }
                }
            }
        }

          stage('Deploy (master only)') {
            when { branch 'master'}
            steps {
                build job: 'deploy-p2-ide-integration', wait: false
            }
        }
    }

}
