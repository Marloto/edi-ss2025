package de.thi.informatik.edi.sensor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Service
public class DataEventService {
	
	@Value("${mqtt.topic:servers/#}")
	private String topic;



	private MessageBrokerService service;

	public DataEventService(MessageBrokerService service) {
		this.service = service;
	}

	/**
	 servers/990c4410fb4a/cpu-usage 0.01005025125628145
	 servers/990c4410fb4a/cpu-free 0.9899497487437185
	 servers/990c4410fb4a/free-mem 6982.91015625
	 servers/990c4410fb4a/process/usage-mem 56037376
	 servers/990c4410fb4a/process/heap-total 17932288
	 servers/990c4410fb4a/total-mem 7959.4609375
	 servers/990c4410fb4a/freemem-percentage 0.877309432269577
	 **/

	private static final Pattern p = Pattern.compile("servers/(.*?)/(.*)");

	@PostConstruct
	public void init() {
		Flux<Tuple2<String, String>> messages = this.service.getMessages();
//		messages
//				.map(el -> Tuples.of(p.matcher(el.getT1()), el.getT2()))
//				.filter(el -> el.getT1().find())
//				.filter(el -> el.getT1().group(1).indexOf("cpu-usage") >= 0);

		messages.filter(el -> el.getT1().contains("cpu-usage"))
				.map(el -> Tuples.of(el.getT1(), Double.valueOf(el.getT2())))
				.filter(el -> el.getT2() > 0.8);
		// Wie können wir z.B. CPU Werte auf eine Grenzüberschreitung auswerten?
		// -> wir haben ein Tupel aus Topic und Nachricht als String
		// Topics filtern, weil viele andere Werte vorhanden
		// -> filter
		// Überprüfung des Wertes, ob dieser den Schwellwert übersteigt
		// Optional: Nachricht "verarbeiten"
	}

}