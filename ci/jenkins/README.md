# Jenkins CI/CD

This repo now includes four Jenkins pipeline definitions:

- `ci/jenkins/psd2-gateway-ci.Jenkinsfile`
- `ci/jenkins/psd2-gateway-cd.Jenkinsfile`
- `ci/jenkins/adapter-dnb-ci.Jenkinsfile`
- `ci/jenkins/adapter-dnb-cd.Jenkinsfile`

## Intended Job Split

- `psd2-gateway-app` CI: test, package, build Docker image, push to Artifactory
- `psd2-gateway-app` CD: pull Docker image from Artifactory and deploy
- `adapter-dnb` CI: test, package, build Docker image, push to Artifactory
- `adapter-dnb` CD: pull Docker image from Artifactory and deploy

## Jenkins Prerequisites

- Jenkins with Pipeline support
- Docker available on the Jenkins agent
- JDK 17 tool configured in Jenkins as `jdk17`
- Jenkins credential `artifactory-docker-creds`
- Environment variable `ARTIFACTORY_DOCKER_REGISTRY`

Example registry value:

`mycompany.jfrog.io/docker-dev-local`

## Artifact Store

The pipelines are written so the registry endpoint is configurable.

For local development, use the local Docker registry:

`localhost:15001`

For a more enterprise setup later, replace that with JFrog Artifactory or another managed registry.

## Recommended Flow

1. CI job runs tests.
2. CI job builds the jar.
3. CI job builds the Docker image.
4. CI job pushes the Docker image to the configured registry.
5. CD job takes an image tag as input.
6. CD job pulls that image from the configured registry.
7. CD job deploys it with `deploy/docker-compose.release.yml`.

## Notes

- The current CD jobs are designed around Docker Compose deployment.
- For a real non-local environment, point the same pipelines at Artifactory, ECR, GCR, ACR, or another registry.
- The mock DNB bank remains local-demo infrastructure for now.
