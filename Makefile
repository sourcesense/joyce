generate_ts:
	yarn run grpc_tools_node_protoc \
    --js_out=import_style=commonjs,binary:./joyce-api/src/grpc \
    --grpc_out=./joyce-api/src/grpc \
    --plugin=protoc-gen-grpc=./node_modules/.bin/grpc_tools_node_protoc_plugin \
    -I joyce-protobuf/src/main/resources/protobuf \
		joyce-protobuf/src/main/resources/protobuf/**/*.proto

	yarn run grpc_tools_node_protoc \
    --plugin=protoc-gen-ts=./node_modules/.bin/protoc-gen-ts \
    --ts_out=./joyce-api/src/grpc \
    -I joyce-protobuf/src/main/resources/protobuf \
		joyce-protobuf/src/main/resources/protobuf/**/*.proto
