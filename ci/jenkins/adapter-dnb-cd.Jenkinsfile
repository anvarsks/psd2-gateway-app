pipeline {
    agent any

    parameters {
        string(name: 'IMAGE_TAG', defaultValue: 'latest', description: 'Docker image tag to deploy')
    }

    environment {
        APP_NAME = 'adapter-dnb'
        PSD2_GATEWAY_IMAGE = "${env.PSD2_GATEWAY_IMAGE ?: 'deploy-psd2-gateway-app:latest'}"
        ADAPTER_DNB_IMAGE = "${env.ARTIFACTORY_DOCKER_REGISTRY}/${APP_NAME}:${params.IMAGE_TAG}"
        MOCK_DNB_BANK_IMAGE = "${env.MOCK_DNB_BANK_IMAGE ?: 'deploy-mock-dnb-bank:latest'}"
        COMPOSE_FILE = 'deploy/docker-compose.release.yml'
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

        stage('Prepare TLS') {
            steps {
                sh './scripts/generate-dev-certs.sh'
            }
        }

        stage('Pull Image') {
            steps {
                withCredentials([usernamePassword(
                        credentialsId: 'artifactory-docker-creds',
                        usernameVariable: 'ARTIFACTORY_USERNAME',
                        passwordVariable: 'ARTIFACTORY_PASSWORD'
                )]) {
                    sh '''
                      echo "${ARTIFACTORY_PASSWORD}" | docker login ${ARTIFACTORY_DOCKER_REGISTRY} \
                        --username "${ARTIFACTORY_USERNAME}" --password-stdin
                      docker pull ${ADAPTER_DNB_IMAGE}
                      docker logout ${ARTIFACTORY_DOCKER_REGISTRY}
                    '''
                }
            }
        }

        stage('Deploy') {
            steps {
                sh '''
                  docker compose -f ${COMPOSE_FILE} up -d mock-dnb-bank outbound-dnb-apigw adapter-dnb
                '''
            }
        }

        stage('Smoke Check') {
            steps {
                sh 'docker compose -f ${COMPOSE_FILE} ps adapter-dnb outbound-dnb-apigw mock-dnb-bank'
            }
        }
    }
}
