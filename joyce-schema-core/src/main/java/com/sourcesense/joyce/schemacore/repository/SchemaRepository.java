package com.sourcesense.joyce.schemacore.repository;

import com.sourcesense.joyce.core.model.JoyceURI;
import com.sourcesense.joyce.schemacore.model.entity.SchemaDocument;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@ConditionalOnProperty(value = "joyce.data.mongodb.enabled", havingValue = "true")
public interface SchemaRepository extends MongoRepository<SchemaDocument, String> {

	@Query("{'metadata.parent': null}")
	List<SchemaDocument> findAllByMetadata_ParentIsNull();

	List<SchemaDocument> findAllByMetadata_SubtypeAndMetadata_Namespace(JoyceURI.Subtype subtype, String namespace);

	@Query("{'metadata.subtype': ?0, 'metadata.namespace': ?1, 'metadata.parent': null}")
	List<SchemaDocument> findAllByMetadata_SubtypeAndMetadata_NamespaceAndMetadata_ParentIsNull(JoyceURI.Subtype subtype, String namespace);

	@Query("{'metadata.extra.reports': {'$exists': true, '$ne': []}}")
	List<SchemaDocument> findAllByReportsIsNotEmpty();
}
