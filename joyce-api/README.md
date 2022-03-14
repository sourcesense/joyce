# Joyce Api

[![Publish Snapshot][joyce logo]](https://sourcesense.github.io/joyce/docs/overview)

This is the api server from which you can consume the final output of selected Joyce transformations with REST or GraphQL protocols in read-only.

It may publish also a jrpc endpoint to push back writes with a CQRS approach. These messages might be consumed with custom components.

Togheter with the REST/GraphQL/JRPC endpoints this server publishes also a swagger-ui and a graphiql pages available at the following urls.

```bash
http://${host}:${JOYCE_API_PORT}/swagger-ui/index.html#/default
http://${host}:${JOYCE_API_PORT}/graphiql/
```

## Documentation

The full documentation is available [here](https://sourcesense.github.io/joyce/docs/rest).

## Install

Joyce Api is distributed as a Docker container:

```bash
docker pull sourcesense/joyce-api:latest
```

### Configure

It is configured by environment variables.

Refer to this [docker-compose] to view an example.

| env var                          | description                                                                            |
| -------------------------------- | -------------------------------------------------------------------------------------- |
| JOYCE_API_CONFIG_SOURCE          | path to configuration file. Default `./api-config.json`                                |
| JOYCE_API_WORKDIR                | path to work folder. Default `./workdir`                                               |
| JOYCE_API_PORT                   | Port where service is. Default: `6650`                                                 |
| JOYCE_API_MONGO_URI              | [Mongo connection uri]                                                                 |
| JOYCE_API_KAFKA_BOOTSTRAPADDRESS | A string of kafka broker/host combination delimited by comma. Default `kafka:9092`     |
| JOYCE_API_KAFKA_COMMAND_TOPIC    | Kafka commando topic. Default: `joyce_command`                                         |
| JOYCE_API_SCHEMA_GRPC_ENDPOINT   | host:port to import-gateway, default `import-gateway:6666`                             |
| JOYCE_API_JAEGER_HOST            | jaeger server host for opentelemetry span report, required to enable tracing           |
| JOYCE_API_JAEGER_PORT            | jaeger server port for opentelemetry span report, default `6832`                       |
| JOYCE_API_SERVICE_NAME           | Logical name of the service, default `unknown_service:nodejs` see [opentelemetry spec] |
| JOYCE_API_SERVICE_NAMESPACE      | A namespace for _JOYCE_API_SERVICE_NAME_. Default `Joyce.API`                          |
| JOYCE_API_ENABLE_TRACING         | Enables tracing with jaeger. Tracing needs _JOYCE_API_JAEGER_HOST_ as well.            |

## Developing

```bash
npm ci
npm run dev
```

[joyce logo]: https://github.com/sourcesense/joyce/raw/main/docs/static/img/logo-horizontal-dark-blue.png
[docker-compose]: https://github.com/sourcesense/joyce-compose/blob/master/docker-compose.yaml
[Mongo connection uri]: https://docs.mongodb.com/drivers/node/current/fundamentals/connection/#connection-uri
[opentelemetry spec]: https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/resource/semantic_conventions/README.md#service
