package de.thi.informatik.edi.kafka;
import java.net.InetAddress;
import java.time.Duration;
import java.util.List;
import java.util.Properties;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExampleConsumer {
	private static Logger logger = LoggerFactory.getLogger(ExampleProducer.class);
	private static boolean running;
	
	public static void main(String[] args) throws Exception {
		final String topic = "test-topic";

		Properties config = new Properties();
		config.put("client.id", InetAddress.getLocalHost().getHostName());
		config.put("bootstrap.servers", "localhost:9092");
		config.put("group.id", "foo");

		// Information notwendig, wie Key und Value in byte[] überführt werden
		config.put("key.deserializer", StringDeserializer.class.getName());
		config.put("value.deserializer", StringDeserializer.class.getName());
		

		// Erzeugt Concumer-API
		try(Consumer<String, String> consumer = new KafkaConsumer<>(config)) {
			consumer.subscribe(List.of(topic));
			running = true;
			new Thread(() -> {
				while(running) {
					ConsumerRecords<String, String> poll = consumer.poll(Duration.ofDays(1));
					poll.forEach(el -> {
						logger.info(el.key() + ": " + el.value() + " (" + el.topic() + ")");
					});
					consumer.commitSync();
				}
			}).start();
			System.in.read();
			running = false;
		}
	}
}