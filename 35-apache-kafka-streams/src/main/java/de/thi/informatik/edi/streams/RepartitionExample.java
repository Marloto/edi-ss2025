package de.thi.informatik.edi.streams;

import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Branched;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Named;

public class RepartitionExample {
	public static void main(String[] args) {
		StreamsBuilder builder = new StreamsBuilder();

		
		Properties config = new Properties();
		config.put(StreamsConfig.APPLICATION_ID_CONFIG, "dev1");
		config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.Void().getClass());
		config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

		// Was war "Depth-First Processing"? Wenn eine Nachricht empfangen wird, wird diese
		// in der vollständigen Datenstromverarbeitung behandelt
		// Es gibt "Sub-Topologien" - man kann diese Depth-First-Verarbeitung
		// bis zum Beginn der Sub-Topologie durchführen, unterbricht diese Sequenz und
		// führt die folgenden Schritte in einem eigenen "Depth-First-Processing-Ansatz" aus.

		// Warum sind "unterbrechungen" der Datenstromverarbeitungsketten in
		// Kafka Streams wichtig bzw. ggf. notwendig?
		// -> Ziel: "repartitionierung"
		// -> "repartion()"
		Map<String, KStream<Void, String>> map = builder.<Void, String>stream("hello-world")
			.split(Named.as("Branch-"))
			.branch((key, value) -> !value.isBlank(), Branched.as("A"))
			.defaultBranch(Branched.as("B"));

		map.get("Branch-B")
			.mapValues(value -> "unknown")
			.merge(map.get("Branch-A"))
			.repartition() // <- startet eine neue Sub-Topologie, unterbricht den "Depth-First" Ansatz
			.mapValues(value -> "Hello " + value + "!")
			.to("hello-world-answer");

		KafkaStreams streams = new KafkaStreams(builder.build(), config);
		streams.start();

		Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
	}
}
