
# Joyce API

`Joyce-API` is the main interface to consume the final output of Joyce transformations.
It exposes readonly **REST** and **GraphQL** interfaces and a **JSON-RPC** CQRS interface to propagate other operations to the underlying systems.

## Configuration

The api needs a json config file that defines which schemas to serve, this enables you to spawn different instances of the api if you want to serve different content to different endpoints.

The configuration file is really simple:

```json
{
  "jsonrpc": false, // default
  "graphQL": true, // default
  "rest": true, // default
  "resources": [
    {
      "path": "[DESIRED_PATH]",
      "source": "[JOYCE_URI_OF_SCHEMA]"
    },
  ]
}
```

```json title="sample api-config.json"
{
  "resources": [
    {
      "path": "users",
      "source": "joyce://schema/import/user"
    },
    {
      "path": "projects",
      "source": "joyce://schema/import/project"
    }
  ]
}
```

The file is read at startup so you have to already have the schema stored within the import gateway.

`Joyce-API` reads the schema and parses its [$metadata](schema#metadata) and properties, knowing the shape of the content, the collection and unique id of content.

There are a number of env vars needed to setup the inner configuration of `Joyce-API`, the most relevant are:

| env var                          | description                                                                            |
| -------------------------------- | -------------------------------------------------------------------------------------- |
| JOYCE_API_MONGO_URI              | [Mongo connection uri]                                                                 |
| JOYCE_API_KAFKA_BOOTSTRAPADDRESS | A string of kafka broker/host combination delimited by comma. Default `kafka:9092`     |
| JOYCE_API_SCHEMA_GRPC_ENDPOINT   | host:port to import-gateway, default `import-gateway:6666`                             |
| JOYCE_API_JAEGER_HOST            | jaeger server host for opentelemetry span report, required to enable tracing           |
| JOYCE_API_JAEGER_PORT            | jaeger server port for opentelemetry span report, default `6832`                       |
| JOYCE_API_SERVICE_NAME           | Logical name of the service, default `unknown_service:nodejs` see [opentelemetry spec] |
| JOYCE_API_SERVICE_NAMESPACE      | A namespace for _JOYCE_API_SERVICE_NAME_. Default `Joyce.API`                          |
| JOYCE_API_ENABLE_TRACING         | Enables tracing with jaeger. Tracing needs _JOYCE_API_JAEGER_HOST_ as well.            |

:::caution

prior to version 1.5.6 of Joyce-API config and env variables were slightly different

:::

## REST

If rest is not explicitly disabled in config, `Joyce-API` will expose a Swagger documentation of the API at `/swagger-ui`.

For every schema configured two endpoints are exposed:

- one to retrive a single content by uid `/[PATH]/[UID]`
- one to list content  `/[PATH]` that can be _paged_, _ordered_ and _filtered_

## GraphQL

If graphQL is not explicitly disabled in config, `Joyce-API` will expose also a [GraphQL](https://graphql.org/) interface at `/graphql` and a playground at `/graphiql`.

The schema is transposed to a graphql schema automatically by using [GraphQL Mesh](https://www.graphql-mesh.com/)

## JSON-RPC

REST and GraphQL APIs are **read-only**, only GET endpoints are exposed for the schemas, but the platform enables also a way to push back writes, following a CQRS approach.

There is a `/jrpc` endpoint where you can send [json-rpc](https://www.jsonrpc.org/specification) messages that will be published on a `joyce_command` topic, that you can consume with custom code to make changes to the backend systems that owns the row data.

[Mongo connection uri]: https://docs.mongodb.com/drivers/node/current/fundamentals/connection/#connection-uri
[opentelemetry spec]: https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/resource/semantic_conventions/README.md#service
