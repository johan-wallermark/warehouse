# Toy warehouse application

Based on Javalin with a helm chart.

## Requirements:

    * Java 11
    * Docker
    * Helm
    * Postgres

## Dev Setup

### Postgres

Install postgres with helm.

1. helm repo add bitnami https://charts.bitnami.com/bitnami
1. helm install warehouse-db bitnami/postgresql -f db/db-values.yaml

## Build

The project utilizes gradle to build the source code and produce a Docker
image. The integration tests needs postgresql in order to pass.

To build the source code and create a docker image run:

```
./gradlew build jibDockerBuild
```

Note that jibDockerBuild builds towards a Docker daemon rather then directly
towards a repository.

## Installing the helm chart

To install the helm chart:

1. helm repo add bitnami https://charts.bitnami.com/bitnami
    (if you didn't do this step already)
1. Follow the Build instructions above as we need the docker image built in
    order to fully install the helm chart.
1. helm dep update helm
1. helm install my-warehouse helm

