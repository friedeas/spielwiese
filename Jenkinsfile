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
    stage('Initialize'){
      steps{
        echo "PATH = ${M2_HOME}/bin:${PATH}"
        echo "M2_HOME = /opt/maven"
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

