# Joyce Import gateway

[![Build Release](https://github.com/sourcesense/joyce-import-gateway/actions/workflows/tags.yaml/badge.svg)](https://github.com/sourcesense/joyce-import-gateway/actions/workflows/tags.yaml)

The Import Gateway is the single point where you ingest your data into the system.
It manages import schemas and applies them to the content you send to it.

## Documentation

The full documentation is available [here](https://sourcesense.github.io/joyce/docs/import-gateway).

## Install

Joyce Import Gateway is distributed as a Docker container:

```bash
docker pull sourcesense/joyce-import-gateway:latest
```

Refer to this [docker-compose](https://github.com/sourcesense/joyce-compose/blob/master/docker-compose.yaml) to know how to configure it.

## Developing

### Dependencies

Joyce Import Gateway needs [joyce Parent](https://github.com/sourcesense/joyce-parent) as dependency.
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

> Building joyce Gateway requires JDK 11

```bash
# build java
mvn clean package

# build docker image
docker build .
# or
docker-compose build
```
