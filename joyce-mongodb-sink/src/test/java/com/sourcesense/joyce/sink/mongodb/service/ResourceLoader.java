package com.sourcesense.joyce.sink.mongodb.service;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public interface ResourceLoader {

	default Path loadResource(String name) throws URISyntaxException {
		URL res = this.getClass().getClassLoader().getResource(name);
		return Paths.get(res.toURI());
	}
}
