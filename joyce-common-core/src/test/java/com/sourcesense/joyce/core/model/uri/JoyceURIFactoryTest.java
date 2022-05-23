package com.sourcesense.joyce.core.model.uri;

import com.sourcesense.joyce.core.enumeration.uri.JoyceURIChannel;
import com.sourcesense.joyce.core.enumeration.uri.JoyceURIContentType;
import com.sourcesense.joyce.core.enumeration.uri.JoyceURIKind;
import com.sourcesense.joyce.core.exception.InvalidJoyceURIException;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

public class JoyceURIFactoryTest {

	private static final String TEST_DOMAIN = "test";
	private static final String TEST_PRODUCT = "default";
	private static final String TEST_NAME = "user";
	private static final String TEST_CHANNEL = JoyceURIChannel.CONNECT;
	private static final String TEST_ORIGIN = "test-connector";
	private static final String TEST_UID = "666";
	private static final String TEST_FULL_NAME = "test:default:user";
	private static final String TEST_COLLECTION = "test-default-user";
	private static final String TEST_SCHEMA_URI = "joyce:content:test:default:user:schema";

	private final JoyceURIFactory joyceURIFactory = JoyceURIFactory.getInstance();

	@Test
	public void shouldCreateOnlyOneInstance() {
		JoyceURIFactory joyceURIFactory2 = JoyceURIFactory.getInstance();
		assertSame(joyceURIFactory, joyceURIFactory2);
	}

	@Test
	public void shouldCreateApiURI() throws URISyntaxException {
		String stringURI = "joyce:api:test:default:user";

		JoyceURI joyceURI = joyceURIFactory.createURIOrElseThrow(stringURI);
		this.testJoyceURI(joyceURI, stringURI, JoyceURIKind.API);

		JoyceTaxonomyURI joyceTaxonomyURI = joyceURIFactory.createURIOrElseThrow(stringURI, JoyceTaxonomyURI.class);
		this.testJoyceTaxonomyURI(joyceTaxonomyURI, stringURI, JoyceURIKind.API);
	}

	@Test
	public void shouldCreateSecretURI() throws URISyntaxException {
		String stringURI = "joyce:secret:test:default:user";

		JoyceURI joyceURI = joyceURIFactory.createURIOrElseThrow(stringURI);
		this.testJoyceURI(joyceURI, stringURI, JoyceURIKind.SECRET);

		JoyceTaxonomyURI joyceTaxonomyURI = joyceURIFactory.createURIOrElseThrow(stringURI, JoyceTaxonomyURI.class);
		this.testJoyceTaxonomyURI(joyceTaxonomyURI, stringURI, JoyceURIKind.SECRET);
	}

	@Test
	public void shouldCreateSchemaURI() throws URISyntaxException {
		String stringURI = TEST_SCHEMA_URI;

		JoyceURI joyceURI = joyceURIFactory.createURIOrElseThrow(stringURI);
		this.testJoyceURI(joyceURI, stringURI, JoyceURIKind.CONTENT);

		JoyceTaxonomyURI joyceTaxonomyURI = joyceURIFactory.createURIOrElseThrow(stringURI, JoyceTaxonomyURI.class);
		this.testJoyceTaxonomyURI(joyceTaxonomyURI, stringURI, JoyceURIKind.CONTENT);

		JoyceContentURI joyceContentURI = joyceURIFactory.createURIOrElseThrow(stringURI, JoyceContentURI.class);
		this.testJoyceContentURI(joyceContentURI, stringURI, JoyceURIKind.CONTENT, JoyceURIContentType.SCHEMA);

		JoyceSchemaURI joyceSchemaURI = joyceURIFactory.createURIOrElseThrow(stringURI, JoyceSchemaURI.class);
		this.testJoyceSchemaURI(joyceSchemaURI, stringURI, JoyceURIKind.CONTENT);
	}

	@Test
	public void shouldCreateDocumentURI() throws URISyntaxException {
		String stringURI = "joyce:content:test:default:user:doc:666";

		JoyceURI joyceURI = joyceURIFactory.createURIOrElseThrow(stringURI);
		this.testJoyceURI(joyceURI, stringURI, JoyceURIKind.CONTENT);

		JoyceTaxonomyURI joyceTaxonomyURI = joyceURIFactory.createURIOrElseThrow(stringURI, JoyceTaxonomyURI.class);
		this.testJoyceTaxonomyURI(joyceTaxonomyURI, stringURI, JoyceURIKind.CONTENT);

		JoyceContentURI joyceContentURI = joyceURIFactory.createURIOrElseThrow(stringURI, JoyceContentURI.class);
		this.testJoyceContentURI(joyceContentURI, stringURI, JoyceURIKind.CONTENT, JoyceURIContentType.DOCUMENT);

		JoyceDocumentURI joyceDocumentURI = joyceURIFactory.createURIOrElseThrow(stringURI, JoyceDocumentURI.class);
		this.testJoyceDocumentURI(joyceDocumentURI, stringURI, JoyceURIKind.CONTENT, TEST_UID);
	}

	@Test
	public void shouldCreateSourceURI() throws URISyntaxException {
		String stringURI = "joyce:content:test:default:user:src:connect:test-connector:666";

		JoyceURI joyceURI = joyceURIFactory.createURIOrElseThrow(stringURI);
		this.testJoyceURI(joyceURI, stringURI, JoyceURIKind.CONTENT);

		JoyceTaxonomyURI joyceTaxonomyURI = joyceURIFactory.createURIOrElseThrow(stringURI, JoyceTaxonomyURI.class);
		this.testJoyceTaxonomyURI(joyceTaxonomyURI, stringURI, JoyceURIKind.CONTENT);

		JoyceContentURI joyceContentURI = joyceURIFactory.createURIOrElseThrow(stringURI, JoyceContentURI.class);
		this.testJoyceContentURI(joyceContentURI, stringURI, JoyceURIKind.CONTENT, JoyceURIContentType.SOURCE);

		JoyceSourceURI joyceSourceURI = joyceURIFactory.createURIOrElseThrow(stringURI, JoyceSourceURI.class);
		this.testJoyceSourceURI(joyceSourceURI, stringURI, JoyceURIKind.CONTENT, TEST_CHANNEL, TEST_ORIGIN, TEST_UID);
	}

	@Test
	public void shouldThrowIfUriInvalidURI() {
		this.testThrowInvalidJoyceURIException("joyce-uri");
	}

	@Test
	public void shouldThrowIfMissingKind()  {
		this.testThrowInvalidJoyceURIException("joyce");
		this.testThrowInvalidJoyceURIException("joyce:");
	}

	@Test
	public void shouldThrowIfWrongURISchema() {
		this.testThrowInvalidJoyceURIException("juice:secret:test:default:user");
	}

	@Test
	public void shouldThrowIfInvalidKind()  {
		this.testThrowInvalidJoyceURIException("joyce:kind");
	}

	@Test
	public void shouldThrowIfMissingName()  {
		this.testThrowInvalidJoyceURIException("joyce:secret");
		this.testThrowInvalidJoyceURIException("joyce:secret:");
	}


	@Test
	public void shouldThrowIfTaxonomyURIIsMissingDomain()  {
		this.testThrowInvalidJoyceURIException("joyce:secret:user");
		this.testThrowInvalidJoyceURIException("joyce:secret::user");
	}

	@Test
	public void shouldThrowIfTaxonomyURIIsMissingProduct()  {
		this.testThrowInvalidJoyceURIException("joyce:secret:test:user");
		this.testThrowInvalidJoyceURIException("joyce:secret:test::user");
	}

	@Test
	public void shouldThrowIfContentURIIsMissingContentType()  {
		this.testThrowInvalidJoyceURIException("joyce:content:test:default:user");
		this.testThrowInvalidJoyceURIException("joyce:content:test:default:user:");
	}

	@Test
	public void shouldThrowIfContentURIHasInvalidContentType()  {
		this.testThrowInvalidJoyceURIException("joyce:content:test:default:user:content");
	}

	@Test
	public void shouldThrowIfDocumentURIIsMissingUid()  {
		this.testThrowInvalidJoyceURIException("joyce:content:test:default:user:doc");
		this.testThrowInvalidJoyceURIException("joyce:content:test:default:user:doc:");
	}

	@Test
	public void shouldThrowIfSourceURIIsMissingChannel()  {
		this.testThrowInvalidJoyceURIException("joyce:content:test:default:user:src");
		this.testThrowInvalidJoyceURIException("joyce:content:test:default:user:src:");
	}

	@Test
	public void shouldThrowIfSourceURIIsMissingOrigin()  {
		this.testThrowInvalidJoyceURIException("joyce:content:test:default:user:src:connect");
		this.testThrowInvalidJoyceURIException("joyce:content:test:default:user:src:connect:");
	}

	@Test
	public void shouldThrowIfSourceURIIsMissingUid()  {
		this.testThrowInvalidJoyceURIException("joyce:content:test:default:user:src:connect:test-connector");
		this.testThrowInvalidJoyceURIException("joyce:content:test:default:user:src:connect:test-connector:");
	}

	private void testJoyceURI(JoyceURI joyceURI, String stringURI, String kind) throws URISyntaxException {
		assertEquals(kind, joyceURI.getKind());
		assertEquals(TEST_NAME, joyceURI.getName());
		assertEquals(stringURI, joyceURI.toString());
		assertEquals(new URI(stringURI), joyceURI.getUri());
	}

	private void testJoyceTaxonomyURI(JoyceTaxonomyURI joyceURI, String stringURI, String kind) throws URISyntaxException {
		this.testJoyceURI(joyceURI, stringURI, kind);
		assertEquals(TEST_DOMAIN, joyceURI.getDomain());
		assertEquals(TEST_PRODUCT, joyceURI.getProduct());
		assertEquals(TEST_FULL_NAME, joyceURI.getFullName());
	}

	private void testJoyceContentURI(JoyceContentURI joyceURI, String stringURI, String kind, String contentType) throws URISyntaxException {
		this.testJoyceTaxonomyURI(joyceURI, stringURI, kind);
		assertEquals(contentType, joyceURI.getContentType());
		assertEquals(TEST_COLLECTION, joyceURI.getCollection());
		assertEquals(TEST_SCHEMA_URI, joyceURI.getSchemaURI().toString());
	}

	private void testJoyceSchemaURI(JoyceSchemaURI joyceURI, String stringURI, String kind) throws URISyntaxException {
		this.testJoyceContentURI(joyceURI, stringURI, kind, JoyceURIContentType.SCHEMA);
	}

	private void testJoyceDocumentURI(JoyceDocumentURI joyceURI, String stringURI, String kind, String uid) throws URISyntaxException {
		this.testJoyceContentURI(joyceURI, stringURI, kind, JoyceURIContentType.DOCUMENT);
		assertEquals(uid, joyceURI.getUid());
	}

	private void testJoyceSourceURI(JoyceSourceURI joyceURI, String stringURI, String kind, String channel, String origin, String uid) throws URISyntaxException {
		this.testJoyceContentURI(joyceURI, stringURI, kind, JoyceURIContentType.SOURCE);
		assertEquals(channel, joyceURI.getChannel());
		assertEquals(origin, joyceURI.getOrigin());
		assertEquals(uid, joyceURI.getUid());
	}

	private void testThrowInvalidJoyceURIException(String stringURI) {
		assertThrows(
				InvalidJoyceURIException.class,
				() -> joyceURIFactory.createURIOrElseThrow(stringURI)
		);
	}
}
