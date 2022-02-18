package com.sourcesense.joyce.core.mapper;

import com.google.protobuf.Message;
import com.google.protobuf.Struct;
import com.sourcesense.joyce.core.mapping.mapper.ProtoConverter;
import com.sourcesense.joyce.core.test.TestUtility;
import com.sourcesense.joyce.protobuf.exception.ProtobufParsingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class ProtoConverterTest implements TestUtility {

	private final static String TEST_JSON_PATH = "mapper/protoConverter/01.json";

	private ProtoConverter protoConverter;

	@BeforeEach
	public void init() {
		protoConverter = new ProtoConverter();
	}

	@Test
	public void shouldConvertJsonToProto() throws IOException, URISyntaxException {
		String json = this.computeResourceAsString(TEST_JSON_PATH);
		Struct struct = this.computeStruct(json);

		assertEquals(
				Optional.of(struct),
				protoConverter.jsonToProto(json, Struct.class)
		);

		assertEquals(
				struct,
				protoConverter.jsonToProtoOrElseThrow(json, Struct.class)
		);
	}

	@Test
	public void shouldConvertProtoToJson() throws IOException, URISyntaxException {
		String json = this.computeResourceAsString(TEST_JSON_PATH);
		Struct struct = this.computeStruct(json);

		assertEquals(
				json.trim(),
				protoConverter.protoToJson(struct).get().trim()
		);

		assertEquals(
				json.trim(),
				protoConverter.protoToJsonOrElseThrow(struct).trim()
		);
	}

	@Test
	public void shouldNotConvertNullJsonToProto()  {
		assertEquals(
				protoConverter.jsonToProto(null, Struct.class),
				Optional.empty()
		);

		assertThrows(
				ProtobufParsingException.class,
				() -> protoConverter.jsonToProtoOrElseThrow(null, Struct.class)
		);
	}

	@Test
	public void shouldNotConvertJsonToWrongProto() throws IOException, URISyntaxException {
		String json = this.computeResourceAsString(TEST_JSON_PATH);

		assertEquals(
				protoConverter.jsonToProto(json, Message.class),
				Optional.empty()
		);

		assertThrows(
				ProtobufParsingException.class,
				() -> protoConverter.jsonToProtoOrElseThrow(json, Message.class)
		);
	}

	@Test
	public void shouldNotConvertNullProtoToJson() throws IOException, URISyntaxException {
		assertEquals(
				protoConverter.protoToJson(null),
				Optional.empty()
		);

		assertThrows(
				ProtobufParsingException.class,
				() -> protoConverter.protoToJsonOrElseThrow(null)
		);
	}
}
