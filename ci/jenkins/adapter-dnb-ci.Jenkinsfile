pipeline {
    agent any

    tools {
        jdk 'jdk17'
    }

    environment {
        APP_NAME = 'adapter-dnb'
        MODULE_DIR = 'adapter-dnb'
        DOCKERFILE_PATH = 'adapter-dnb/Dockerfile'
        IMAGE_TAG = "${env.BUILD_NUMBER}"
        IMAGE_REPOSITORY = "${env.ARTIFACTORY_DOCKER_REGISTRY}/${APP_NAME}"
    }

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Test') {
            steps {
                sh 'mvn -q -f adapter-dnb/pom.xml test'
            }
        }

        stage('Package') {
            steps {
                sh 'mvn -q -f adapter-dnb/pom.xml -DskipTests package'
            }
        }

        stage('Build Image') {
            steps {
                sh '''
                  docker build \
                    -f ${DOCKERFILE_PATH} \
                    -t ${IMAGE_REPOSITORY}:${IMAGE_TAG} \
                    -t ${IMAGE_REPOSITORY}:latest \
                    ${MODULE_DIR}
                '''
            }
        }

        stage('Push Image') {
            steps {
                withCredentials([usernamePassword(
                        credentialsId: 'artifactory-docker-creds',
                        usernameVariable: 'ARTIFACTORY_USERNAME',
                        passwordVariable: 'ARTIFACTORY_PASSWORD'
                )]) {
                    sh '''
                      echo "${ARTIFACTORY_PASSWORD}" | docker login ${ARTIFACTORY_DOCKER_REGISTRY} \
                        --username "${ARTIFACTORY_USERNAME}" --password-stdin
                      docker push ${IMAGE_REPOSITORY}:${IMAGE_TAG}
                      docker push ${IMAGE_REPOSITORY}:latest
                      docker logout ${ARTIFACTORY_DOCKER_REGISTRY}
                    '''
                }
            }
        }
    }

    post {
        success {
            archiveArtifacts artifacts: 'adapter-dnb/target/*.jar', fingerprint: true
        }
    }
}
