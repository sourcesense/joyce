const { model, Schema } = require("mongoose");
var fs = require("fs");

var data = fs.readFileSync(__dirname + "/../../../assets/schemas.json", {
  encoding: "utf8",
});
var schemaJson = JSON.parse(data);

var models = new Object();

Object.keys(schemaJson["schemas"]).forEach(function (key) {
  let data = JSON.parse(
    fs.readFileSync("./assets/" + key + ".json", {
      encoding: "utf8",
      flag: "r",
    })
  );

  let schema = new Schema(data.properties);
  models[key] = model(key, schema);
  console.log(models);
});

module.exports = models;
