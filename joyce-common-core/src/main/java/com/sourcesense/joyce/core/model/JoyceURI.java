/*
 *  Copyright 2021 Sourcesense Spa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sourcesense.joyce.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.sourcesense.joyce.core.mapping.deserializer.JoyceURIDeserializer;
import com.sourcesense.joyce.core.mapping.serializer.JoyceURISerializer;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This uri is a unique identifier of content that get passed around topics.
 * It has a semantic that determines the nature of the content it identifies.
 * <p>
 * joyce://TYPE/SUBTYPE/COLLECTION/ID
 */
@Getter
@EqualsAndHashCode
@JsonDeserialize(using = JoyceURIDeserializer.class)
@JsonSerialize(using = JoyceURISerializer.class)
public class JoyceURI {

	public static final String NAMESPACE_SEPARATOR = ".";
	public static final String NAMESPACE_DEFAULT = "default";

	public enum Type {
		/**
		 * This type of content is the id given by The connectors to content they produce
		 */
		RAW("raw"),

		/**
		 * This is the type that identifies Schema objects
		 */
		SCHEMA("schema"),

		/**
		 * This is content that has been transformed
		 */
		CONTENT("content");

		private final String value;

		Type(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		private static final Map<String, Type> lookup = new HashMap<>();

		static {
			for (Type type : Type.values()) {
				lookup.put(type.getValue(), type);
			}
		}

		public static Optional<Type> get(String type) {
			return Optional.ofNullable(lookup.get(type));
		}

		public static Type getOrElseThrow(String type) {
			return get(type)
					.orElseThrow(() -> new IllegalArgumentException(
							String.format("Invalid type %s", type)
					));
		}
	}

	/**
	 * Marks the secondary type of messages
	 */
	public enum Subtype {
		/**
		 * Content or schema produced by the import phase
		 */
		@JsonProperty("import")
		IMPORT("import"),

		/**
		 * Content or schema produced by the the schema engine (EE only)
		 */
		@JsonProperty("model")
		MODEL("model"),

		/**
		 * Content produced by fixed content inside a schema
		 */
		@JsonProperty("fixed")
		FIXED("fixed"),

		/**
		 * Content produced by rest endpoints
		 */
		@JsonProperty("rest")
		REST("rest"),

		/**
		 * Content produced by other sources
		 */
		@JsonProperty("other")
		OTHER("other");

		private final String value;

		Subtype(String value) {
			this.value = value;
		}

		@JsonValue
		public String getValue() {
			return value;
		}

		private static final Map<String, Subtype> lookup = new HashMap<>();

		static {
			for (Subtype subtype : Subtype.values()) {
				lookup.put(subtype.getValue(), subtype);
			}
		}

		public static Optional<Subtype> get(String subtype) {
			return Optional.ofNullable(lookup.get(subtype));
		}

		public static Subtype getOrElseThrow(String subtype) {
			return get(subtype)
					.orElseThrow(() -> new IllegalArgumentException(
							String.format("Invalid subtype %s", subtype)
					));
		}
	}

	private static final String schema = "joyce";
	private Type type;
	private Subtype subtype;
	private String collection;
	private String namespace;
	private String name;
	private String id;

	private URI uri;

	public static JoyceURI make(Type type, Subtype subtype, String collection, String id) {
		return new JoyceURI(URI.create(String.format("%s://%s/%s/%s/%s", schema, type.getValue(), subtype.getValue(), URLEncoder.encode(collection, StandardCharsets.UTF_8), URLEncoder.encode(id, StandardCharsets.UTF_8)).toLowerCase()));
	}

	public static JoyceURI make(Type type, Subtype subtype, String collection) {
		return new JoyceURI(URI.create(String.format("%s://%s/%s/%s", schema, type.getValue(), subtype.getValue(), URLEncoder.encode(collection, StandardCharsets.UTF_8)).toLowerCase()));
	}

	public static JoyceURI makeNamespaced(Type type, Subtype subtype, String namespace, String collection) {
		return new JoyceURI(URI.create(String.format("%s://%s/%s/%s%s%s", schema, type.getValue(), subtype.getValue(), URLEncoder.encode(namespace, StandardCharsets.UTF_8), NAMESPACE_SEPARATOR, URLEncoder.encode(collection, StandardCharsets.UTF_8)).toLowerCase()));
	}

	/**
	 * Main constructor, throws if something is wrong in the uri composition
	 *
	 * @param uri
	 */
	public JoyceURI(URI uri) {
		List<String> paths = Arrays.stream(uri.getPath().split("/"))
				.filter(Predicate.not(String::isEmpty))
				.collect(Collectors.toList());

		if (paths.size() < 2) {
			throw new IllegalArgumentException(String.format("Missing class and id in URI %s", uri.getPath()));
		}

		this.uri = uri;
		this.type = Type.getOrElseThrow(uri.getHost());
		this.subtype = Subtype.getOrElseThrow(paths.get(0));
		this.collection = paths.get(1);

		String[] namespaced = paths.get(1).split("\\" + NAMESPACE_SEPARATOR);

		this.namespace = namespaced.length > 1 ? this.computeNamespace(namespaced) : NAMESPACE_DEFAULT;
		this.name = namespaced.length > 1 ? namespaced[namespaced.length - 1] : collection;

		if (paths.size() > 2) {
			this.id = paths.get(2);
		}
	}

	private String computeNamespace(String[] namespaced) {
		return Arrays.stream(namespaced)
				.limit(namespaced.length - 1)
				.reduce((v1, v2) -> v1.concat(".").concat(v2))
				.orElse(namespaced[0]);
	}

	/**
	 * Static creator handler, returns an optional doesn't throws
	 *
	 * @param uriString
	 * @return
	 */
	public static Optional<JoyceURI> createURI(String uriString) {
		try {
			URI uri = new URI(uriString);
			if (!uri.getScheme().equals(schema)) {
				return Optional.empty();
			}
			JoyceURI joyceUri = new JoyceURI(uri);

			return Optional.of(joyceUri);
		} catch (Exception e) {
			return Optional.empty();
		}
	}

	@Override
	public String toString() {
		return uri.toString();
	}
}
