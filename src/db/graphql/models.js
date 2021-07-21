const { model, Schema } = require("mongoose");
const fs = require("fs");

const SCHEMAS_SOURCE = process.env.SCHEMAS_SOURCE || "assets/schemas.json";

var data = fs.readFileSync(SCHEMAS_SOURCE, {
  encoding: "utf8",
});
var schemaJson = JSON.parse(data);
console.log(schemaJson);

var models = new Object();

Object.keys(schemaJson["schemas"]).forEach(function (key) {
  let data = JSON.parse(
    fs.readFileSync("assets/" + key + ".json", {
      encoding: "utf8",
      flag: "r",
    })
  );

  let schema = new Schema(data.schema.properties);
  models[key] = model(key, schema, data.schema['$metadata']['collection']);
});

module.exports = models;
