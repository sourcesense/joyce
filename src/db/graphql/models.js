const { model, Schema } = require("mongoose");
const fs = require("fs");
const _ = require("lodash");

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
	data.schema.properties["_id"] = { "type": "string" };
	let schema = new Schema(data.schema.properties);
	let collection = `${data.schema["$metadata"]["namespace"] || "default"}.${data.schema["$metadata"]["collection"]}`;
	key = _.upperFirst(_.camelCase(key));
	models[key] = model(key, schema, collection);
});

module.exports = models;
