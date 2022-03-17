
# Overview

> 
> Joyce is a highly scalable event-driven Cloud Native [Data Hub](https://en.wikipedia.org/wiki/Data_hub).
> 

Ok! Wait, what? Joyce allows you to ingest data from (almost) any source and expose the ingested data as standard APIs (REST, event notification) **automatically**. In order to specify to Joyce which data we want to pick from the incoming data stream and how APIs will look like you need to describe the expected behaviour with a DSL based on `json-schema`.

From a high level perspective Joyce performs 4 tasks:

- acquire content produced from different sources.
- **transform** the raw content with a DSL (a `schema`)
- store it somewhere (to a `sink`)
- serve the result of this process with an **automatic REST API**.

Joyce runs in a Kubernetes cluster (Helm chart in progress) and the event-driven architecture relies on Apache Kafka (more on this in the [Architecture](architecture) section), MongoDB is the database we use to store the ingested data you can query through generated APIs.

There are 4 main components you can configure to scale out independently and meet your unique needs:

- kafka-connect. The framework we use to ingest data
- import-gateway. It's main purpose is schema validation and data enrichment
- mongodb-sink. The service to persist data 
- mongodb-api. The auto-generated API layer

Our data ingestion capabilities are built on top of the Kafka Connect (https://www.youtube.com/watch?v=J6adhl3wEj4) framework to provide a simple and widely adopted solution to connect data sources to Apache Kafka. We also provide an admin REST API to upload CSV files (if your source of data doesn't fit with a stream approach).

In order to let the magic happen you need to provide to Joyce 3 things:

1. configure the Kafka Connect connector to bind Joyce with the source
2. describe the data you want to pick from the source (with some basic string manipulation features)
3. describe how you want to present the ingested data

Please refer to the [Schema](schema) section to exploit schema capabilities and the degrees of freedom available for (2) and (3) 

## Who is it for?

Joyce can be a good fit for you if:

- Your application/service/whatever relies on data you don't own or you cannot query directly (legacy systems, databases, custom apps, external services, ...)
- Your source data has a poor formatting (but structured) or you need to get data from slightly different sources that provide data for the same domain (more on this in the [Tutorial](/tutorial-accomodations))
- You want to build a common layer of REST APIs to access the application data (and you don't want to bother you with a lot of boilerplate code)
- You need a convenient way to deliver different views of the same data for different purposes/devices/use cases
- You are just curious and you want to give it a try

Some things we want to point out:

- Joyce's main goal is handle (and provide a unified view of) operational data, not a platform for analytical data pipelines
- Our intention is to share with the community a platform that reduces complexity when you have to deal with complex problems, there's no reason to use a lot of technology and tackle asynchronicity, eventual consistency, ... if your use case doesn't need it
- Data definition, validation and enrichment are expressed through `schemas` as how to shape the content and bind the functions that will be applied to the actual data on the stream, this implies that every action on **data in transit is synchronous** with the input data processing; more formally is a function f(X) where X is the incoming data on the stream. To go deeper please refer to [Schema](Schema)

You can extend the schema DSL in order to add custom data validation/enrichment features (more in [Extending](Extending)); we think that this is a powerful extension point but comes with some warnings. As stated above every custom function will be synchronous with the ingestion, this means:
- Slow functions may affect dramatically data ingestion performance
- Enrichment errors must be treated as first class citizens (i.e. returned as content in the data node and handled properly at the application level)
- If your function has side effects (like a REST API call) then has to properly manage them and return a meaningful result gracefully

## Open Standards

Joyce is built upon open source and estabilished standards:

- Java and Spring Framework
- json-schema
- json-path
- json-rpc
- mustache
- graphql
- kafka
- mongodb

## Features

- Transformation of data, with a DSL based on `json-schema`.
- Event-Driven Architecture.
- Mongodb data persistance.
- Automatic REST endpoints for the modeled data.
- Extensibility through:
  - Augmentation of the Schema DSL
  - Custom source-connector
  - Custom sink
