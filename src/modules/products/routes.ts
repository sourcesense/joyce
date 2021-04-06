import { getHealthSchema as schema } from "./schema";
import { ObjectID } from "mongodb";

function sintizedQueryParams(req) {
  const skip = req.query.skip && req.query.skip > 0 ? req.query.skip : 0;
  const limit = req.query.limit && req.query.limit > 0 ? req.query.limit : 10;
  return { skip, limit };
}
export default function productHandler(db) {
  return function productHandler(server, options, next) {
    const enabledCollections = process.env.ENABLED_COLLECTIONS
      ? process.env.ENABLED_COLLECTIONS.split(",")
      : [];
    server.get("/:_name", { schema }, async (req, res) => {
      const name = req.params._name;

      if (enabledCollections.length > 0 && !enabledCollections.includes(name)) {
        return res.status(404).send();
      }
      const { skip, limit } = sintizedQueryParams(req);

      try {
        const collection = db.collection(name);
        const docs = await collection
          .find({})
          .limit(limit)
          .skip(skip)
          .toArray();
        res.status(200).send(docs);
      } catch (errore) {
        res.status(500).send(errore);
      }
    });

    server.get("/:_name/:_id", async (req, res) => {
      const paramId = req.params._id;
      const paramName = req.params._name;
      try {
        const collection = db.collection(paramName);
        const id = new ObjectID(paramId);
        const docs = await collection.find({ _id: id }).toArray();
        res.status(200).send(docs);
      } catch (errore) {
        res.status(500).send(errore);
      }
    });
    next();
  };
}
