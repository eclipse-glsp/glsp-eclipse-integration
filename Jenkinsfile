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
                     sh "mvn clean verify --batch-mode package"    
                }
            }
        }
    }

}
