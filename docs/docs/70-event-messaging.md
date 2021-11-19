# Event Messaging

Joyce platform is event-based and is based on passing messages around, you'll have available to hook inside the platform by consuming this messages.

Different topics are used in the platform.

## joyce_import topic

This is the topic where Kafka Connect or other systems publishes the content to be imported.

The content of the topic is raw json content that has to be processed

## joyce_schema topic

This topic is used as persistent storage for Schemas, you can consume this topic to watch for Schema changes.

The content of this topic are [Schemas](schema)

## joyce_content topic

This topic is where Import Gateway writes processed content and Sinks read from to store it.

The content of this topic is json shaped validated by the schema used to transform.

You can consume from this topic to watch for content changes.

## joyce_notification topic

This topic is used by every object of the architecture to notify events of succes or failure in the different steps that a content imported go through.

If something goes wrong a failure event is published here.
You can consume this topic to know all the events that occurs to a content imported in the system.

## joyce_command topic

This topic is used by the API to send `json-rpc` messages, you can conusme this topic to create custom handler for  `json-rpc` messages you send.