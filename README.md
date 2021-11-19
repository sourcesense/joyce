# Joyce Compose

| Component          | docker                                         | latest version|
| --- | --- | --- |
| Import Gateway     | sourcesense/joyce-import-gateway | ![Docker Image Version (latest semver)](https://img.shields.io/docker/v/sourcesense/joyce-import-gateway) |
| Joyce Kafka Connect | sourcesense/joyce-kafka-connect | ![Docker Image Version (latest semver)](https://img.shields.io/docker/v/sourcesense/joyce-kafka-connect) |
| Mongodb Sink |  sourcesense/joyce-mongodb-sink | ![Docker Image Version (latest semver)](https://img.shields.io/docker/v/sourcesense/joyce-mongodb-sink) |
| Rest       | sourcesense/joyce-rest | ![Docker Image Version (latest semver)](https://img.shields.io/docker/v/sourcesense/joyce-rest) |
## Introduction

> 
> Joyce is a highly scalable event-driven Cloud Native [Data Hub](https://en.wikipedia.org/wiki/Data_hub).
> 

Ok! Wait, what? Joyce allows you to ingest data from (almost) any source and expose the ingested data as standard APIs (REST, event notification) **automatically**. In order to specify to Joyce which data we want to pick from the incoming data stream and how APIs will look like you need to describe the expected behaviour with a DSL based on `json-schema`.

From a high level perspective Joyce performs 4 tasks:

- acquire content produced from different sources.
- **transform** the raw content with a DSL (a `schema`)
- store it somewhere (to a `sink`)
- serve the result of this process with an **automatic REST API**.

## Documentation

Documentation is available [here](https://sourcesense.github.io/joyce-docs)

## Getting Started

```bash
cd joyce-compose
docker-compose up -d
```

This will startup:
  - a single node `kafka` instance persisted under `data` directory
  - a single node `zookeeper` instance persisted under `data` directory
  - a single node `mongodb` instance persisted under `data` directory
  - [AKHQ](https://akhq.io/) to monitor kafka topics exposed at [localhost:6680](http://localhost:6680)
  - **joyce-import-gateway** exposing it's API at [localhost:6651](http://localhost:6651/docs)
  - **joyce-mongodb-sink** to store processed content to mongodb
  - **joyce-rest** exposed at [localhost:6650](http://localhost:6650/docs) to consume processed content.

### Save a schema

First of all we have to store a schema that tells the system how to project the content we import inside Joyce.

A schema is an enhanced `json-schema` with keywords that tells how to transform/project a content.  

For a complete documentation on schema go [here](https://sourcesense.github.io/joyce-docs/docs/schema)

You can write a schema in json or yaml.

Let's try to save one.

```bash
cat > import-user.yaml  <<- "EOF"
$schema: https://joyce.sourcesense.com/v1/schema
$metadata:
  subtype: import
  namespace: default
  name: user
  description: A test schema
  development: true
  uid: code
  collection: users
type: object
properties:
  code:
    type: integer
    $path: $.user_id
  name:
    type: string
    $path: $.first_name
  surname:
    type: string
    $path: $.last_name
  full_name:
    type: string
    $script: 
      language: python
      code: "'_'.join([source['first_name'].upper(), source['last_name'].upper()])"
  email:
    type: string
  email_checks:
    type: object
    $rest:
      url: "https://api.eva.pingutil.com/email?email={{email}}"
      method: GET
      headers:
        Content-Type: application/json
      vars:
        email: "$.email"
      extract: "$.data"
    properties:
      valid:
        type: boolean
        $path: $.valid_syntax
      disposable:
        type: boolean
      spam:
        type: boolean
  kind:
    type: string
    $fixed: "SimpleUser"
EOF
```

Now we have to save the schema to `import-gateway` component:

```bash
curl -X POST -H "Content-Type: application/x-yaml" --data-binary @import-user.yaml http://localhost:6651/api/schema
```

Now your schema is ready to be used by the api, you can check it by going to [http://localhost:6651/api/schema/import/default/user](http://localhost:6651/api/schema/import/default/user).

### Configure API

If you go to [http://localhost:6650/docs](http://localhost:6650/docs) you'll see a swagger interface with no resources, that's why resource derives from schema and must be configured to be exposed.

create a file `schemas.json` with this content:
```bash
cat > schemas.json  <<- "EOF"
{   
    "schemas": {
        "test-users": {
            "source": "http://import-gateway:6651/api/schema/import/default/user"
        }
    }
}
EOF
```

Edit the docker compose to expose the file as a volume:

```yaml
  rest:
     image: sourcesense/joyce-rest:latest
     ports:
       - "6650:6650"
     environment:
       - MONGO_URI=mongodb://user:password@mongodb:27017/joyce
+      - SCHEMAS_SOURCE=/opt/schemas.json
+    volumes:
+      - ./schemas.json:/opt/schemas.json
     links:
       - mongodb
       - import-gateway
```

Now restart the api to load the schema:

```bash
docker-compose stop rest
docker-compose up -d rest
```

Check again swagger [http://localhost:6650/docs](http://localhost:6650/docs) and you'll see your resource.

### Import documents

Now you are ready to store content to the import-gateway:
```bash
curl -0 -v "http://localhost:6651/api/import" \
-H 'Content-Type: application/json; charset=utf-8' \
-H "accept: application/json; charset=utf-8" \
-H "X-Joyce-Schema-Id: joyce://schema/import/default.user" "http://localhost:6651/api/import" \
--data-binary @- << EOF
{
    "user_id": 1337,
    "first_name": "Jon",
    "last_name": "Snow",
    "email": "jon@winterfell.cold",
    "state": "Westeros"
}
EOF
```

Your content should be transformed soon and can be retrieved using the api
```bash
curl http://localhost:6650/test-users
```

If anything goes wrong, notification of errors and success during processing are published on the `joyce_notification` topic on kafka, you can inspect easily by using akhq on [localhost:6680](http://localhost:6680).
