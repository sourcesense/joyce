/* eslint-disable no-console */
const fetch = require("node-fetch");
const fs = require("fs");
const _ = require("lodash");
const yaml = require("js-yaml");
const path = require("path");
const crypto = require("crypto");
const Handlebars = require("handlebars");

const SCHEMAS_SOURCE = process.env.SCHEMAS_SOURCE || "src/templates/schemas.json";
const WORKDIR = process.env.WORKDIR || "./workdir";
const mongoURI = process.env.MONGO_URI || "mongodb://localhost:27017/ingestion";
const HASHES_FILE = `${WORKDIR}/hashes.json`;

async function fetchSchema(name, config) {
	let response =  await fetch(config["source"]);
	let text = await response.text();
	text = text.replace(/\integer/g, "number");
	let json = JSON.parse(text);
	json.schema["$metadata"]["name"] = _.upperFirst(_.camelCase(json.schema["$metadata"]["name"]));
	json.schema["$metadata"]["endpoint"] = name;
	json.name = _.upperFirst(_.camelCase(json.name));

	const data = JSON.stringify(json);
	
	
	return data ;
}


function writeModel(data, key, template) {
	let name = _.upperFirst(_.camelCase(key));
	let json_schema_module = path.resolve("./src/scripts/json-schema.js");
	let model = template({ data, name, json_schema_module });
	fs.writeFileSync(`${WORKDIR}/${name}.model.js`, model, "utf8");
}

function saveMeshrc(keys, mongoURI) {
	
	let sources = [];
	if (keys.length == 0) {
		sources.push({
			name: "Sample Stackoverflow api",
			handler: {
				openapi: {
					source: "https://raw.githubusercontent.com/grokify/api-specs/master/stackexchange/stackexchange-api-v2.2_openapi-v3.0.yaml"
				}
			}
		});
	} else {
		sources.push({
			name: "Mongoose",
			handler: {
				mongoose: {
					connectionString: mongoURI,
					models: keys.map((key) => {
						const name = _.upperFirst(_.camelCase(key));
						return { 
							name: name, 
							path: path.resolve(`${WORKDIR}/${name}.model.js`) 
						}; 
					}),
				},
			},
		});
	}
	// Write meshrc
	let ymlRc = {
		sources: sources,
		require: [
			"ts-node/register/transpile-only",
		],
		serve: {
			customServerHandler: path.resolve("./src/scripts/mesh-server.js"),
		},
	};

	fs.writeFileSync(`${WORKDIR}/.meshrc.yml`, yaml.dump(ymlRc), "utf8");
}

async function run() {
	var data = fs.readFileSync(SCHEMAS_SOURCE, {
		encoding: "utf8",
	});
	let schemaJson = JSON.parse(data);
	
	const template = fs.readFileSync("./src/templates/model.js.handlebars", "utf8");
	const compiledTemplate = Handlebars.compile(template);
	for (const key of Object.keys(schemaJson["schemas"])) {
		// Fetch has
		let data  = await fetchSchema(key, schemaJson["schemas"][key]);
		writeModel(data, key, compiledTemplate);
	}

	// compare computed schemas hashes, if they differ we exit with a code to let npm script do a mesh build
	saveMeshrc(Object.keys(schemaJson["schemas"]), mongoURI);	
}

(async function() {
	await run();
})();

