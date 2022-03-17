---
sidebar_label: Schema
---

# What is a schema?

A schema is first of all a [json-schema](https://json-schema.org/).  
It represent how you want your content to be shaped, it represent a single entity with properties and property types, if you are familiar with Swagger, the syntax it's the same (swagger files are json-schema too).  

What Joyce adds, are **keywords** (prepended with a `$` sign) in properties definition, that drive how to **tranform** the source into the described content.   

In this way a Schema is a DSL that describe an *entity* and how to trasform a source in this *entity*.

Let's see an example:

```yaml
$schema: https://joyce.sourcesensce.com/v1/schema
$metadata:
  name: user
  description: A test schema
  subtype: import
  uid: code
  collection: users
  development: true
  namespace: clients
  store: true
  indexes:
    - kind: 1
    - email: 1
      kind: 1
type: object
properties:
  code:
    type: integer
    $path: "$.user_id"
  full_name:
    type: string
    $path: ["$.first_name", " ", "$.last_name"]
  email:
    type: string
  kind:
    type: string
    $fixed: "SimpleUser"
  collection:
    type: string
    $meta: "$.collection"
```

Applying this schema to the following source content:

```json
{
  "user_id": "1337",
  "first_name": "Jon",
  "last_name": "Snow",
  "email": "jon@winterfell.cold",
  "state": "Westeros"
}
```

Will result in this transformed content:
```json
{
  "code": 1337,
  "full_name": "Jon Snow",
  "email": "jon@winterfell.cold",
  "kind": "SimpleUser"
}
```

Let's dissect the schema.  

The `$schema` node is part of the json-schema specification.  
`$metadata` node stores information used during all the flows of the content.  See [$metadata](#metadata)

Then starts a standard json-schema object that descibe our desired content, the keys `$path` and `$fixed` are joyce specific nodes that tells the schema engine how to precess the source.  

Those key are tied to built-in [Handlers](#handlers), that implements a logic. You can [extend the Schema DSL](extending) defining your own.

## How it works

The engine that process the schema takes as in put a schema and a source json and output a transofrmed json.   
It iterates through properties of the schema and for each one of them:

- if it finds an Handler's key, applies the handler and put the result as the value of the property (*code*, *full_name*, *kind*)
- if it does not find an Handler's key, tries to get the value from the source json with the same key. (*email*)

It does it cascading for nodes of `array` and `object` types.   
Tries to convert types autonomously (*code*)

## Namespace

A schema is characterized by a namespace, a namespace is expressed with dot notation, like a package ( `clients.automotive.ford` ). The namespace is used as a prefix for the name that identifies a schema, as well for the collection where the content will be saved.

A schema with this metadata: 
```yaml
$metadata:
  name: italian-car
  collection: cars
  namespace: clients.automotive.ford
  subtype: import
```

will be saved with this uid `joyce://schema/import/clients.automotive.ford.italian-car` and will generate contents with uids like `joyce://content/import/clients.automotive.ford.cars/<UID>` that will get saved in collection `clients.automotive.ford.cars`.

Namespace will give you a way to organize schemas in a hierarchical way.

## Schema Inheritance

You can define a schema to have a parent schema:
```yaml
$metadata:
  name: user-child
  description: A test child schema
  subtype: import
  parent: joyce://schema/import/user
```
The schema having a parent schema, will have those metadata values overriden (they are no longer required in the child schema):

- namespace
- collection
- uid

The processed content is validated not only against the child schema but also against the parent.

This is useful when you have content coming from different import sources that is in different shapes, so you need different schemas to transform it, but you want as anoutput the same entity and store it together in a common collection.   
See this [Tutorial](/tutorial-accomodations).


## Connectors

From version 1.2.0 of the import-gateway, you can specify kafka connectors configuration inside the schema to manage with a unique declarative object the whole import pipeline.

This is done within the `extra` node of metadata. See [extra.connectors](#extraconnectors) to learn how to configure them.

`import-gateway` based on these connectors configuration, will manage directly the connector by invoking Kafka Connect REST API to create update and delete connectors as specified inside the schema.

It will apply (if not specofied) a transofrmation to build correctly the kafka message key to process the message.

In addition `import-gateway` expose [additional API](import-gateway#connectors-api) on the schema resource to restart, pause and resume the connector configured.

```
extra:
    connectors:
      - name: "news-sourcenews-connect"
        config:
          connector.class: "org.kaliy.kafka.connect.rss.RssSourceConnector"
          rss.urls: "https://news.sourcesense.com/rss"
          topic: "joyce_import"
```



## Metadata

The `$metadata` node is one of the enhancement that Joyce adds, it stores metadata information that will be used during all the flow of the content.

### name

*(required)*  
Is the name of the schema, along with the namespace and subtype, identifies univocally the schema inside the system. 

### subtype

*(required)*  
Indicates the type of the schema. It participates in the creation of the [Joyce URI](joyce-uris) of the schema.
### namespace

*[default: default]*  
Organize and  group schema in a hierarchical way, it is defined with  a package like dot notation (like java packages). 

### description

Is just an optional description of the schema.

### uid

*(required)*  
Tells which key of the defined entity is to be used as Unique Identifier of the content.

### collection

*(required)*  
Is the name of the collection (prefixed by teh namespace) in which the produced content will be stored.


### store

*[default: true]*  
If **false** content generated from this schema is published to the `joyce_content` topic but not stored by the Sink

### indexes

*[default: []]*  
A list of `Map<String,Int>` that defines indexes to be created by the sink in the content collection.

ie. the following configuration created an index on field **section** DESC, and a composite index on fields **section DESC**, **published_date ASC**.

```yaml
indexes:
  - section: 1
  - section: 1
    published_date: 0
```

### development

*(default false)*  
If true mark the schema as a development schema and saving the schema changed will skip [versioning controls].(import-gateway#versioning)

### parent

If specified it must be a valid Schema [Joyce URI](joyce-uris).  
See [Schema Inheritance](#schema-inheritance)


### extra 
*(default {})*  
This node has extra features that are specific for the type of schema.

### extra.filter

From version `1.3` you can enter a [JSON Logic](https://jsonlogic.com/) expression that will filter content to be imported with this schema. If the expression evaluate to false the content get skipped.   
It is useful when a  connector produce a lota of data that we don't want to get processed.   
See [JSON logic docs](https://jsonlogic.com/operations.html) know how to express the filter. 
### extra.connectors
*(default {})*  

This node is an array of connectors configuration, these connectors are automatically configured to build a suitable key ([see Connectors section](connectors) for more info) to use the schema we are working on.

a connectors element is like this:
```yaml
name: techcrunch-news   # the name that will be used to create the effective connector on Kafka Connect
importKeyUid: link      # the field that will be used as unique id in the messages produced by the connector
config: # Kafka connect specfic configurations
  connector.class: "org.kaliy.kafka.connect.rss.RssSourceConnector"
  rss.urls: "https://feeds.feedburner.com/Techcrunch"
  ...
```

## Handlers

In order transform the content from the source json to what gets publicated to kafka and then stored and served, you use `schema handlers` by defining in the schema how to populate de properties of your final document.

These are the built-in handlers.

### $path

Use it's value as a `json-path` expression, the underlying implementation is [Jayway JsonPath](https://github.com/json-path/JsonPath) so it's full semantic is supported.

The exprassion is evaluated and the result returned.

If it's value is an array, every item of the array that starts with a json-path expression (`$` sign) is evalueted and joined with the other that are not.

As an add on feature you can specify a default value just inside the json-path expression in this way `$.some.path ?? DEFAULT_VALUE`.

### $meta

It's the same as `$path` handler but the json-path expression is applied to the `$metadata` node instead of the source json.

### $fixed

It simply use it's value as the value to return.

### $script

This handler will let you write a small script that has access to the orignal source, context and schema metadata to produce a value for the field where it is used.
It is not intended to run complex programs but small scripts to manipulate teh source content.

The code specified will get wrapped inside a function to wchich  are passed 3 object:

- a dict/map object representing the source json
- a dict/map object representing the schema metadata
- a generic context object that will vary in its content by  the handlers used before.

the function  in python  is something like this:

```python
def wrapper(source, metadata, context):
```

This handler is configured with this parameters:

#### Configuration
- `language`   
the language used by the script, at the moment you can  use these 3 engines: **python**, **javascript** and **groovy**.
- `online`   
**[default: true]** Whether the script is a oneliner or multiline. If it is a multiline script **YOU MUST write a return statement**. The return value is implicit if you use online script.
- `code`  
The code of the script.

#### Example

```yaml
$script:
  language: python
  oneLine: false
  code: |
    asd = source['object']['objectField'].upper()
    return {'field1': asd, 'field2': 'newField'}
```

### $rest

With this handler you can populate a schema property value with the result of an http call.  
You can control pretty much everything of the http call: Method, Headers, Body and Url parameters.

There is a special config `vars` where you can define variables that you can use in the aformentioned fields values (headers, url, body).  
These variables can be populated with a `josn-path` expression executed on the source data.  

In request construction fields `url`, `headers`, `body` you can use the variables using a [Mustache](https://mustache.github.io/mustache.5.html) template syntax.

To return what you are interested from the http response, you can use the config `extract` that is a `json-path` expressione executed on the result of the http call.

:::caution
The handler expects the http call to return json.
:::

#### Configuration
- `url`   
The url to call, include here url get parameters as you wish, in every part of the url you can use a variable.  
(ie. `http://example.com/api/{{resource}}?startfrom={{date}}`).
- `method`   
The http method, GET, POST, ...
- `headers`  
A map of headers to include in the request. You can use variables in teh value of the request
- `vars`  
A map of vars that you can use as variables in other field, the value of every key is a `json-path` expression to extract fields from the source.
- `extract`  
The `json-path` expression used to extract the result
#### examples

```yaml
$rest:
  url: "https://example.com/api/users?email={{email}}"
  method: GET
  headers:
    Content-Type: application/json
  vars:
    email: "$.user_email"
  extract: "$.data"
```

```yaml
$rest:
  url: "https://example.com/api/search"
  method: POST
  headers:
    Content-Type: application/json
    Authorization: Bearer {{token}}
  body: |
    {
      "user": "{{username}}",
      "search": "simple"
    }
  vars:
    token: "$.user.token"
    username: "$.user.name"
  extract: "$.result"
```

