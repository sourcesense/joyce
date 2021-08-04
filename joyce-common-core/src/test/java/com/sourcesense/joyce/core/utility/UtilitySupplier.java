package com.sourcesense.joyce.core.utility;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public interface UtilitySupplier {

	default ObjectMapper initJsonMapper() {
		return new ObjectMapper()
				.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
				.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
	}
}
