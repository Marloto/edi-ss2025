package de.thi.informatik.edi.highscore;

import java.security.Key;
import java.time.Duration;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.kstream.Suppressed.BufferConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.thi.informatik.edi.highscore.model.BodyTemp;
import de.thi.informatik.edi.highscore.model.CombinedVitals;
import de.thi.informatik.edi.highscore.model.Pulse;

public class PatientMonitoringTopology {
	private static final Logger logger = LoggerFactory.getLogger(PatientMonitoringTopology.class);
	public static Topology build() {
		StreamsBuilder builder = new StreamsBuilder();

		// Herausforderung: selbe Frequenz? kann man sie entsprechend einfach kombinieren?
		// -> mittelwerte nutzen, bei bestimmten Ereignissen, wenn man keinen hat, dann den vorherigen?
		// -> geht bei pulse nicht, dort müsste man in einem zeitbezug zählen

		// Wie sieht es mit dem Zeitbezug aus?
		// -> Datenmodell hat einen Zeitbezug (vgl. timestamp)
		// -> Kafka Recorder hat ebenfalls eine Zeit
		// -> Verarbeitungszeitpunkt

		// Warum ist das bei einer Messung des Pulses schwierig?
		// Sind zum Zeitpunkt der Verarbeitung immer genau die entsprechenden -60s vorhanden
		// oder ist das nicht garantierbar?
		// -> Producer.poll liefert einfach alle aufgelaufenen Ereignisse
		// -> wir brauchen eine art "nachlaufzeit" um verspätete Ereignisse zu berücksichtigen

		KStream<String, Pulse> pulses = builder.stream(Configurator.PULSE_EVENTS,
			Consumed.with(Serdes.String(), JsonSerdes.pulse())
				.withTimestampExtractor(new VitalTimestampExtractor()));
		KStream<String, BodyTemp> bodyTemps = builder.stream(Configurator.BODY_TEMP_EVENTS,
			Consumed.with(Serdes.String(), JsonSerdes.bodyTemp())
				.withTimestampExtractor(new VitalTimestampExtractor()));

		// Zählen in einem Fenster
		TimeWindows window = TimeWindows.ofSizeAndGrace(Duration.ofSeconds(60), Duration.ofSeconds(5));
		KStream<Windowed<String>, Long> pulseCount = pulses
			.groupByKey()
			.windowedBy(window)
			.count(Materialized.as(Configurator.PULSE_COUNTS))
			.suppress(Suppressed.untilWindowCloses(BufferConfig.unbounded()))
			.toStream();
		pulseCount.print(Printed.toSysOut());

		// Nächste Schritt: Entscheidung von "zuviel" Puls in ein einer Messung
		KStream<String, Long> highPulse = pulseCount
			.filter((key, value) -> value >= 90)
			.map((key, value) -> KeyValue.pair(key.key(), value));

		highPulse.print(Printed.toSysOut());

		KStream<String, BodyTemp> highTemp = bodyTemps
			.filter((key, value) -> value.getTemperature() >= 38);

		highTemp.print(Printed.toSysOut());

		JoinWindows joinWindows = JoinWindows.ofTimeDifferenceAndGrace(Duration.ofSeconds(60), Duration.ofSeconds(5));
		KStream<String, CombinedVitals> vitals = highPulse.join(highTemp,
			(pulseRate, bodyTemp) -> new CombinedVitals(pulseRate.intValue(), bodyTemp),
			joinWindows,
			StreamJoined.with(Serdes.String(), Serdes.Long(), JsonSerdes.bodyTemp()));
		
		vitals.to(Configurator.ALERTS, Produced.with(Serdes.String(), JsonSerdes.combinedVitals()));
		vitals.print(Printed.toSysOut()); // debugging

		return builder.build();
	}
}
