---

---

# Import Gateway

The Import Gateway is the single point where you ingest your data into the system.
It manages import schemas and applies them to the content you send to it.

You can image it as a function that given a schema and a content as input it outputs the content transformed as the schema describes.

It has two functions: Managing schemas and Import Content.

## Import API

Import Gateway has two interface to process source content a **REST API** and a **Kafka Consumer API**.

### REST API

Swagger docuemntation can be find here [http://localhost:6651/docs](http://localhost:6651/docs).
To specify the schema to use you have to set a custem header `X-Joyce-Schema-Id`.
Different endpoints let you insert/delete a single content, bulk insert from a csv or just testing a scehma transformation with a dry run.


#### POST /api/import

Applies the **schema** specified by `X-Joyce-Schema-Id`  and publish the processed content to `content-topic` and returns the id generated for the content.

#### DELETE /api/import

Applies the **schema** specified by `X-Joyce-Schema-Id` and publish an empty message to mark the deletion of the given content.

#### POST /api/import/bulk

Applies the **schema** specified by `X-Joyce-Schema-Id` to every line of the csv sent as multipart along the http request and publish a message to `content-topic` for every line transofrmed.
The csv has to be valid and the first line is parsed as header giving name to columns.

#### POST /api/import/dryrun

Applies the **schema** specified by `X-Joyce-Schema-Id` and returns syncronously the result of the processing, WITHOUT pushing it inside kafka.


### Kafka Consumer API

Import Gateway consumes messages from `joyce_import`, it exepcts the message to have a Json message key in this form:

```json
{
  "uid": "someUniqueValue",
  "schema": "joyce://schema/import/project",
  "source": "jdbc-source-project"
}
```

- `uid` is a unique identifier of the imported content
- `schema` is the uri of the schema to apply
- `source` identify the source of the content

This information can be configured to be set on a [Kafka Connect](connectors) with a transformation configuration.

If you specify the [connectors inside the schema](schema#connectors) the transformation to obtain a suitable key for the scheam is done automatically by import gateway. **This is the preferred way**.


## Schema Management

Input Gateway has a CRUD REST interface to manage [Schemas](schema), you can access its swagger documentation at [http://localhost:6651/docs](http://localhost:6651/docs).

With it you can do what you expect, create, read, update and delete schemas for a namespace, you can also list namespaces.
Updating schemas has few constraints, because the system tries to keep consistency within the data inside the system, preventing changes to the schema that can corrupt previous imported data.

When you upsert a schema, checks are made to understand what has changed, if there are **breaking changes** the schema cannot be saved.

Breaking changes are:

- remove a required property
- change the type of property

### Developing mode

Where you are in the process of developing a schema, you can bypass these checks by flagging the schema in developing mode, by putting this value in the Schema metadata

```yaml
metadata:
  ...
  development: true
```

This will save the content but it doesn't gurantee that data is validated against the schema when saved in the storage.

Once you set `development: false` in a schema you **cannot go back**.


## Connectors API

By configuring connectors directly inside the schema, `import-gateway` expose additional resources on the schema resource to control kafka connect tasks.

#### GET /api/schema/{subtype}/{namespace}/{name}/connectors

List connectors and their configurations, present in the schema definition.

#### GET /api/schema/{subtype}/{namespace}/{name}/connectors/{connector}/status

Output the status of the connector.

#### POST /api/schema/{subtype}/{namespace}/{name}/connectors/{connector}/restart

Restart the given connector.

#### PUT /api/schema/{subtype}/{namespace}/{name}/connectors/{connector}/pause

Pause tasks of the given connector.
#### PUT /api/schema/{subtype}/{namespace}/{name}/connectors/{connector}/resume

Restart tasks of the given connector.
