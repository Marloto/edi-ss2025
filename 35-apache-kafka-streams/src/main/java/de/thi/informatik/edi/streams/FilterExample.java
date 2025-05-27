package de.thi.informatik.edi.streams;

import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;

public class FilterExample {
	public static void main(String[] args) {
		StreamsBuilder builder = new StreamsBuilder();

		// Wenn kein Serdes verwendet wird, kann man über Typkonkretisierung
		// beim Methodenaufruf sagen, wie dies verwendet wird (muss mit default config passen)
		// -> erster Parameter ist Key-Typ
		// -> zweiter Parameter ist Value-Typ
		// -> Void bedeutet "leerer" Key, wird aber als Object bereitgestellt (vom Typ Void)
		builder.<Void, String>stream("hello-world")
				//.map((key, value) -> KeyValue.pair(key, "Hello, " + value))
				.filter((unused, value) -> !value.isBlank())
				// es gibt weitere
				// Randinfo: .filterNot((unused, value) -> value.isBlank())
				.mapValues(value -> "Hello " + value + "!")
				.to("hello-world-answer");
		
		Properties config = new Properties();
		config.put(StreamsConfig.APPLICATION_ID_CONFIG, "dev1");
		config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		
		// Standard Serdes Config
		config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.Void().getClass());
		config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

		KafkaStreams streams = new KafkaStreams(builder.build(), config);
		streams.start();

		Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
	}
}
