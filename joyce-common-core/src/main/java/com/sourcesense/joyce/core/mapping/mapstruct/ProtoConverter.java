package com.sourcesense.joyce.core.mapping.mapstruct;

import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;
import com.sourcesense.joyce.protobuf.exception.ProtobufParsingException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ProtoConverter {

	public <M extends Message> Optional<M> jsonToProto(String json, Class<M> protoClass) {
		try {
			return Optional.ofNullable(
					this.jsonToProtoOrElseThrow(json, protoClass)
			);
		} catch (Exception exception) {
			return Optional.empty();
		}
	}

	public <M extends Message> M jsonToProtoOrElseThrow(String json, Class<M> protoClass) {
		try {
			M.Builder builder = (M.Builder) protoClass.getMethod("newBuilder").invoke(null);
			JsonFormat.parser().ignoringUnknownFields().merge(json, builder);
			return (M) builder.build();

		} catch (Exception exception) {
			throw new ProtobufParsingException(String.format(
					"There was an error while parsing json to proto, error message is: '%s'", exception.getMessage()
			));
		}
	}

	public Optional<String> protoToJson(MessageOrBuilder messageOrBuilder) {
		try {
			return Optional.ofNullable(
					this.protoToJsonOrElseThrow(messageOrBuilder)
			);
		} catch (Exception exception) {
				return Optional.empty();
		}
	}

	public String protoToJsonOrElseThrow(MessageOrBuilder messageOrBuilder) {
		try {
			return JsonFormat.printer().print(messageOrBuilder);

		} catch (Exception exception) {
			throw new ProtobufParsingException(String.format(
					"There was an error while parsing proto to json, error message is: '%s'", exception.getMessage()
			));
		}
	}
}
