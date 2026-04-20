pipeline {
    agent any

    parameters {
        string(name: 'IMAGE_TAG', defaultValue: 'latest', description: 'Docker image tag to deploy')
        string(name: 'DEPLOY_PROFILE', defaultValue: '', description: 'Optional compose profile, for example observability')
    }

    environment {
        APP_NAME = 'psd2-gateway-app'
        REPO_ROOT = '/Users/anvarshameemks/psd2-gateway-app'
        PSD2_GATEWAY_IMAGE = "${env.ARTIFACTORY_DOCKER_REGISTRY}/${APP_NAME}:${params.IMAGE_TAG}"
        ADAPTER_DNB_IMAGE = "${env.ADAPTER_DNB_IMAGE ?: 'deploy-adapter-dnb:latest'}"
        MOCK_DNB_BANK_IMAGE = "${env.MOCK_DNB_BANK_IMAGE ?: 'deploy-mock-dnb-bank:latest'}"
        COMPOSE_FILE = "${REPO_ROOT}/deploy/docker-compose.release.yml"
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
                dir("${env.REPO_ROOT}") {
                    sh './scripts/generate-dev-certs.sh'
                }
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
                      docker pull ${PSD2_GATEWAY_IMAGE}
                      docker logout ${ARTIFACTORY_DOCKER_REGISTRY}
                    '''
                }
            }
        }

        stage('Deploy') {
            steps {
                dir("${env.REPO_ROOT}") {
                    sh '''
                      docker compose -f ${COMPOSE_FILE} up -d psd2-gateway-app kong nginx-edge
                    '''
                }
            }
        }

        stage('Smoke Check') {
            steps {
                dir("${env.REPO_ROOT}") {
                    sh '''
                      docker inspect --format='{{.State.Health.Status}}' psd2-gateway-app | grep -qx healthy
                      docker exec kong-psd2-gateway kong health
                    '''
                }
            }
        }
    }
}
