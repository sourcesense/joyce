FROM confluentinc/cp-kafka-connect-base:6.1.1

RUN confluent-hub install --no-prompt confluentinc/kafka-connect-jdbc:10.1.0
RUN confluent-hub install --no-prompt confluentinc/kafka-connect-jira:latest
RUN confluent-hub install --no-prompt jcustenborder/kafka-connect-spooldir:latest
RUN confluent-hub install --no-prompt castorm/kafka-connect-http:0.8.6
RUN confluent-hub install --no-prompt kaliy/kafka-connect-rss:0.1.0

RUN curl -fSL -o /tmp/plugin.tar.gz \
    https://github.com/RedHatInsights/expandjsonsmt/releases/download/0.0.5/kafka-connect-smt-expandjsonsmt-0.0.5.tar.gz && \
    tar -xzf /tmp/plugin.tar.gz -C $KAFKA_CONNECT_PLUGINS_DIR && \
    rm -f /tmp/plugin.tar.gz;

RUN mkdir -p /usr/share/java/camel && \
  wget https://repo1.maven.org/maven2/org/apache/camel/kafkaconnector/camel-file-kafka-connector/0.7.0/camel-file-kafka-connector-0.7.0-package.tar.gz -O /usr/share/java/camel/camel-file-kafka-connector-0.7.0-package.tar.gz && \
  tar -xvzf /usr/share/java/camel/camel-file-kafka-connector-0.7.0-package.tar.gz --directory /usr/share/java/camel && \
  rm /usr/share/java/camel/camel-file-kafka-connector-0.7.0-package.tar.gz

RUN mkdir -p /usr/share/java/joyce
COPY InsertJoyceMessageKey/target/InsertJoyceMessageKey-*-SNAPSHOT.jar /usr/share/java/joyce/



# ENV CONNECT_PLUGIN_PATH=/usr/share/java/,/usr/share/confluent-hub-components/,/opt/plugins/camel/
ENV CONNECT_BOOTSTRAP_SERVERS=afka:9092
ENV CONNECT_REST_PORT=6682
ENV CONNECT_KEY_CONVERTER=org.apache.kafka.connect.json.JsonConverter
ENV CONNECT_VALUE_CONVERTER=org.apache.kafka.connect.json.JsonConverter
ENV CONNECT_KEY_CONVERTER_SCHEMAS_ENABLE=false
ENV CONNECT_SCHEMA_IGNORE=true
ENV CONNECT_VALUE_CONVERTER_SCHEMAS_ENABLE=false
ENV CONNECT_GROUP_ID=joyce-jdbc-connector
ENV CONNECT_CONFIG_STORAGE_TOPIC=joyce-kafka-connector-config
ENV CONNECT_OFFSET_STORAGE_TOPIC=joyce-kafka-connector-offsets
ENV CONNECT_STATUS_STORAGE_TOPIC=joyce-kafka-connector-status
ENV CONNECT_CONFIG_STORAGE_REPLICATION_FACTOR=1 
ENV CONNECT_OFFSET_STORAGE_REPLICATION_FACTOR=1 
ENV CONNECT_STATUS_STORAGE_REPLICATION_FACTOR=1
ENV CONNECT_REST_ADVERTISED_HOST_NAME=localhost