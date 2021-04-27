const kafka = require("kafka-node");
const NILE_API_KAFKA_BOOTSTRAPADDRESS =
  process.env.NILE_API_KAFKA_BOOTSTRAPADDRESS || "kafka:9092";

const HighLevelProducer = kafka.HighLevelProducer;
const client = new kafka.KafkaClient({
  kafkaHost: NILE_API_KAFKA_BOOTSTRAPADDRESS,
  autoConnect: true,
});
const producer = new HighLevelProducer(client);

const KafkaProducerPromise = (logger) => {
  return new Promise((resolve, reject) => {
    producer.on("error", function (err) {
      return reject(err);
    });

    producer.on("ready", function () {
      logger.info(
        `Connesso correttamente a ${NILE_API_KAFKA_BOOTSTRAPADDRESS}`
      );
      return resolve(producer);
    });
  });
};
export default KafkaProducerPromise;
