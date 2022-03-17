
# Architecture

Joyce has an event-driven architecture based on Apache Kafka, every service that interacts with the stream `transforms` the data and/or stores it in a `sink`. From a phisical standpoint is a microservices architecture designed to execute in a Kubernetes cluster, there's no direct (e.g. synchronous) communication between the microservices, everything happens triggered by an event. 

We designed Joyce to be reliable, scalable and extensible.

Most of the heavy lift on reliability and extensibility is made by the great software we've selected, from our side we designed all the services to be stateless and asynchronous, keeping high cohesion and the lowest possible coupling.

You can [extend](Extending) every Joyce's feature: ingestion (`source`), store (`sink`), API and engine (`transform`). 

![architecture](/img/architecture/Architecture-v4.png)

> Orange arrow blocks are Kafka Topics.   
> Green esagon are joyce components.   
> Dotted lines are reads.  
> Continuos lines are writes.   

### Source (Kafka Connect)

Joyce leverages the power of [Kafka Connect](https://docs.confluent.io/platform/current/connect/index.html) to move data out of a variety of system ( with its connectors ) into Joyce platform.   

### Schema Engine (import-gateway)

The main core of Joyce has two purpose:
- It manages transformation [Schemas](schema).
- Consumes content from the `joyce_import` topic and from a rest REST API, applies to it a schema and produce a *transformed* content to `joyce_content`.

### Sink (mongodb-sink)

Consumes from `joyce_content` topic and stores what it reads to `mongodb`

### API (rest)

Exspose its configured collection with a read only API.  
Expose a json-rpc interface to send comamnds to a `joyce_command` from which external system can read.


