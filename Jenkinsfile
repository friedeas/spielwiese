pipeline {
  agent {
    kubernetes {
      yaml '''
        spec:
          containers:
          - name: maven
            image: maven:3.8.1-jdk-11
        '''		  
    }
  }		  
  stages {
    stage('Initialize'){
      steps{
        echo "PATH = ${M2_HOME}/bin:${PATH}"
        echo "M2_HOME = /opt/maven"
      }
    }
    stage('Build') { 
      steps {
        sh 'mvn -B -DskipTests clean package' 
      }
    }
  }
}

