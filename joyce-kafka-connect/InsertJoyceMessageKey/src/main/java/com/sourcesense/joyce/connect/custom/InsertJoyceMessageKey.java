package com.sourcesense.joyce.connect.custom;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.ConnectRecord;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.transforms.Transformation;
import org.apache.kafka.connect.transforms.util.SimpleConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InsertJoyceMessageKey<R extends ConnectRecord<R>> implements Transformation<R> {

	private final static String KEY_SOURCE_UID_FIELD = "sourceUidField";
	private final static String KEY_SOURCE_UID_FIELD_DEFAULT = "id";

	private final static String KEY_SOURCE_URI = "sourceUri";
	private final static String KEY_SOURCE_URI_DEFAULT = "joyce:content:test:default:user:src:connect:user-connector:%s";

	private final static String KEY_URI = "uri";
	private final static String KEY_URI_DEFAULT = "joyce:content:test:default:user:src:connect:user-connector:-1";

	private final static String KEY_ACTION = "action";
	private final static String KEY_ACTION_DEFAULT = "INSERT";

	private final static ConfigDef CONFIG_DEF = computeConfigDef();

	private String sourceUidField;
	private String sourceUri;

	@Override
	public void configure(Map<String, ?> props) {
		SimpleConfig config = new SimpleConfig(CONFIG_DEF, props);
		sourceUidField = config.getString(KEY_SOURCE_UID_FIELD);
		sourceUri = config.getString(KEY_SOURCE_URI);
	}

	@Override
	public R apply(R record) {
		Map<String, Object> updatedKey = new HashMap<>();
		updatedKey.put(KEY_URI, this.computeUri(record));
		updatedKey.put(KEY_ACTION, this.computeAction(record));
		return record.newRecord(record.topic(), record.kafkaPartition(), null, updatedKey, record.valueSchema(), record.value(), record.timestamp());
	}

	@Override
	public ConfigDef config() {
		return CONFIG_DEF;
	}

	@Override
	public void close() {}

	private String computeUri(R record) {
		return Optional.of(record)
				.map(R::value)
				.filter(Struct.class::isInstance)
				.map(Struct.class::cast)
				.map(value -> value.get(sourceUidField))
				.map(Object::toString)
				.map(sourceUid -> sourceUri.replace("[uid]", sourceUid))
				.orElse(KEY_URI_DEFAULT);
	}

	//Todo: Might be retrieved from record in the future
	private String computeAction(R record) {
		 return KEY_ACTION_DEFAULT;
	}

	private static ConfigDef computeConfigDef() {
		return new ConfigDef()
				.define(
						KEY_SOURCE_UID_FIELD, ConfigDef.Type.STRING,
						KEY_SOURCE_UID_FIELD_DEFAULT, ConfigDef.Importance.HIGH,
						"Name of the field of the source that will be used as uid")
				.define(
						KEY_SOURCE_URI, ConfigDef.Type.STRING,
						KEY_SOURCE_URI_DEFAULT, ConfigDef.Importance.HIGH,
						"Source uri missing the uid");
	}
}
