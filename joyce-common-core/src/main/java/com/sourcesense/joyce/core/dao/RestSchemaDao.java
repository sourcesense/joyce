package com.sourcesense.joyce.core.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sourcesense.joyce.core.dto.Schema;
import com.sourcesense.joyce.core.exception.RestException;
import com.sourcesense.joyce.core.exception.handler.CustomExceptionHandler;
import com.sourcesense.joyce.core.mapper.SchemaMapper;
import com.sourcesense.joyce.core.model.JoyceURI;
import com.sourcesense.joyce.core.model.SchemaEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(value = "joyce.schema-service.database", havingValue = "rest")
@Component
public class RestSchemaDao implements SchemaDao {

	@Value("${joyce.schema-service.rest-endpoint}")
	private String restEndpoint;

	private final ObjectMapper mapper;
	private final RestTemplate restTemplate;
	private final SchemaMapper schemaMapper;
	private final CustomExceptionHandler customExceptionHandler;


	@Override
	public Optional<SchemaEntity> get(String id) {
		try {
			return JoyceURI.createURI(id)
					.map(this::getSchemaEndpoint)
					.map(endpoint -> restTemplate.getForEntity(endpoint, Schema.class))
					.filter(response -> HttpStatus.OK.equals(response.getStatusCode()))
					.map(ResponseEntity::getBody)
					.map(Schema::getSchema)
					.map(schema -> mapper.convertValue(schema, SchemaEntity.class));

		} catch (Exception exception) {
			customExceptionHandler.handleNonBlockingException(exception);
			return Optional.empty();
		}
	}

	@Override
	public List<SchemaEntity> getAll() {
		try {
			String endpoint = this.getSchemaEndpoint() + "?fullSchema=true";
			return this.computeFetchedSchemas(endpoint);

		} catch (Exception exception) {
			throw new RestException(exception.getMessage());
		}
	}

	@Override
	public void save(SchemaEntity schemaEntity) {
		try {
			String endpoint = this.getSchemaEndpoint();
			Optional.of(schemaEntity)
					.map(schemaMapper::toDtoSave)
					.map(body -> restTemplate.postForEntity(endpoint, body, JoyceURI.class))
					.filter(response -> HttpStatus.CREATED.equals(response.getStatusCode()))
					.orElseThrow(() -> new RuntimeException(
							String.format("An error happened when calling '%s' to save schema", endpoint))
					);
		} catch (Exception exception) {
			throw new RestException(exception.getMessage());
		}
	}

	@Override
	public void delete(SchemaEntity schemaEntity) {
		try {
			restTemplate.delete(this.getSchemaEndpoint(
					schemaEntity.getMetadata().getSubtype(),
					schemaEntity.getMetadata().getNamespace(),
					schemaEntity.getMetadata().getName())
			);
		} catch (Exception exception) {
			throw new RestException(exception.getMessage());
		}
	}

	@Override
	public List<SchemaEntity> getAllBySubtypeAndNamespace(JoyceURI.Subtype subtype, String namespace) {
		try {
			String endpoint = this.getSchemaEndpoint(subtype, namespace) + "?fullSchema=true";
			return this.computeFetchedSchemas(endpoint);

		} catch (Exception exception) {
			throw new RestException(exception.getMessage());
		}
	}

	@Override
	public List<String> getAllNamespaces() {
		//Todo: must be implemented
		return Collections.emptyList();
	}

	private String getSchemaEndpoint() {
		return String.format("%s/api/schema", restEndpoint);
	}

	private String getSchemaEndpoint(JoyceURI.Subtype subtype, String namespace) {
		return String.format("%s/%s/%s", this.getSchemaEndpoint(), subtype.getValue(), namespace);
	}

	private String getSchemaEndpoint(JoyceURI.Subtype subtype, String namespace, String name) {
		return String.format("%s/%s/%s/%s", this.getSchemaEndpoint(), subtype.getValue(), namespace, name);
	}

	private String getSchemaEndpoint(JoyceURI joyceURI) {
		return String.format(
				"%s/%s/%s/%s",
				this.getSchemaEndpoint(),
				joyceURI.getSubtype().getValue(),
				joyceURI.getNamespace(),
				joyceURI.getName()
		);
	}

	private List<SchemaEntity> computeFetchedSchemas(String endpoint) {

		ParameterizedTypeReference<List<SchemaEntity>> responseType = new ParameterizedTypeReference<>() {};
		return Optional.of(endpoint)
				.map(url -> restTemplate.exchange(url, HttpMethod.GET, null, responseType))
				.filter(response -> HttpStatus.OK.equals(response.getStatusCode()))
				.map(ResponseEntity::getBody)
				.orElseThrow(() -> new RestException(
						String.format("An error happened when calling '%s' to fetch schemas", endpoint))
				);
	}
}
