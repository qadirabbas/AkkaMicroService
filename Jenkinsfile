pipeline{
  agent {
    dockerfile true
  }
    stages{
      stage('Example'){
        steps{
          echo 'Hello World Qadir'
          script {
                    docker.withTool('docker') {
                        docker.build('my-app:latest', 'target/docker/stage')
                    }}
         // sh 'echo customvar = $customVar'
      }
    }
  }
}


