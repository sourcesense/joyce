# Joyce Kafka Connect

[![Docker Image](https://github.com/sourcesense/joyce-kafka-connect/actions/workflows/master.yaml/badge.svg)](https://github.com/sourcesense/joyce-kafka-connect/actions/workflows/master.yaml)

Joyce Kafka Connect it's [Kafka Connect](https://docs.confluent.io/platform/current/connect/index.html) with some connectors already installed:

- confluentinc/kafka-connect-jdbc
- apache/camel-file-kafka-connector
- confluentinc/kafka-connect-jira
- jcustenborder/kafka-connect-spooldir
- ...


## Install

Joyce Kafka Connect is distributed as a Docker container:

```bash
docker pull sourcesense/joyce-kafka-connect:latest
```

## Configuration

Refer to Kafka Connect documentation to view configurations you can set via environment.
Check this [docker-compose](https://github.com/sourcesense/joyce-compose/blob/master/docker-compose.yaml) to view an example.
