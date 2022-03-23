package com.sourcesense.joyce.core.service;

import com.sourcesense.joyce.core.model.JoyceURI;
import com.sourcesense.joyce.core.model.entity.SchemaEntity;

import java.util.List;
import java.util.Optional;

public interface SchemaClient {

	Optional<SchemaEntity> get(String id);
	Optional<SchemaEntity> get(JoyceURI.Subtype subtype, String namespace, String name);
	SchemaEntity getOrElseThrow(String id);
	SchemaEntity getOrElseThrow(JoyceURI.Subtype subtype, String namespace, String name);
	List<SchemaEntity> getAll(Boolean rootOnly) ;
	List<SchemaEntity> getAllBySubtypeAndNamespace(JoyceURI.Subtype subtype, String namespace,	Boolean rootOnly);
	List<SchemaEntity> getAllByReportsIsNotEmpty();
	List<String> getAllNamespaces();
}
