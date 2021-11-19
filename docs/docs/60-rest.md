
# Joyce API

`joyce-rest` is the main interface to consume the final output of Joyce transofrmations.  
It expose a **REST** and a **GraphQL** interface to *read* and a **JSON-RPC** interface to propagate other operations to the source backend systems.

## Configuration

The api needs a json config file that defines which schemas to serve, this enables you to spawn different instances of the api if you want to serve different content to different endpoints.

The configuration file is really simple:

```json
{   
    "schemas": {
        "[PATH]": {
            "source": "[JOYCE_URI_OF_SCHEMA]"
        }
    }
}
```

ie.
```json
{   
    "schemas": {
        "users": {
            "source": "http://import-gateway:6651/api/schema/user"
        },
        "projects": {
            "source": "http://import-gateway:6651/api/schema/project"
        }
    }
}
```

The file is read at startup so you have to already have the schema stored within the import gateway.

`joyce-rest` read the schema and parses its [$metadata](schema#$metadata), knowing the shape of the content, the collection and unique id of content.

## REST 

With this information it will expose a Swagger documentation of the API at `/docs`.

For every schema configured two enpoint are exposed:

- one to retrive a single content by uid `/[PATH]/[UID]`
- one to list content  `/[PATH]` that can be *paged*, *ordered* and *filtered*

## JSON-RPC

API are **read-only**, only GET endpoint are exposed for the collections, but the platform enables also a way to push back writes, but following a CQRS approach does it in a different way.

There is a `/jrpc` endpoint where you can send [json-rpc](https://www.jsonrpc.org/specification) messages that will be published on a `joyce_command` topic, that you can consume with custom code to make changes to the backend systems that owns the row data.

## GraphQL

This service will expose also a [GraphQL](https://graphql.org/) interface at `/query` and a playground at `/graphql`. 
The schema is transposed to a graphql schema automatically by using [GraphQL Mesh](https://www.graphql-mesh.com/)