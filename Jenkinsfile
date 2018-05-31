node {
   def mvnHome
   stage('Preparation') { 
			git branch: '${BRANCH_NAME}', credentialsId: '1a70b761-a34e-4cf2-962b-7a394137ec16', url: 'https://github.com/Clayn/yagol.git'
            mvnHome = tool 'Maven'
            dir('YAGOL') {
                if (isUnix()) {
                sh "'${mvnHome}/bin/mvn' clean"
            } else {
                bat(/"${mvnHome}\bin\mvn" clean/)
            }
            }
             
        }
	dir('YAGOL') {
        
		stage('Build') {
            if (isUnix()) {
                sh "'${mvnHome}/bin/mvn' -DskipTests install"
            } else {
                bat(/"${mvnHome}\bin\mvn" -DskipTests install/)
            }
        }
        stage('Test') {
            if (isUnix()) {
                sh "'${mvnHome}/bin/mvn' -Dmaven.test.failure.ignore=true test"
            } else {
                bat(/"${mvnHome}\bin\mvn" -Dmaven.test.failure.ignore=true test/)
            }
        }
        
        stage('Reporting') {
            if (isUnix()) {
                sh "'${mvnHome}/bin/mvn' -DskipTests site"
            } else {
                bat(/"${mvnHome}\bin\mvn" -DskipTests site/)
            }
        }
        stage('Results') {
            junit allowEmptyResults: true, testResults: '**/TEST-*.xml'
            archive 'target/*.jar'
            archive 'target/*.zip'
            archive 'target/*.tar.gz'
            jacoco()
        }
   }
}