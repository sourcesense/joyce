---
title: Tutorial
---

# How to build with Joyce a Unified API for italian regional hotels

This tutorial is a practical, non trivial, real world example of how you can use Joyce platform.
We'll go step by step through the creation a complete flow of data integration and serving of a final API.

## Goal

We want to create an API that expose information on all italian territory tourism accomodation (Hotels, B&B, ...). Our data source are the data that every singular region publishes as open data on [this site](http://www.datiopen.it/en/catalogo-opendata/turismo-0).
We can download csv data of the accomodation region by region, the only problem is that every region exports data in similar but slighlty different way, some information are present or not, others have different column names.

What we want is to have a unified api for all the data and be able to update it over time.
We'll use Joyce to do everything, we'll define the shape of the final data with a Schema, and then we'll write a child schema for every different data source and describe its transofrmation.
We then use the bulk import capability of joyce to  import and transform all the data.

## Setup

You'll need to have docker installed, we'll be using docker-compose to sturtup a minimal installation of Joyce.

Let's begin:

```bash
mkdir italian-accomodations
cd italian-accomodations
wget https://raw.githubusercontent.com/sourcesense/joyce-compose/master/docker-compose.yaml

docker-compose up -d
```

Once everything is up (it will take a while, check  with `docker-compose ps`) you'll have:


- `import-gateway` API at [http://localhost:6651/docs](http://localhost:6651/docs)
- `rest` API at [http://localhost:6650/docs](http://localhost:6650/docs)
- AKHQ at [http://localhost:6680/](http://localhost:6680/)

`joyce-rest` has no endpoint configured, we must tell him about the collection/schema we want to expose, to do that we must first create a schema on `import-gateway` and then tell the `joyce-rest` about it.

First things first, let's shape the data we ant for our accomodation api.

## Write Schemas

A schema inside Joyce is a `json-schema` with an embedded DSL to make transformation, see [Schema docs](/docs/schema).
In our case things are more complex, we want a parent Schema with no transformations that represents the data we will serve, and several child schema with the actual transformation, one for every different source format (every italian region exports a csv with a different set of fields).

Something like this:

![schemas](/img/tutorial-compose/Schemas.png)

### Parent Schema

Save this file to `import-accomodation.yaml`

```yaml
$schema: https://joyce.sourcesense.com/v1/schema
metadata:
  subtype: import
  namespace: default
  name: accomodation
  description: A model that represents Accomodations
  development: true
  collection: accomodations
  uid: code
type: object
properties:
  code:
    type: string
  name:
    type: string
  email:
    type: string
  phone:
    type: string
  website:
    type: string
  location:
    type: object
    properties:
      city:
        type: string
      region:
        type: string
      cap:
        type: integer
```

This is a simple `json-schema` that shape and validate our data, the only strange thing is the `metadata` node, this tells our system what to do with content generated with this schema, most important fields are `collection` and `uid` taht respectively tells Joyce to which collection content  will  be  stored and which key is to be considered as unique id.
Refer to [Schema docs](/docs/schema) for further info.

### Save schema

Now we will register this schema to the `import-gateway`, either use following curl or do the same with the swagger ui.

```bash
curl -X POST -H "Content-Type: application/x-yaml" --data-binary @import-accomodation.yaml http://localhost:6651/api/schema
```

If everything is fine you'll have the schema registered:

```bash
curl http://localhost:6651/api/schema
```

### Setup joyce-rest

Now we should tell `joyce-rest` about this schema.

Edit `docker-compose.yaml` and add this to environment variables of `joyce-rest` service:
```yaml
- SCHEMAS_SOURCE=/opt/schemas.json
```
and this volume:
```yaml
volumes:
  - "./schemas.json:/opt/schemas.json"
```

then save this json to `schemas.json`

```json
{
    "schemas": {
        "accomodation": {
            "source": "http://import-gateway:6651/api/schema/import/default/accomodation"
        }
    }
}
```

Finally restart `joyce-rest`

```bash
docker-compose stop rest
docker-compose up -d rest
```

You should now see at [http://localhost:6650/docs](http://localhost:6650/docs) the accomodation endpoint exposed.

But obviously there is no data:

```bash
curl "http://localhost:6650/accomodation?orderBy=asc&sortBy=name"
[]
```

### Write first child schema

Before writing our first child schema we must know how our source data is shaped, go [here](http://www.datiopen.it/en/catalogo-opendata/turismo-0) and download the Regional data for Veneto.

```bash
curl "http://www.datiopen.it/export/csv/Regione-Veneto---Elenco-strutture-ricettive.csv" | iconv -f iso8859-1 -t utf-8 > veneto.csv
```

We pipe through iconv because we have to convert the file to utf-8 before processing.

Open the csv with your preferred editor and have a look at it (yep it is in italian, sorry ), so we can understand how to make the transformation to obtain our final data.

Here's the result, save it to `import-accomodation-veneto.yaml`

```yaml
$schema: https://joyce.sourcesense.com/v1/schema
metadata:
  subtype: import
  namespace: default
  name: accomodation-veneto
  description: A model that represents Accomodations for Veneto regional data
  development: true
  parent: joyce://schema/import/default.accomodation
type: object
properties:
  code:
    type: string
    $path: "$['Codice identificativo']"
  name:
    type: string
    $path: "$.Denominazione"
  email:
    type: string
    $path: "$['Posta elettronica']"
  phone:
    type: string
    $path: "$.Telefono"
  website:
    type: string
    $path: "$['Sito internet']"
  location:
    type: object
    properties:
      city:
        type: string
        $path: "$.Comune"
      region:
        type: string
        $fixed: "Veneto"
      cap:
        type: integer
        $path: "$.CAP"
  tipology:
    type: string
    $path: "$.Tipologia"
```

You see it has the same shape of our parent schema, but for every field we've used the `$path` handler to retrieve with a `json-path` expression the actual value we wanted (see [docs](/docs/schema) for other handlers and their usage ).

We also added a field `tipology` that we will store, but not serve within the api because it is not present on every source data.

Go ahead and save this schema too as before.


```bash
curl -X POST -H "Content-Type: application/x-yaml" --data-binary @import-accomodation-veneto.yaml http://localhost:6651/api/schema
```

A curl to retrieve schemas now should have this output:

```bash
curl -Ss http://localhost:6651/api/schema | jq
[
  {
    "uid": "joyce://schema/import/default.accomodation-veneto",
    "name": "accomodation-veneto",
    "description": "A model that represents Accomodations for Veneto regional data",
    "development": true
  },
  {
    "uid": "joyce://schema/import/default.accomodation",
    "name": "accomodation",
    "description": "A model that represents Accomodations",
    "development": true
  }
]
```

We are now ready to test our schema, get a sample json from the csv with this python fu:

```bash
head -n 2 Regione-Veneto---Elenco-strutture-ricettive.csv | python -c 'import csv;import json; from io import StringIO; import sys;reader = csv.DictReader(StringIO(sys.stdin.read()), delimiter=";"); print(json.dumps(list(reader), indent=2))'
```

Copy the json element and use `import-gateway` swagger-ui to test the schema by using `​/api​/import​/dryrun` dryrun endpoint, in the header put the schema id ``.

Put in schema id `accomodation-veneto` and you should have the transofrmed json as output:

```json
{
  "result": {
    "code": "307",
    "name": "CAORSA",
    "email": "info@caorsa.it",
    "phone": "0457235039",
    "website": "www.caorsa.it",
    "location": {
      "city": "AFFI",
      "region": "Veneto",
      "cap": 37010
    },
    "tipology": "AFFITTACAMERE"
  }
}
```

The transformation seems fine, try to really import it now by using the import endpoint `​/api​/import​`.

try to query now `joyce-rest`:

```bash
curl "http://localhost:6650/accomodation?orderBy=asc&sortBy=name"
```

You should see your imported document transofrmed.
## Bulk import the file

Time now to import the whole document.
from swagger use the `​/api​/import​/bulk` import, set the schema id and change the separators accordingly `;` for column and `,` for array elemnts.

Then choose the csv to import.

Output will be an error message, something like :
```json
{
  "message": "com.sourcesense.joyce.schemaengine.exception.InvalidSchemaException: $.location.cap: null found, integer expected",
  "error": "com.sourcesense.joyce.core.exception.NotifiedException"
}
```
This means something went wrong processing an entry.
When bulk importing, the process stops at the first error giving you notice of it syncronously, but indeed the good ones before were correctly processed.

You can consume the api to see it:

```bash
curl "http://localhost:6650/accomodation?orderBy=asc&sortBy=name"
```

Let's go deeper and see what happened...

## Notifications & Troubleshooting

When a content enter inside Joyce platform, it gets uris to be identified across the steps it pass through, and every component that interact with it sends notification of success or failure on `joyce-notification` kafka topic.

You can do whatever you like with them and they are structured in a way that it is easy to track flow of a content.

If you have an ELK stack within your infrustructure it's easy to setup a Kafka Connect connector to sink notifications on an Elasticsearch index.

For a quick troubleshoot without this overhead we can use akhq to read errors notification, go to [http://localhost:6680/](http://localhost:6680/) and select topic `joyce_notification` in the search box insert `IMPORT_INSERT_FAILED` few message should pop up:

We are interested in metadata field: `$.location.cap: null found, integer expected` schema was expecting an Integer instead he got an empty value, you can view the content that triggered this error:

you'll see that the json has `"CAP":""` the cause of our error.

Now based on how you want your data, either you can ignore the error and reject the entry, being sure all entry have the correct info, or you can relax the schema and import content with this missing data anyway.

For the purpose of exercise we'll change the schema to accept nullable values on `cap` field.
Open `import-accomodation-veneto.yaml` and change the description of `cap` field as follows

```yaml
cap:
  type: ["integer", "null"]
  $path: "$.CAP"
```

Update the schema on the Import Gateway:
```bash
curl -H "Content-Type: application/x-yaml" --data-binary @import-accomodation-veneto.yaml http://localhost:6651/api/schema
```

We must do the same for the parent schema, because the produced json is verified against the parent schema too, make the same change on `import-accomodation.yaml` and save it:

```bash
curl -H "Content-Type: application/x-yaml" --data-binary @import-accomodation.yaml http://localhost:6651/api/schema
```

Use again the bulk import call from swagger with the same file.

:::info
Don't worry for the reprocessing of all the lines, the operation is idempotent, docs with the same id will just be overwritten.
:::

## Consume REST api

Now you can use `joyce-rest` and get data:

```bash
curl "http://localhost:6650/accomodation?orderBy=asc&sortBy=name"

# use pagination and ordering
curl "http://localhost:6650/accomodation?orderBy=desc&sortBy=name&size=10&page=4"

# Get a single object
curl "http://localhost:6650/accomodation/1704"
```



## Other data sources

Go to [the site](http://www.datiopen.it/en/catalogo-opendata/turismo-0) and download data for Sicily region:
```bash
curl "http://www.datiopen.it/export/csv/Regione-Sicilia---Mappa-delle-strutture-ricettive.csv" | iconv -f iso8859-1 -t utf-8 > sicily.csv
```

Again with python kung-fu get one entry to view how data is:

```bash
head -n 2 data/connect/toprocess/Regione-Sicilia---Mappa-delle-strutture-ricettive.csv | python -c 'import csv;import json; from io import StringIO; import sys;reader = csv.DictReader(StringIO(sys.stdin.read()), delimiter=";"); print(json.dumps(list(reader), indent=2))'
```

and write a new schema to `import-accomodation-sicily.yaml`

```yaml
$schema: https://joyce.sourcesense.com/v1/schema
metadata:
  subtype: import
  namespace: default
  name: accomodation-sicilia
  description: A model that represents Accomodations for Sicilia regional data
  development: true
  parent: joyce://schema/import/default.accomodation
type: object
properties:
  code:
    type: string
    $path: "$.Codice"
  name:
    type: string
    $path: "$.Nome"
  email:
    type: string
    $path: "$['Indirizzo posta elettronica']"
  phone:
    type: string
    $path: "$.Telefono"
  website:
    type: string
    $path: "$['Sito internet']"
  location:
    type: object
    properties:
      city:
        type: string
        $path: "$.Comune"
      region:
        type: string
        $fixed: "Sicily"
      cap:
        type: "null"
```

Save it and test it like before:

```bash
curl -H "Content-Type: application/x-yaml" --data-binary @import-accomodation-sicily.yaml http://localhost:6651/api/schema
```

Bulk import this file using the new schema `joyce://schema/import/default.accomodation-sicilia`.

Quering `joyce-rest` you should see also the data from Sicily:

```bash
curl "http://localhost:6650/accomodations?orderBy=asc&sortBy=name&size=5&location.region=Sicily"
```
:::tip
It's easy to add new sources, just a schema uplaod a file and boom tons of data just in the form you needed, how cool is that?
:::
## Conclusion

This is a simple, but effective use case on how Joyce can be used, but you can have already grasped its potential, following tutorial will be focused on how to setup a connector to automate ingestion.
