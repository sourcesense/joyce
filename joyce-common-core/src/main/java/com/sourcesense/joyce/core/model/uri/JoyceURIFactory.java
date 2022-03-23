package com.sourcesense.joyce.core.model.uri;

import com.sourcesense.joyce.core.enumeration.uri.JoyceURIContentType;
import com.sourcesense.joyce.core.enumeration.uri.JoyceURIKind;
import com.sourcesense.joyce.core.exception.InvalidJoyceURIException;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class JoyceURIFactory {

	private static JoyceURIFactory instance;

	protected JoyceURIFactory() {
	}

	public static JoyceURIFactory getInstance() {
		return instance = Objects.isNull(instance)
				? new JoyceURIFactory()
				: instance;
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
			throw InvalidJoyceURIException.wrongNumberOfParts();
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
		throw new InvalidJoyceURIException(String.format(
				"Impossible to create Joyce uri, invalid kind %s.", uriParts.get(1)
		));
	}

	protected JoyceURI computeJoyceURI(URI uri, List<String> uriParts) {
		if (uriParts.size() < 3) {
			throw InvalidJoyceURIException.wrongNumberOfParts(uriParts.get(1));
		}
		return new JoyceURI(uri, uriParts.get(1), uriParts.get(2));
	}

	protected JoyceTaxonomyURI computeJoyceTaxonomyURI(URI uri, List<String> uriParts) {
		if (uriParts.size() < 5) {
			throw InvalidJoyceURIException.wrongNumberOfParts(uriParts.get(1));
		}
		return new JoyceTaxonomyURI(uri, uriParts.get(1), uriParts.get(4), uriParts.get(2), uriParts.get(3));
	}

	protected JoyceContentURI computeJoyceContentURI(URI uri, List<String> uriParts) {
		if (uriParts.size() < 6) {
			throw InvalidJoyceURIException.wrongNumberOfParts(uriParts.get(1));
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
		throw new InvalidJoyceURIException(String.format(
				"Impossible to create Joyce content uri, invalid contentType %s.", uriParts.get(5)
		));
	}

	protected JoyceSchemaURI computeJoyceSchemaURI(URI uri, List<String> uriParts) {
		return new JoyceSchemaURI(uri, uriParts.get(1), uriParts.get(4), uriParts.get(2), uriParts.get(3), uriParts.get(5));
	}

	protected JoyceDocumentURI computeJoyceDocumentURI(URI uri, List<String> uriParts) {
		if (uriParts.size() < 7) {
			throw InvalidJoyceURIException.wrongNumberOfParts(uriParts.get(1));
		}
		return new JoyceDocumentURI(uri, uriParts.get(1), uriParts.get(4), uriParts.get(2), uriParts.get(3), uriParts.get(5), uriParts.get(6));
	}

	protected JoyceSourceURI computeJoyceSourceURI(URI uri, List<String> uriParts) {
		if (uriParts.size() < 9) {
			throw InvalidJoyceURIException.wrongNumberOfParts(uriParts.get(1));
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
		String[] uriParts = uri.toString().split(JoyceURI.URI_SEPARATOR);
		return Arrays.stream(uriParts)
				.filter(Predicate.not(String::isEmpty))
				.collect(Collectors.toList());
	}
}
