const fetch = require("node-fetch");
const fs = require("fs");
const _ = require("lodash");

const SCHEMAS_SOURCE = process.env.SCHEMAS_SOURCE || "assets/schemas.json";

function dl(schemaJson, key) {
	if (schemaJson["schemas"][key] !== undefined && schemaJson["schemas"][key]["source"] !== undefined) {
		fetch(schemaJson["schemas"][key]["source"]).then((response) =>
			response.text().then((json) => {
				json = json.replace(/\integer/g, "number");
				json = JSON.parse(json);
				json.schema["$metadata"]["name"] = _.upperFirst(_.camelCase(json.schema["$metadata"]["name"])); // remember edit in SchemaConfiguration.ts
				json.schema["$metadata"]["endpoint"] = key;
				json.name = _.upperFirst(_.camelCase(json.name));
				fs.writeFileSync(`assets/${key}.json`, JSON.stringify(json), "utf8");
			}),
		);
	}
}

var data = fs.readFileSync(SCHEMAS_SOURCE, {
	encoding: "utf8",
});
var schemaJson = JSON.parse(data);

Object.keys(schemaJson["schemas"]).forEach(function (key) {
	dl(schemaJson, key);
});
