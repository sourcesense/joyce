package com.sourcesense.joyce.core.exception.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomExceptionHandler {

	public void handleException(Exception exception) {
		log.error(exception.getMessage());
		if (log.isDebugEnabled()) {
			log.debug(ExceptionUtils.getStackTrace(exception));
		}
	}

	public void handleNonBlockingException(Exception exception) {
		log.info(exception.getMessage());
		if (log.isDebugEnabled()) {
			log.debug(ExceptionUtils.getStackTrace(exception));
		}
	}

	public void handleElasticsearchStatusException(Throwable rootCause, String key) {
		if (rootCause != null && rootCause.getSuppressed() != null) {
			log.error("Cannot handle message with key: {} cause: {}",
					key, this.computeCause(rootCause.getSuppressed())
			);
		}
	}

	private String computeCause(Throwable[] suppressed) {
		return Arrays.stream(suppressed)
				.filter(Objects::nonNull)
				.map(Throwable::getMessage)
				.collect(Collectors.joining("\n"));
	}
}
