package com.sourcesense.joyce.core.service;

import com.sourcesense.joyce.core.model.entity.SchemaEntity;

import java.util.List;
import java.util.Optional;

public interface SchemaClient {

	Optional<SchemaEntity> get(String id);
	Optional<SchemaEntity> get(String domain, String product, String name);
	SchemaEntity getOrElseThrow(String id);
	SchemaEntity getOrElseThrow(String domain, String product, String name);
	List<SchemaEntity> getAll(Boolean rootOnly) ;
	List<SchemaEntity> getAllByDomainAndProduct(String domain, String product, Boolean rootOnly);
	List<SchemaEntity> getAllByReportsIsNotEmpty();
}
