const yaml = require("js-yaml");
const fs = require("fs");
const _ = require("lodash");

const SCHEMAS_SOURCE = process.env.SCHEMAS_SOURCE || "assets/schemas.json";
const mongoURI = process.env.MONGO_URI || "mongodb://localhost:27017/ingestion";

const models = [];

var data = fs.readFileSync(SCHEMAS_SOURCE, {
	encoding: "utf8",
});
var schemaJson = JSON.parse(data);

Object.keys(schemaJson["schemas"]).forEach(function (key) {
	key = _.upperFirst(_.camelCase(key));
	models.push({
		name: key,
		path: `./src/db/graphql/models.js#${key}`,
	});
});

let ymlRc = {
	sources: [
		{
			name: "Mongoose",
			handler: {
				mongoose: {
					connectionString: mongoURI,
					models: models,
				},
			},
		},
	],
	require: [
		"ts-node/register/transpile-only",
	],
	serve: {
		customServerHandler: "./mesh-server.js",
		// browser: false,
		// method: "GET",
		// endpoint: "/query",
	},
};

fs.writeFileSync(".meshrc.yml", yaml.dump(ymlRc), "utf8");
