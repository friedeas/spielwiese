pipeline {
  agent {
      kubernetes {
          inheritFrom 'maven'
      }
  }
  stages {
    stage('Environment') {
      steps {
          echo "PATH = ${PATH}"
      }
    }    
    stage('Build') { 
      steps {
        dir("/var/jenkins_home/workspace/mavent-test-pipeline") {      
        sh 'mvn -B -DskipTests clean package' 
        }
      }
    }
  }
}

