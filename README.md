# Nile API Rest

[![Publish Snapshot](https://github.com/sourcesense/nile-mongodb-rest/actions/workflows/master.yaml/badge.svg)](https://github.com/sourcesense/nile-mongodb-rest/actions/workflows/master.yaml)

Swagger endpoint

```
http://${INTERNAL_URL}:${port}/docs/index.html#/default
```

## Variables available

Folder variable where search for schema source's paths

```
SCHEMAS_SOURCE
```

Base url of the API (for show on Swagger)

```
PRODUCTION_URL
```

Base url of the API on internal/local network

```
INTERNAL_URL
```

Endpoint to check healty of serverice. Default: _"/health"_

```
HEALTH_PATH
```

Port where service is. Default: _6650_

```
PORT
```

Mongo URI

```
MONGO_URI
```

Kafka Host

```
NILE_API_KAFKA_BOOTSTRAPADDRESS
```

Jakfka commando topic. Default: _commands_

```
NILE_API_KAFKA_COMMAND_TOPIC
```
