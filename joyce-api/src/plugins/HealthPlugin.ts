import { FastifyPluginCallback } from "fastify";

const healthPlugin: FastifyPluginCallback<unknown> = async function (fastify, options, done) {
	fastify.get(
		"/",
		{
			schema: {
				tags: ["health"],
				summary: "health check",
				description: "health check",
				response: {
					200: {
						type: "object",
						properties: {
							status: {
								type: "string",
							},
						},
					},
				},
			},
		},
		(req, res) => {
			res.send({ status: "ok" });
		},
	);

	done();
};

export default healthPlugin;
