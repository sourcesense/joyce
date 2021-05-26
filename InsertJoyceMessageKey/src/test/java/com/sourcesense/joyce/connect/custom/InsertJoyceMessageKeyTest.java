package com.sourcesense.joyce.connect.custom;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.errors.DataException;
import org.apache.kafka.connect.source.SourceRecord;
import org.junit.After;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class InsertJoyceMessageKeyTest {

    private InsertJoyceMessageKey<SourceRecord> xform = new InsertJoyceMessageKey<>();

    @After
    public void tearDown() throws Exception {
        xform.close();
    }


    @Test
    public void schemalessInsertUuidField() {
        final Map<String, Object> props = new HashMap<>();

        props.put("uid", "asd");
        props.put("schema", "asd-schema");
        props.put("source", "asd-source");

        xform.configure(props);

        final SourceRecord record = new SourceRecord(null, null, "test", 0,
                null, Collections.singletonMap("magic", 42L));

        final SourceRecord transformedRecord = xform.apply(record);
        assertEquals(42L, ((Map) transformedRecord.value()).get("magic"));
        assertEquals("asd-source", ((Map) transformedRecord.key()).get("source"));

    }
}