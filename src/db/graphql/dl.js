const fetch = require("node-fetch");
const yaml = require("js-yaml");
var fs = require("fs");

function dl(schemaJson, key) {
  if (
    schemaJson["schemas"][key] !== undefined &&
    schemaJson["schemas"][key]["source"] !== undefined
  ) {
    fetch(schemaJson["schemas"][key]["source"]).then((response) =>
      response.text().then((yml) => {
        const doc = yaml.load(yml);
        fs.writeFileSync(
          "assets/" + key + ".json",
          JSON.stringify(doc, null, 2),
          "utf8"
        );
      })
    );
  }
}

var data = fs.readFileSync(__dirname + "/../../../assets/schemas.json", {
  encoding: "utf8",
});
var schemaJson = JSON.parse(data);

Object.keys(schemaJson["schemas"]).forEach(function (key) {
  dl(schemaJson, key);
});
