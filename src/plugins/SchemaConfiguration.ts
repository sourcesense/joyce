import createFetch from "@vercel/fetch";
import { ResponsableSchema, SchemaResources } from "../types";

export class SchemaConfiguration {
  readonly sources = [];
  constructor({ schemas }: { schemas: SchemaResources }) {
    Object.keys(schemas).map((label) => {
      this.sources.push({
        label,
        ...schemas[label],
      });
    });
  }
  requestSchemas(): Promise<ResponsableSchema>[] | [] {
    const fetch = createFetch();

    return this.sources.map((resource) => {
      const finalFetchUrl =
        resource.version !== "latest" && resource.version
          ? `${resource.source}/version/${resource.version}`
          : resource.source;
      console.log(finalFetchUrl);
      return fetch(resource.source)
        .then((r) => {
          if (!r.ok) {
            throw r;
          }
          return r.json();
        })
        .then((j) => {
          console.log("-", resource.label, "schema Found");
          return {
            ...resource,
            schema: j.schema,
          };
        })
        .catch((e) => {
          const { statusText } = e;
          console.log("*", resource.label, "schema", statusText);
          return {};
        });
    });
  }
}
