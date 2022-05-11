package com.sourcesense.joyce.connect.custom;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.junit.After;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class InsertJoyceMessageKeyTest {

	private final static String TEST_SOURCE_UID_FIELD_KEY = "sourceUidField";
	private final static String TEST_SOURCE_UID_FIELD_VALUE = "id";
	private final static String TEST_SOURCE_UID_VALUE = "666";

	private final static String TEST_SOURCE_URI_KEY = "sourceUri";
	private final static String TEST_SOURCE_URI_VALUE = "joyce:content:test:default:user:src:connect:user-connector:[uid]";

	private final static String TEST_URI_KEY = "uri";
	private final static String TEST_URI_VALUE = "joyce:content:test:default:user:src:connect:user-connector:666";

	private final static String TEST_ACTION_KEY = "action";
	private final static String TEST_ACTION_VALUE = "INSERT";

	private final InsertJoyceMessageKey<SourceRecord> xform = new InsertJoyceMessageKey<>();

	@After
	public void tearDown() {
		xform.close();
	}

	@Test
	public void schemalessInsertUuidField() {
		Map<String, Object> props = new HashMap<>();
		props.put(TEST_SOURCE_URI_KEY, TEST_SOURCE_URI_VALUE);
		props.put(TEST_SOURCE_UID_FIELD_KEY, TEST_SOURCE_UID_FIELD_VALUE);

		xform.configure(props);

		Schema schema = SchemaBuilder.struct()
				.field("magic", Schema.INT64_SCHEMA)
				.field(TEST_SOURCE_UID_FIELD_VALUE, Schema.STRING_SCHEMA);

		Struct recordValue = new Struct(schema);
		recordValue.put("magic", 42L);
		recordValue.put(TEST_SOURCE_UID_FIELD_VALUE, TEST_SOURCE_UID_VALUE);

		SourceRecord record = new SourceRecord(null, null, "test", 0, null, recordValue);
		SourceRecord transformedRecord = xform.apply(record);

		assertEquals(42L, ((Struct) transformedRecord.value()).get("magic"));
		assertEquals(TEST_URI_VALUE, ((Map<?, ?>) transformedRecord.key()).get(TEST_URI_KEY));
		assertEquals(TEST_ACTION_VALUE, ((Map<?, ?>) transformedRecord.key()).get(TEST_ACTION_KEY));
	}
}
