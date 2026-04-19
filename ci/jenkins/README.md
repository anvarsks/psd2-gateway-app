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

## Why JFrog Artifactory

Artifactory can act as:

- a Docker registry for your application images
- a Maven repository for your Java artifacts if you decide to publish jars as well

For this repo, the Jenkins CI jobs are wired to push Docker images first. That is the cleanest starting point for CD because the deploy jobs can pull immutable image tags.

## Recommended Flow

1. CI job runs tests.
2. CI job builds the jar.
3. CI job builds the Docker image.
4. CI job pushes the Docker image to Artifactory.
5. CD job takes an image tag as input.
6. CD job pulls that image from Artifactory.
7. CD job deploys it with `deploy/docker-compose.release.yml`.

## Notes

- The current CD jobs are designed around Docker Compose deployment.
- For a real non-local environment, point the CD jobs at the target Docker host or move the same image flow to Kubernetes, TAS, or another runtime later.
- The mock DNB bank remains local-demo infrastructure for now.
