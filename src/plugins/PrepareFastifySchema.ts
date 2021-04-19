import { CustomeSchemaParser } from "./CustomSchemaParser";

const globaleQueryStringPagination = {
  page: { type: "integer" },
  size: { type: "integer" },
};

export function SingleEntitySchema(entitySchema: CustomeSchemaParser): object {
  return {
    schema: {
      params: {
        type: "object",
        properties: {
          id: { type: "string" },
        },
      },
      response: {
        200: {
          type: "object",
          properties: entitySchema.getSchemaProperties(),
        },
      },
    },
  };
}

export function MultipleEntitySchema(
  entitySchema: CustomeSchemaParser
): object {
  return {
    schema: {
      querystring: globaleQueryStringPagination,
      response: {
        200: {
          type: "array",
          items: {
            type: "object",
            properties: entitySchema.getSchemaProperties(),
          },
        },
      },
    },
  };
}
