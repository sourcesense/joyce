package com.sourcesense.joyce.core.model.uri;

import com.sourcesense.joyce.core.enumeration.uri.JoyceURIContentType;
import com.sourcesense.joyce.core.enumeration.uri.JoyceURIKind;
import com.sourcesense.joyce.core.exception.InvalidJoyceURIException;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class JoyceURIFactory {

	protected static JoyceURIFactory instance;

	protected JoyceURIFactory() {
	}

	public static JoyceURIFactory getInstance() {
		return instance = Objects.isNull(instance)
				? new JoyceURIFactory()
				: instance;
	}

	public JoyceURI createURIOrElseThrow(String kind, String name) {
		return this.createURIOrElseThrow(
				String.format("%s:%s:%s", JoyceURI.URI_SCHEMA, kind, name)
		);
	}

	public JoyceTaxonomyURI createTaxonomyURIOrElseThrow(String kind, String domain, String product, String name) {
		return this.createURIOrElseThrow(
				String.format("%s:%s:%s:%s:%s", JoyceURI.URI_SCHEMA, kind, domain, product, name),
				JoyceTaxonomyURI.class
		);
	}

	public JoyceContentURI createContentURIOrElseThrow(String domain, String product, String name, String contentType) {
		return this.createURIOrElseThrow(
				String.format("%s:%s:%s:%s:%s:%s", JoyceURI.URI_SCHEMA, JoyceURIKind.CONTENT, domain, product, name, contentType),
				JoyceContentURI.class
		);
	}

	public JoyceSchemaURI createSchemaURIOrElseThrow(String domain, String product, String name) {
		return this.createURIOrElseThrow(
				String.format("%s:%s:%s:%s:%s:%s", JoyceURI.URI_SCHEMA, JoyceURIKind.CONTENT, domain, product, name, JoyceURIContentType.SCHEMA),
				JoyceSchemaURI.class
		);
	}

	public Optional<JoyceDocumentURI> createDocumentURI(String domain, String product, String name, String uid) {
		try {
			return Optional.ofNullable(
					this.createDocumentURIOrElseThrow(domain, product, name, uid)
			);
		} catch (Exception exception) {
			return Optional.empty();
		}
	}

	public JoyceDocumentURI createDocumentURIOrElseThrow(String domain, String product, String name, String uid) {
		return this.createURIOrElseThrow(
				String.format("%s:%s:%s:%s:%s:%s:%s", JoyceURI.URI_SCHEMA, JoyceURIKind.CONTENT, domain, product, name, JoyceURIContentType.DOCUMENT, uid),
				JoyceDocumentURI.class
		);
	}

	public JoyceSourceURI createSourceURIOrElseThrow(String domain, String product, String name, String channel, String origin, String uid) {
		return this.createURIOrElseThrow(
				String.format("%s:%s:%s:%s:%s:%s:%s:%s:%s", JoyceURI.URI_SCHEMA, JoyceURIKind.CONTENT, domain, product, name, JoyceURIContentType.SOURCE, channel, origin, uid),
				JoyceSourceURI.class
		);
	}

	public <U extends JoyceURI> Optional<U> createURI(String stringURI, Class<U> clazz) {
		try {
			return Optional.ofNullable(
					this.createURIOrElseThrow(stringURI, clazz)
			);
		} catch (Exception exception) {
			return Optional.empty();
		}
	}

	public <U extends JoyceURI> U createURIOrElseThrow(String stringURI, Class<U> clazz) {
		return clazz.cast(
				this.createURIOrElseThrow(stringURI)
		);
	}

	public Optional<JoyceURI> createURI(String stringURI) {
		try {
			return Optional.ofNullable(
					this.createURIOrElseThrow(stringURI)
			);
		} catch (Exception exception) {
			return Optional.empty();
		}
	}

	public JoyceURI createURIOrElseThrow(String stringURI) {
		URI uri = this.computeURI(stringURI);
		List<String> uriParts = this.computeUriParts(uri);
		if (uriParts.size() < 2) {
			this.throwWrongNumberOfPartsException();
		}
		if (!JoyceURI.URI_SCHEMA.equals(uriParts.get(0))) {
			throw new InvalidJoyceURIException("Impossible to create Joyce uri, uri schema is not Joyce.");
		}
		switch (uriParts.get(1)) {
			case JoyceURIKind.API:
			case JoyceURIKind.SECRET:
				return this.computeJoyceTaxonomyURI(uri, uriParts);
			case JoyceURIKind.CONTENT:
				return this.computeJoyceContentURI(uri, uriParts);
			default:
				return this.computeUnhandledJoyceURI(uri, uriParts);
		}
	}

	protected JoyceURI computeUnhandledJoyceURI(URI uri, List<String> uriParts) {
		return this.throwInvalidKindException(uriParts);
	}

	protected JoyceURI computeJoyceURI(URI uri, List<String> uriParts) {
		if (uriParts.size() < 3) {
			this.throwWrongNumberOfPartsException(uriParts);
		}
		return new JoyceURI(uri, uriParts.get(1), uriParts.get(2));
	}

	protected JoyceTaxonomyURI computeJoyceTaxonomyURI(URI uri, List<String> uriParts) {
		if (uriParts.size() < 5) {
			this.throwWrongNumberOfPartsException(uriParts);
		}
		return new JoyceTaxonomyURI(uri, uriParts.get(1), uriParts.get(4), uriParts.get(2), uriParts.get(3));
	}

	protected JoyceContentURI computeJoyceContentURI(URI uri, List<String> uriParts) {
		if (uriParts.size() < 6) {
			this.throwWrongNumberOfPartsException(uriParts);
		}
		switch (uriParts.get(5)) {
			case JoyceURIContentType.SCHEMA:
				return this.computeJoyceSchemaURI(uri, uriParts);
			case JoyceURIContentType.DOCUMENT:
				return this.computeJoyceDocumentURI(uri, uriParts);
			case JoyceURIContentType.SOURCE:
				return this.computeJoyceSourceURI(uri, uriParts);
			default:
				return this.computeUnhandledJoyceContentURI(uri, uriParts);
		}
	}

	protected JoyceContentURI computeUnhandledJoyceContentURI(URI uri, List<String> uriParts) {
		return this.throwInvalidContentTypeException(uriParts);
	}

	protected JoyceSchemaURI computeJoyceSchemaURI(URI uri, List<String> uriParts) {
		return new JoyceSchemaURI(uri, uriParts.get(1), uriParts.get(4), uriParts.get(2), uriParts.get(3), uriParts.get(5));
	}

	protected JoyceDocumentURI computeJoyceDocumentURI(URI uri, List<String> uriParts) {
		if (uriParts.size() < 7) {
			this.throwWrongNumberOfPartsException(uriParts);
		}
		return new JoyceDocumentURI(uri, uriParts.get(1), uriParts.get(4), uriParts.get(2), uriParts.get(3), uriParts.get(5), uriParts.get(6));
	}

	protected JoyceSourceURI computeJoyceSourceURI(URI uri, List<String> uriParts) {
		if (uriParts.size() < 9) {
			this.throwWrongNumberOfPartsException(uriParts);
		}
		return new JoyceSourceURI(uri, uriParts.get(1), uriParts.get(4), uriParts.get(2), uriParts.get(3), uriParts.get(5), uriParts.get(6), uriParts.get(7), uriParts.get(8));
	}

	protected URI computeURI(String stringURI) {
		try {
			return new URI(stringURI);

		} catch (Exception exception) {
			throw new InvalidJoyceURIException(String.format(
					"Impossible to create Joyce uri, %s is not a valid uri,", stringURI
			));
		}
	}

	protected List<String> computeUriParts(URI uri) {
		String[] uriParts = uri.toString().split(":");
		return Arrays.stream(uriParts)
				.filter(Predicate.not(String::isEmpty))
				.collect(Collectors.toList());
	}

	protected void throwWrongNumberOfPartsException() {
		this.throwWrongNumberOfPartsException(null);
	}

	protected void throwWrongNumberOfPartsException(List<String> uriParts) {
		throw new InvalidJoyceURIException(String.format(
				"Impossible to create %s Joyce uri, uri contains an unexpected number of parts.",
				Objects.nonNull(uriParts) ? uriParts.get(1) : StringUtils.EMPTY
		));
	}

	protected JoyceURI throwInvalidKindException(List<String> uriParts) {
		throw new InvalidJoyceURIException(String.format(
				"Impossible to create Joyce uri, invalid kind %s.", uriParts.get(1)
		));
	}

	protected JoyceContentURI throwInvalidContentTypeException(List<String> uriParts) {
		throw new InvalidJoyceURIException(String.format(
				"Impossible to create Joyce content uri, invalid contentType %s.", uriParts.get(5)
		));
	}
}
