# Extending

Joyce architecture offers different point of extension

## Connectors
If Kafka connect isn't enough for you, and you don't want to develop a Kafka Connect Plugin, both REST API and Consumer API of the [Import Gateway](import-gateway) can be used to develop a custom connector that produce content to be imported as you like.

## Sinks

Joyce ships with  `joyce-mongodb-sink` but you can develop your own sinks to store content transformed by joyce as you need.

## Schema Engine

Most powerful extension mechanism is enriching the semantics of the Schema DSL, you can register custom handlers for the schema engine inside the Import Gateway.

What you have to do is 
- create a project based on [this skeleton](https://github.com/sourcesense/joyce-demo-custom-handler) to develop your handler
- package it as a jar
- make teh jar available to the plugin folder of import gateway.

You can control the plugin directory for the gateway with this environment variable `JOYCE_SCHEMA_ENGINE_PLUGIN_PATH`, so you can easily mount a docker volume with your plugins.

There is a [Tutorial](plugin-tutorial) that describes step by step how to do it.
