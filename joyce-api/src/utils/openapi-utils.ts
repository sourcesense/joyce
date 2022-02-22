export function parseProperties(properties, transformLabel = "") {
	const props = Object.keys(properties);
	const j = props.reduce((r, prop) => {
		if (properties[prop].type === "object") {
			return {
				...r,
				...parseProperties(properties[prop].properties, prop),
			};
		}
		return {
			...r,
			[`${transformLabel ? `${transformLabel}.` : ""}${prop}`]:
				properties[prop],
		};
	}, {});
	return j;
}
