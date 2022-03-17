# Connectors

Joyce use [Kafka Connect](https://docs.confluent.io/platform/current/connect/index.html) as the preferred way to import content inside the platform.

There are a lot of battle tested Source Connectors that can be easily used to import data from a variety of sources, see [Confluent Hub](https://www.confluent.io/hub/).

Joyce ships with a custom kafka-connect container with some plugins already installed, ready to be configured:

- confluentinc/kafka-connect-jdbc
- confluentinc/kafka-connect-jira
- confluentinc/kafka-connect-elasticsearch
- jcustenborder/kafka-connect-spooldir
- castorm/kafka-connect-http
- kaliy/kafka-connect-rss

To enable Connectors work well with Joyce and let [Import Gateway](import-gateway) know wich Schema to apply to transform the content you have to build a message key as described [here](import-gateway#kafka-consumer-api).

:::info
The transformation is configured automatically by `import-gateway` if you configure the [connector inside the schema](schema#connectors).

This is Joyce prefereed way to iunteract with kafka connect, not directly but mutuated by import-gateway.
:::

It's easy using Kafka Connect built-in InsertJoyceMessageKey SMT (Single message Transform):

```json

    "transforms": "joyceKey",
    "transforms.joyceKey.type": "com.sourcesense.joyce.connect.custom.InsertJoyceMessageKey",
    "transforms.joyceKey.uid": "user_id",
    "transforms.joyceKey.schema": "joyce://schema/import/user",
    "transforms.joyceKey.source": "user-table-source"
```

Given a content like this:
```json
{
    "user_id": 1337,
    "first_name": "Jon",
    "last_name": "Snow",
    "email": "jon@winterfell.cold",
    "state": "Westeros"
}
```

Will produce this message key:
```json
{
  "uid": "1337",
  "schema": "joyce://schema/import/user",
  "source": "user-table-source"
}
```
  
Read SMT [documentation](https://docs.confluent.io/platform/current/connect/transforms/overview.html) for an advanced usage.