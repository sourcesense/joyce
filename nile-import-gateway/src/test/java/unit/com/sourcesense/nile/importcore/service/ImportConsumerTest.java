package unit.com.sourcesense.nile.importcore.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sourcesense.nile.core.enumeration.ImportAction;
import com.sourcesense.nile.core.enumeration.KafkaCustomHeaders;
import com.sourcesense.nile.core.exception.handler.CustomExceptionHandler;
import com.sourcesense.nile.core.service.SchemaService;
import com.sourcesense.nile.importcore.service.ImportConsumer;
import com.sourcesense.nile.importcore.service.ImportService;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ImportConsumerTest {


	@Mock
	private ImportService importService;
	@Mock
	private SchemaService schemaService;
	@Mock
	private CustomExceptionHandler customExceptionHandler;

	private ImportConsumer importConsumer;

	@BeforeEach
	void init() {
		importConsumer = new ImportConsumer(new ObjectMapper(), importService, schemaService, customExceptionHandler);
	}

	@Test
	void testActionInsert() {

		String messageKey = "user";
		ObjectNode message = new ObjectNode(JsonNodeFactory.instance);
		Map<String, String> headers = Map.of(
				KafkaCustomHeaders.MESSAGE_ACTION, ImportAction.INSERT.toString(),
				KafkaCustomHeaders.IMPORT_SCHEMA, "");

		importConsumer.consumeMessage(message, messageKey, headers);

		verify(importService, times(1)).processImport(any(), any(), any());
	}

	@Test
	void testActionDelete() {

		String messageKey = StringUtils.EMPTY;
		ObjectNode message = new ObjectNode(JsonNodeFactory.instance);
		Map<String, String> headers = Map.of(KafkaCustomHeaders.MESSAGE_ACTION, ImportAction.DELETE.toString());

		importConsumer.consumeMessage(message, messageKey, headers);

		verify(importService, times(1)).processImport(any(), any(), any());
	}


}
