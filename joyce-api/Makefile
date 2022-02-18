generate_ts:
	mkdir -p ./generated/grpc

	yarn run grpc_tools_node_protoc \
    --js_out=import_style=commonjs,binary:./generated/grpc \
    --grpc_out=grpc_js:./generated/grpc \
    -I ../joyce-protobuf/src/main/resources/protobuf \
		../joyce-protobuf/src/main/resources/protobuf/**/*.proto

	yarn run grpc_tools_node_protoc \
    --plugin=protoc-gen-ts=./node_modules/.bin/protoc-gen-ts \
    --ts_out=grpc_js:./generated/grpc \
    -I ../joyce-protobuf/src/main/resources/protobuf \
		../joyce-protobuf/src/main/resources/protobuf/**/*.proto



