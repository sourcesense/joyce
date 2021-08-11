export const getHealthSchema = {
	summary: "health check",
	description: "health check",
	querystring: {
		skip: { type: "integer" },
		limit: { type: "integer" },
	},
};
