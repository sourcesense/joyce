# Joyce Mongodb Sink

[![Build Release](https://github.com/sourcesense/joyce-mongodb-sink/actions/workflows/tags.yaml/badge.svg)](https://github.com/sourcesense/joyce-mongodb-sink/actions/workflows/tags.yaml) [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=sourcesense_joyce-mongodb-sink&metric=alert_status)](https://sonarcloud.io/dashboard?id=sourcesense_joyce-mongodb-sink) [![Coverage](https://sonarcloud.io/api/project_badges/measure?project=sourcesense_joyce-mongodb-sink&metric=coverage)](https://sonarcloud.io/dashboard?id=sourcesense_joyce-mongodb-sink)

It is a simple Sink that stores transformed content published to joyce-content topic inside Mongodb.

## Documentation

Documentation is available [here](https://sourcesense.github.io/joyce/docs/mongodb-sink).

## Install

Joyce Mongodb Sink is distributed as a Docker container:

```bash
docker pull sourcesense/joyce-mongodb-sink:latest
```

Refer to this [docker-compose](https://github.com/sourcesense/joyce-compose/blob/master/docker-compose.yaml) to know how to configure it.

## Developing

### Dependencies

Joyce Mongodb Sink needs [Joyce Parent](https://github.com/sourcesense/joyce-parent) as dependency.
You can install it **from source**, or get it from **Maven repository**.

To get it from Maven repository you have to maven to retrieve package from Github packages.

Add to your `~/.m2/settings.xml` following config:

```xml
<repositories>
  ...
  <repository>
    <id>github</id>
    <url>https://maven.pkg.github.com/sourcesense/*</url>
    <snapshots>
      <enabled>true</enabled>
    </snapshots>
  </repository>
<repositories>
...
  <servers>
    ...
    <server>
      <id>github</id>
      <username>YOUR_GITHUB_USERNAME</username>
      <password>PERSONAL_ACCESS_TOKEN_WITH_READ_PACKAGES_WITH_READ_PACKAGES_PERMISSION</password>
    </server>
  </servers>
```

Refer to [official docs](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry).

### Build

> Building Joyce Mongodb Sink requires JDK 11

```bash
# build java
mvn clean package

# build docker image
docker build .
# or
docker-compose build
```
