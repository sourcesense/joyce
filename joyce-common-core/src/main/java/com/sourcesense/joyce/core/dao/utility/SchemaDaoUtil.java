package com.sourcesense.joyce.core.dao.utility;

import com.sourcesense.joyce.core.model.SchemaEntity;

import java.util.Optional;

public interface SchemaDaoUtil {

	Optional<SchemaEntity> mapFromData(Object data);
}
