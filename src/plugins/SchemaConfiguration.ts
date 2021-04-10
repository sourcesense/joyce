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

    return this.sources.map((resource) =>
      fetch(resource.source)
        .then((r) => r.json())
        .then((j) => {
          return {
            ...resource,
            schema: j.schema,
          };
        })
    );
  }
}
