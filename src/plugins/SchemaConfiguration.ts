import createFetch from "@vercel/fetch";
import { SchemaMetadata, SchemaProperties } from "./CustomSchemaParser";

export interface SchemaResources {
  [key: string]: Resource;
}
export interface Resource {
  version: string;
  source: string;
}
export interface ResponsableSchema extends Resource {
  label: string;
  schema: { $metadata: SchemaMetadata; properties: SchemaProperties };
}
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