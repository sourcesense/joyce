package com.sourcesense.joyce.core.model.uri;

public class JoyceURIValues {

	private final static JoyceURIFactory joyceURIFactory = JoyceURIFactory.getInstance();

	public final static String TAXONOMY_URI = "joyce:secret:test:default:user";
	public final static String SCHEMA_URI = "joyce:content:test:default:user:schema";
	public final static String SOURCE_URI = "joyce:content:test:default:user:src:connect:user-connector:666";
	public final static String DOCUMENT_URI = "joyce:content:test:default:user:doc:666";

	public final static JoyceTaxonomyURI JOYCE_TAXONOMY_URI = joyceURIFactory.createURIOrElseThrow(TAXONOMY_URI, JoyceTaxonomyURI.class);
	public final static JoyceSchemaURI JOYCE_SCHEMA_URI =  joyceURIFactory.createURIOrElseThrow(SCHEMA_URI, JoyceSchemaURI.class);
	public final static JoyceSourceURI JOYCE_SOURCE_URI = joyceURIFactory.createURIOrElseThrow(SOURCE_URI, JoyceSourceURI.class);
	public final static JoyceDocumentURI JOYCE_DOCUMENT_URI = joyceURIFactory.createURIOrElseThrow(DOCUMENT_URI, JoyceDocumentURI.class);
}
