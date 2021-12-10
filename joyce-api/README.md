# Joyce Rest

[![Publish Snapshot](https://github.com/sourcesense/joyce-docs/raw/main/static/img/logo-horizontal-dark-blue.png)](https://sourcesense.github.io/joyce-docs/)

Is the REST api from which you can consume the final output of Joyce transofrmations.

```
http://${INTERNAL_URL}:${port}/docs/index.html#/default
```

## Documentation

The full documentation is available [here](https://sourcesense.github.io/joyce/docs/mongodb-rest).

## Install

Joyce Mongodb Rest is distributed as a Docker container:

```bash
docker pull sourcesense/joyce-api:latest
```

### Configure

It is configured by environment variables.

Refer to this [docker-compose](https://github.com/sourcesense/joyce-compose/blob/master/docker-compose.yaml) to view an example.

| var                              | description                                                 |
| -------------------------------- | ----------------------------------------------------------- |
| SCHEMAS_SOURCE                   | Folder variable where search for schema source's paths      |
| PRODUCTION_URL                   | Base url of the API (for show on Swagger)                   |
| INTERNAL_URL                     | Base url of the API on internal/local network               |
| HEALTH_PATH                      | Endpoint to check healty of serverice. Default: _"/health"_ |
| PORT                             | Port where service is. Default: _6650_                      |
| MONGO_URI                        | Mongo URI                                                   |
| JOYCE_GRAPHQL                    | `true` / `false` enable or disable graphql                  |
| JOYCE_API_KAFKA_BOOTSTRAPADDRESS | Kafka Host                                                  |
| JOYCE_API_KAFKA_COMMAND_TOPIC    | Kafka commando topic. Default: _commands_                   |

## Developing

```bash
npm install
npm run dev
```
