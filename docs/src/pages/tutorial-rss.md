# How to build an API that enrich and aggregates rss feeds.

This tutorial is a practical, non trivial, real world example of how you can use Joyce platform.
We'll go step by step through the creation a complete flow of data integration and serving of a final API.

## Goal

We want to realize an api that aggregates several rss feeds and enrich them extracting topics and categorization of the link that arrives.
Our source will be IT news sites:

- http://feeds.arstechnica.com/arstechnica/index/
- https://www.engadget.com/rss.xml
- https://hnrss.org/newest?points=100

## Setup

You'll need to have docker installed, we'll be using docker-compose to startup a minimal installation of Joyce.

Let's begin:

```bash
mkdir rss-aggregator
cd rss-aggregator
wget https://raw.githubusercontent.com/sourcesense/joyce-compose/master/docker-compose.yaml

docker-compose up -d
```

# Modeling our sources

We start by modeling our data with a Joyce [schema](/docs/schema), and configuring how to pull data from the rss feed with a [Kafka Connect RSS source connector](https://github.com/kaliy/kafka-connect-rss/blob/master/README.md) configuration inside the schema.

As you can read from the documentation, the connector produce json with this form:
```json
{
  "feed": {
    "title": "CNN.com - RSS Channel - App International Edition",
    "url": "http://rss.cnn.com/rss/edition.rss"
  },
  "title": "The 56,000-mile electric car journey",
  "id": "https://www.cnn.com/2019/03/22/motorsport/electric-car-around-the-world-wiebe-wakker-spt-intl/index.html",
  "link": "https://www.cnn.com/2019/03/22/motorsport/electric-car-around-the-world-wiebe-wakker-spt-intl/index.html",
  "content": "For three years and 90,000 kilometers and counting, he's traveled the world powered both by electricity and strangers' kindness.",
  "author": "CNN",
  "date": "2019-03-22T13:34:17Z"
}
```

It's trivial to write a schema that reshape this content, save this to `news.yaml`:

```yaml
$schema: https://joyce.sourcesense.com/v1/schema
metadata:
  type: import
  domain: test
  product: default
  name: technology-news
  description: Technology news from rss feeds
  production: false
  validation: false
  uid: link
  extra:
    connectors:
      - name: arstechnica
        importKeyUid: link
        config:
          connector.class: "org.kaliy.kafka.connect.rss.RssSourceConnector"
          rss.urls: "http://feeds.arstechnica.com/arstechnica/index/"
          topic: "joyce_import"
type: object
properties:
  link:
    type: string
  title:
    type: string
  date:
    type: string
  summary:
    type: string
    value: $.src.content
  source:
    type: string
    value: $.src.feed.title
```

And than send it to the import-gateway:

```bash
curl -X POST -H "Content-Type: application/x-yaml" --data-binary @news.yaml http://localhost:6651/api/schema
```

You'll see this output:

```json
{
  "schemaUri": "joyce://schema/import/default.technology-news",
  "connectors": [
    {
      "name": "default.technology-news.arstechnica",
      "connectorOperation": "CREATE",
      "status": "CREATED",
      "body": {
        "name": "default.technology-news.arstechnica",
        "config": {
          "connector.class": "org.kaliy.kafka.connect.rss.RssSourceConnector",
          "rss.urls": "https://techcrunch.com/feed",
          "topic": "joyce_import",
          "transforms": "joyceKey",
          "transforms.joyceKey.uid": "link",
          "transforms.joyceKey.source": "arstechnica",
          "transforms.joyceKey.schema": "joyce://schema/import/default.technology-news",
          "transforms.joyceKey.type": "com.sourcesense.joyce.connect.custom.InsertJoyceMessageKey",
          "name": "default.technology-news.arstechnica"
        },
        "tasks": [],
        "type": "source"
      }
    }
  ]
}
```

##  Kafka Connectors

A lot already happened, not only the schema is saved, but giving the configuration of the connector, a kafka connect task with the specific configuration is created and started.

This means that if we didn't do anything wrong in the schema transofrmation, content is already pumped through joyce and correctly transformed.

Head over [akhq](http://localhost:6680/ui) and look at the content of `joyce_content` topic, you'll see messages from the rss feed already processed.

Import Gateway expose [different endpoints](/docs/import-gateway#connectors-api) to control and check the status of the connectors within a schema.

Now pause the connector, because we want to enrich the schema with more complex transformation:

```bash
curl -X  PUT http://localhost:6651/api/schema/import/default/technology-news/connectors/arstechnica/pause
```

## Transformation Handlers

The result of the transoformation is good, but we want to enrich what arrives from the rss feeds, we miss few things to have a nice api:

- a categorization of the content.
- an image for the article if we can get to it.
- a summary more relevant than what could arrive from the feed.


How can we do it? We'll be using the power of joyce [transformation handlers](/docs/schema#handlers), in particular  **$script** and **$rest**.
Let's see how.

### Categorization of the content

We need to extracts topics from the article text, there are tons of way to do it with NLP libraries and custom code, but we go'll the short way and use a service that does it with an exposed API.

Head over [https://www.textrazor.com/signup](https://www.textrazor.com/signup) and signup for a free account, you'll obtain an api token that is everything we need to use their service.
Have a look at their [rest api documentation](https://www.textrazor.com/docs/rest) and try this call to extract topics from a random link:

```bash
curl -X POST \
    -H "x-textrazor-key: YOURAPIKEY" \
    -d "extractors=topics" \
    -d "url=https://tenthousandmeters.com/blog/python-behind-the-scenes-13-the-gil-and-its-effects-on-python-multithreading/" \
    https://api.textrazor.com/
```

We are ready to enrich our imported model with topics by using this call inside the schema using `$rest` handler, add this field to your Schema:

```yaml
  topics:
    type: array
    apply:
      - handler: rest
        args:
          url: https://api.textrazor.com/
          method: POST
          headers:
            x-textrazor-key: YOURAPIKEY
          body: "extractors=topics&url={{ ctx.src.url }}"
      - handler: extract
        args: "$.out.response.topics[?(@.score > 0.9)]"
    items:
      type: string
      value: $.src.label
```

What are we doing here??
We're adding a field `topics` that is an array of string, we populate it with a `$rest` handler that calls the TextRazor api and extract the topics label, we used some filtering in the json path expression to take more relevant extracted topics (see [json-path docs](https://github.com/json-path/JsonPath)).

Save the yaml, and update the schema:
```bash
curl -H "Content-Type: application/x-yaml" --data-binary @news.yaml http://localhost:6651/api/schema
```

Before restarting the connector we're going to test the transformation with a dry run  using a json you can retrieve from the `joyce_import` topic.

```bash
curl -X POST "http://localhost:6651/api/import/dryrun" -H  "accept: application/json; charset=utf-8" -H  "X-Joyce-Schema-Id: joyce://schema/import/default.technology-news" -H  "Content-Type: application/json" -d '{"feed":{"title":"Ars Technica","url":"http://feeds.arstechnica.com/arstechnica/index/"},"title":"A new formula may help Black patients’ access to kidney care","id":"https://arstechnica.com/?p=1798361","link":"https://arstechnica.com/?p=1798361","content":"Algorithm made it harder for Black patients to qualify for transplants, other treatments.","author":"WIRED","date":"2021-09-25T11:22:23Z"}'
```

We are happy with the output, topics are extracted nicely.

### Enrich with open graph data

We want to enrich more the results with some more info, a summary and an image for the article, how can we do it?

Every news site, usually, includes [Open Graph](https://ogp.me/) tags, we could grab and use them to obtain what we want.

Joyce ships with a `$script` handler that gives the ability to use scripting language to make transformations, currently you can write scripts in `python`,  `javascript`, `groovy`.

We'll go with python.
We need to obtain the http from the url, parse the html and get the metadata tags we need.

With some python/regex kung-fu we can write a small script to do that, add this property to the Schema:

```yaml
  metadata:
    type: object
    $script:
      language: python
      multiline: true
      code: |
        import urllib
        import re
        html = urllib.urlopen(source['link']).read()
        img =  re.search("<meta[^\>]*property=\"og:image\"[^\>]*content=\"(.+?)\"[^\>]*>", html)
        desc = re.search("<meta[^\>]*property=\"og:description\"[^\>]*content=\"(.+?)\"[^\>]*>", html)
        return {
          'image': img.group(1) if img is not None else "",
          'description': desc.group(1) if desc is not None else ""
        }
    properties:
      image:
        type: string
      description:
        type: string
```

Save the schema and try again a dryrun you should have as a result something like this:
```json
{
  "link": "https://arstechnica.com/?p=1798361",
  "title": "A new formula may help Black patients’ access to kidney care",
  "date": "2021-09-25T11:22:23Z",
  "summary": "Algorithm made it harder for Black patients to qualify for transplants, other treatments.",
  "source": "Ars Technica",
  "topics": [
    "Information technology",
    "Computing",
    "Internet properties",
    "Websites",
    "World Wide Web",
    "Cyberspace",
    "Internet",
    "Technology",
    "Digital media",
    "Multimedia",
    "Communication",
    "Software",
    "Privacy",
    "Mass media"
  ],
  "metadata": {
    "image": "https://cdn.arstechnica.net/wp-content/uploads/2021/09/kidney-760x380.jpg",
    "description": "Algorithm made it harder for Black patients to qualify for transplants, other treatments."
  }
}
```

Now  we can resume operation of the kafka connector:

```bash
curl -X  PUT http://localhost:6651/api/schema/import/default/technology-news/connectors/arstechnica/resume
```


##  Expose the rest api

Now we should tell `joyce-rest` about the schema to expose it.

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
        "news": {
            "source": "http://import-gateway:6651/api/schema/import/default/technology-news"
        }
    }
}
```

Finally restart `joyce-rest`

```bash
docker-compose stop rest
docker-compose up -d rest
```

You can call your new shiny api:
```bash
curl http://localhost:6650/news?orderBy=desc&sortBy=date
```

Yay, News!

## Add other sources

Time now to add another rss source, add this element to `metadata.extra.connectors`  array:

```yaml
      - name: engadget
        importKeyUid: link
        config:
          connector.class: "org.kaliy.kafka.connect.rss.RssSourceConnector"
          rss.urls: "https://www.engadget.com/rss.xml"
          topic: "joyce_import"
```

Save the schema and send it to the import gateway, soon enough, by calling again the rest api you should see articles from engadget too.

Your schema now, should be something like this:

```yaml
$schema: https://joyce.sourcesense.com/v1/schema
metadata:
  type: import
  domain: test
  product: default
  name: technology-news
  description: Technology news from rss feeds
  production: false
  validation: false
  uid: link
  extra:
    connectors:
      - name: arstechnica
        importKeyUid: link
        config:
          connector.class: "org.kaliy.kafka.connect.rss.RssSourceConnector"
          rss.urls: "http://feeds.arstechnica.com/arstechnica/index/"
          topic: "joyce_import"
      - name: engadget
        importKeyUid: link
        config:
          connector.class: "org.kaliy.kafka.connect.rss.RssSourceConnector"
          rss.urls: "https://www.engadget.com/rss.xml"
          topic: "joyce_import"
type: object
properties:
  link:
    type: string
  title:
    type: string
  date:
    type: string
  summary:
    type: string
    value: $.src.content
  source:
    type: string
    value: $.src.feed.title
  topics:
    type: array
    apply:
      - handler: rest
        args:
          url: https://api.textrazor.com/
          method: POST
          headers:
            x-textrazor-key: 8023291c131f0afb1c5ef775356bf04d2ee3f1af39ba64a9e03c98c5
          body: extractors=topics&url={{ ctx.src.link }}
      - handler: extract
        args: "$.out.response.topics[?(@.score > 0.9)]"
    items:
      type: string
      value: $.src.label
  metadata:
    type: object
    apply:
      - handler: script
        args:
          language: python
          multiline: true
          code: |
            import urllib
            import re
            html = urllib.urlopen(source['link']).read()
            img =  re.search("<meta[^\>]*property=\"og:image\"[^\>]*content=\"(.+?)\"[^\>]*>", html)
            desc =  re.search("<meta[^\>]*property=\"og:description\"[^\>]*content=\"(.+?)\"[^\>]*>", html)
            return {
              'image': img.group(1) if img is not None else "",
              'description': desc.group(1) if desc is not None else ""
            }
    properties:
      image:
        type: string
      description:
        type: string
```

## Conclusion

You know how to add the third source right?
s
How powerful it is to have **input source**, **modeling data** and **transformation** in a single, declarative file?
It is the only thing you should version, no code, just configuration, and you have enriched news news from multiple rss sources.
