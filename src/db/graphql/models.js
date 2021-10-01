const { model, Schema } = require("mongoose");
const fs = require("fs");
const _ = require("lodash");
const createMongooseSchema = require("./json-schema").default;

const SCHEMAS_SOURCE = process.env.SCHEMAS_SOURCE || "assets/schemas.json";

var data = fs.readFileSync(SCHEMAS_SOURCE, {
	encoding: "utf8",
});
var schemaJson = JSON.parse(data);

var models = new Object();

Object.keys(schemaJson["schemas"]).forEach(function (key) {
	let data = JSON.parse(
		fs.readFileSync(`assets/${key}.json`, {
			encoding: "utf8",
			flag: "r",
		}),
	);
	/**
	 * the "_ID" field is added to define the presence of the properties in the rest api
	 */
	data.schema.properties["_id"] = { "type": "string" };
	const convertedSchema = createMongooseSchema(data.schema.properties, data.schema);
	let schema = new Schema(convertedSchema);
	let collection = `${data.schema["$metadata"]["namespace"] || "default"}.${data.schema["$metadata"]["collection"]}`;
	key = _.upperFirst(_.camelCase(key));
	models[key] = model(key, schema, collection);
});

module.exports = models;
