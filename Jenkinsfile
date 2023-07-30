pipeline {
    agent {
		kubernetes {
		  inheritFrom 'mypod'
		  yaml '''
		  spec:
			containers:
			- name: maven
			  image: maven:3.8.1-jdk-11
			'''		  
		}
	}		  
    stages {
        stage('Build') { 
            steps {
                sh 'mvn -B -DskipTests clean package' 
            }
        }
    }
}

