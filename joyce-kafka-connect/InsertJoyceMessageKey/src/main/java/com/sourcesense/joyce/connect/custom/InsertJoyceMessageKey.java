package com.sourcesense.joyce.connect.custom;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.ConnectRecord;
import org.apache.kafka.connect.transforms.Transformation;
import org.apache.kafka.connect.transforms.util.SimpleConfig;

import java.util.HashMap;
import java.util.Map;

import static org.apache.kafka.connect.transforms.util.Requirements.requireMap;

public  class InsertJoyceMessageKey<R extends ConnectRecord<R>> implements Transformation<R> {
    private final static String KEY_UID = "uid";
    private final static String KEY_SCHEMA = "schema";
    private final static String KEY_SOURCE = "source";
    public static final String OVERVIEW_DOC =
            "Insert a Joyce Message Key based on configuration";

    public static final ConfigDef CONFIG_DEF = new ConfigDef()
            .define(KEY_UID, ConfigDef.Type.STRING, "id",
                    ConfigDef.Importance.HIGH, "document field name of unique id")
            .define(KEY_SCHEMA, ConfigDef.Type.STRING, "joyce://schema/import/test",
                    ConfigDef.Importance.HIGH, "Schema uri to use with produced content")
            .define(KEY_SOURCE, ConfigDef.Type.STRING, "test-connector-default",
                    ConfigDef.Importance.HIGH, "String identifier of the current connector configuration");;

    private String uid;
    private String schema;
    private String source;

    @Override
    public void configure(Map<String, ?> props) {

        final SimpleConfig config = new SimpleConfig(CONFIG_DEF, props);
        uid = config.getString(KEY_UID);
        schema = config.getString(KEY_SCHEMA);
        source = config.getString(KEY_SOURCE);

    }

    @Override
    public R apply(R record) {
        final Map<String, Object> updatedKey = new HashMap<>();

        updatedKey.put(KEY_UID, uid);
        updatedKey.put(KEY_SCHEMA, schema);
        updatedKey.put(KEY_SOURCE, source);
        return record.newRecord(record.topic(), record.kafkaPartition(), null, updatedKey, record.valueSchema(), record.value(), record.timestamp());
    }



    @Override
    public ConfigDef config() {
        return CONFIG_DEF;
    }

    @Override
    public void close() {

    }


}
