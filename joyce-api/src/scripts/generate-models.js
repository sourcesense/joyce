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

async function fetchSchemaAndHash(name, config) {
	let response =  await fetch(config["source"]);
	let text = await response.text();
	text = text.replace(/\integer/g, "number");
	let json = JSON.parse(text);
	json.schema["$metadata"]["name"] = _.upperFirst(_.camelCase(json.schema["$metadata"]["name"]));
	json.schema["$metadata"]["endpoint"] = name;
	json.name = _.upperFirst(_.camelCase(json.name));

	const data = JSON.stringify(json);
	
	const hash = crypto.createHash("md5").update(data).digest("hex");
	
	return { hash, data };
}


function writeModel(data, key, template) {
	let name = _.upperFirst(_.camelCase(key));
	let json_schema_module = path.resolve("./src/scripts/json-schema.js");
	let model = template({ data, name, json_schema_module });
	fs.writeFileSync(`${WORKDIR}/${name}.model.js`, model, "utf8");
}

function saveMeshrc(keys, mongoURI) {

	// Write meshrc
	let ymlRc = {
		sources: [
			{
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
			},
		],
		require: [
			"ts-node/register/transpile-only",
		],
		serve: {
			customServerHandler: path.resolve("./src/scripts/mesh-server.js"),
		// browser: false,
		// method: "GET",
		// endpoint: "/query",
		},
	};

	fs.writeFileSync(`${WORKDIR}/.meshrc.yml`, yaml.dump(ymlRc), "utf8");
}

async function run() {
	var data = fs.readFileSync(SCHEMAS_SOURCE, {
		encoding: "utf8",
	});
	let schemaJson = JSON.parse(data);
	let hashes = {};


	// Load hashes file if it exists
	if (fs.existsSync(HASHES_FILE)) {
		hashes = JSON.parse(fs.readFileSync(HASHES_FILE, { encoding: "utf8" }));
	} 
	let schemas_hash = Object.keys(hashes).reduce((acc, key) => {
		acc.update(hashes[key] || "");
		return acc;
	}, crypto.createHash("md5")).digest("hex");
	
	
	// Iterate models
	const new_hash = crypto.createHash("md5");
	const template = fs.readFileSync("./src/templates/model.js.handlebars", "utf8");
	const compiledTemplate = Handlebars.compile(template);
	for (const key of Object.keys(schemaJson["schemas"])) {
		// Fetch has
		let { hash, data } = await fetchSchemaAndHash(key, schemaJson["schemas"][key]);
		
		if (hash != hashes[key] || "") {
			// write model if hashes differ
			writeModel(data, key, compiledTemplate);
		}
		hashes[key] = hash;
		new_hash.update(hash);
	}

	// compare computed schemas hashes, if they differ we exit with a code to let npm script do a mesh build
	if (Object.keys(schemaJson["schemas"]).length == 0 || schemas_hash !== new_hash.digest("hex")) {
		console.log("hash differs");
		fs.writeFileSync(HASHES_FILE, JSON.stringify(hashes), "utf8");
		saveMeshrc(Object.keys(schemaJson["schemas"]), mongoURI);
		process.exit(1);
	} else {
		console.log("hash are THE SAME");
		process.exit(0);
	}
}

(async function() {
	await run();
})();

