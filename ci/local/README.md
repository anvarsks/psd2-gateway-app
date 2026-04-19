# Local Jenkins + Artifactory

This folder provides a local runtime setup for:

- Jenkins
- Docker Registry

## Start

Jenkins + local registry:

```bash
cd /Users/anvarshameemks/psd2-gateway-app
docker compose -f ci/local/docker-compose.yml up --build -d
```

## URLs

- Jenkins: `http://localhost:18080`
- Local Docker registry: `http://localhost:15001/v2/`

## Jenkins First Login

Get the initial admin password:

```bash
docker exec local-jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

Then install the suggested plugins or continue with the already bundled pipeline-related plugins.

## Registry Notes

The local registry is a plain Docker Registry for development speed.

The Jenkins CI jobs in this repo expect:

- a Docker registry hostname in `ARTIFACTORY_DOCKER_REGISTRY`
- Jenkins credentials with ID `artifactory-docker-creds`

For a quick local pattern, use:

`localhost:15001`

If you do not enable auth on the local registry, you can simplify the Jenkinsfiles later by removing the login step or by using a dummy Jenkins credential just for local testing.

## Stop

```bash
cd /Users/anvarshameemks/psd2-gateway-app
docker compose -f ci/local/docker-compose.yml down
```
