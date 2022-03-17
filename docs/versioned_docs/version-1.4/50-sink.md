# Mongodb Sink


Once content is piped through `joyce_import` topic by Kafka Connect and then transformed and published to `joyce_content` topic by the Import Gateway, Sinks save the transformed content to a Storage, ready to be served.

Joyce Currently support one kind of Sink, Mongodb Sink and gives you  a default REST API above it.

## Overview

Mongodb Sink is the object that reads from `joyce_content` topic and manages content inside mongodb upserting and deleting content accordingly.

It also reads the schema `joyce_schema` topic to perform two task:

## Schema Validation

If configured, use the Schema as mongodb [Schema Validation](https://docs.mongodb.com/manual/core/schema-validation/).


:::caution
The user used to connect to mongodb must have the right roles to issue `collMod` command.
You can do it this way:
```
db.grantRolesToUser( "user", [ 
    { role: "dbAdmin", db: "joyce" }
    ])
```
:::

## Indexes

In the schema metadata we can define a list of indexes, represented with a Map:

```yaml
indexes:
  - section: 1
  - section: 1
    published_date: 0
```
When the schema changes new indexes are created, **indexes removed from the Schema ARE NOT removed from the collection** You have to delete manually from mongodb.

## Configuration

We can change and tune its behaviour "per schema" with settings in the metadata node of the [Schema](schema):  

- `$metadata.collection` is the collaction name where content is stored.
- `$metadata.uid` is the key of the content to be used as `_id` in mongodb.
- `$metadata.store` by putting this settings as false the Sink will NOT store the content
- `$metadata.indexes` it's an array of Maps defining the indexes (single or composite) to create in the colelction
