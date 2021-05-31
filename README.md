# Joyce Rest

[![Publish Snapshot](https://github.com/sourcesense/joyce-rest/actions/workflows/master.yaml/badge.svg)](https://github.com/sourcesense/joyce-rest/actions/workflows/master.yaml)

Is the REST api from which you can consume the final output of Joyce transofrmations.

```
http://${INTERNAL_URL}:${port}/docs/index.html#/default
```
## Documentation

The full documentation is available [here](https://sourcesense.github.io/joyce/docs/mongodb-rest).

## Install

Joyce Mongodb Rest is distributed as a Docker container:

```bash
docker pull sourcesense/joyce-rest:latest
```


### Configure

It is configured by environment variables.

Refer to this [docker-compose](https://github.com/sourcesense/joyce-compose/blob/master/docker-compose.yaml) to view an example.

| var                             | description                                                 |
| ------------------------------- | ----------------------------------------------------------- |
| SCHEMAS_SOURCE                  | Folder variable where search for schema source's paths      |
| PRODUCTION_URL                  | Base url of the API (for show on Swagger)                   |
| INTERNAL_URL                    | Base url of the API on internal/local network               |
| HEALTH_PATH                     | Endpoint to check healty of serverice. Default: _"/health"_ |
| PORT                            | Port where service is. Default: _6650_                      |
| MONGO_URI                       | Mongo URI                                                   |
| JOYCE_API_KAFKA_BOOTSTRAPADDRESS | Kafka Host                                                  |
| JOYCE_API_KAFKA_COMMAND_TOPIC    | Kafka commando topic. Default: _commands_                   |

## Developing

```bash
npm install
npm run dev
```
