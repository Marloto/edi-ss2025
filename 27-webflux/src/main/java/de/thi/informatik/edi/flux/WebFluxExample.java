package de.thi.informatik.edi.flux;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.stream.Stream;

import org.reactivestreams.Publisher;
import reactor.adapter.JdkFlowAdapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.Many;
import reactor.util.function.Tuples;

public class WebFluxExample<T> {

	public static void main(String[] args) throws IOException {
		Mono<String> mono = Mono.just("Hello");
		Flux<String> other = Flux.just("Hello");

		other.subscribe(); //neben Subscriber an sich, gibt es auch abwandlungen z.B. ein Consumer
		// void doSomething(T t)
		other.subscribe(System.out::println);

		// Erzeugen von einer unendlichen Liste an Ereignissen pro Sekunde
		//Flux.interval(Duration.ofSeconds(1)).subscribe(System.out::println);

		// WebFLux definieren eigene Publisher / Subscriber - es gibt Adapter um zw.
		// den Schnittstellen zu wechseln (Flow zu Flux und umgekehrt)
		//Flux<String> flux = JdkFlowAdapter.flowPublisherToFlux(new SubmissionPublisher<>());
		//flux.subscribe(System.out::println);

		//Selbst gebaute Senke
//		Flux<String> mySource = Flux.<String>create(sink -> {
//			System.out.println("Sink started");
//			sink.next("A");
//			sink.next("B");
//			sink.next("C");
//		});
//
//		mySource.subscribe(System.out::println);
//		mySource.subscribe(System.out::println);

//		Many<Object> many = Sinks.many().multicast().onBackpressureBuffer();
//		Flux<Object> flux2 = many.asFlux();
//		flux2.subscribe(System.out::println);
//		flux2.subscribe(System.out::println);
//
//		many.tryEmitNext("A");
//		many.tryEmitNext("B");
//		many.tryEmitNext("C");

		// Einfaches Beispiel für einen Filter
//		Flux
//				.interval(Duration.ofSeconds(1))
//				.filter(el -> el % 2 == 0)
//				.subscribe(System.out::println);

		// Einfaches Beispiel für nimm letztes Element in 3 Sekunden
//		Flux
//				.interval(Duration.ofSeconds(1))
//				.sample(Duration.ofSeconds(3))
//				.subscribe(System.out::println);

		// Entferne Duplikate
//		Flux.fromArray(new Integer[] {1, 2, 2, 1, 3, 2, 4})
//			.distinct()
//			.subscribe(System.out::println);

		// Count in Stream-API vs. Reactive
		//long count1 = Arrays.asList(1,2,3,4).stream().count(); // Terminal-Element, keine weitere Verarbeitung
		//Mono<Long> count2 = Flux.fromArray(new Integer[]{1, 2, 2, 1, 3, 2, 4}).count();

		// Reduce so ähnlich wie in Stream-API
		//Flux.fromArray(new Integer[]{1, 2, 2, 1, 3, 2, 4})
		//		.reduce((a, b) -> a + b).subscribe(System.out::println);

		// Was passiert bei Mehrfachverwendungen?
//		Stream<Integer> integers = Arrays.asList(1,2,3,4).stream();
//		integers.filter(integer -> integer % 2 == 0).forEach(System.out::println);
//		integers.filter(integer -> integer >= 2).forEach(System.out::println);

		// Flux<Integer> integers = Flux.fromArray(new Integer[]{1,2,3,4});
		// integers.filter(integer -> integer % 2 == 0).subscribe(System.out::println);
		// integers.filter(integer -> integer >= 2).subscribe(System.out::println);

		// Flux<Integer> combined = Flux.concat(
		//		Flux.fromArray(new Integer[] {1, 2, 2, 1, 3, 2, 4}),
		//		Flux.fromArray(new Integer[] {9, 8, 8, 9, 7, 8, 6})
		// );
		// combined.subscribe(System.out::print);
//		Flux<String> combined = Flux.merge(
//				Flux.interval(Duration.ofSeconds(1)).map(el -> "A"),
//				Flux.interval(Duration.ofSeconds(1)).map(el -> "B")
//		);
//		combined.subscribe(System.out::print);

		// Beispiel für das Verhalten von zip und combineLatest
//		Flux<Long> a = Flux.interval(Duration.ofSeconds(1));
//		Flux<Long> b = Flux.interval(Duration.ofSeconds(2));
//		a.map(el -> "a: " + el.toString()).subscribe(System.out::println);
//		b.map(el -> "b: " + el.toString()).subscribe(System.out::println);
//		Flux.zip(a, b).map(el -> "z: " + el.toString())
//				.subscribe(System.out::println);
//		Flux.combineLatest(a, b, (x, y) -> List.of(x, y))
//				.map(el -> "c: " + el.toString()).subscribe(System.out::println);

		// Window / Buffer
//		Flux.interval(Duration.ofSeconds(1))
//				.window(Duration.ofSeconds(3))
//				.subscribe(win -> win
//						.reduce((a, b) -> a + b)
//						.subscribe(System.out::println));

		Flux<Long> flux1 = Flux.interval(Duration.ofSeconds(1));
		Flux<Flux<Long>> window = flux1.window(Duration.ofSeconds(3));
		Flux<Long> reducedWindow = window.flatMap(win -> win.reduce((a, b) -> a + b));
		reducedWindow.subscribe(System.out::println);

		Flux<Double> flux = Flux.fromArray(new Double[] {1.0, 5.0, 2.0, 3.0, 7.0, 2.0, 4.0, 9.0, 3.0});
		Mono<Long> count = flux.count();
		Mono<Double> reduce = flux.reduce((a, b) -> a + b);
		Flux.concat(count, reduce).reduce((a, b) -> b.doubleValue() / a.doubleValue());

		flux.map(el -> Tuples.of(el, 1)).reduce((a, b) ->
				Tuples.of(a.getT1() + b.getT1(), a.getT2() + b.getT2())).map((t) -> t.getT1() / t.getT2());

		Flux.interval(Duration.ofSeconds(3)).buffer(3);

//		Flux<Double> flux2 = Flux.fromArray(new Double[] {1.0, 5.0, 2.0, 3.0, 7.0, 2.0, 4.0, 9.0, 3.0});
//		Flux<List<Double>> buffer = flux2.buffer(3);
//		Flux<Flux<Double>> map = buffer.map(el -> Flux.fromIterable(el));
		// damit eine Verarbeitungskette "arbeitet" ist eine subscription notwendig
		// wie kann z.B. flatMap helfen
//		map.subscribe(fluxFromBuffer -> { //
//			fluxFromBuffer
//					.map(bufferEl -> Tuples.of(bufferEl, 1))
//					.reduce((a, b) ->
//						Tuples.of(a.getT1() + b.getT1(), a.getT2() + b.getT2()))
//					.map((t) -> t.getT1() / t.getT2())
//					.subscribe(System.out::println);
//		});
		// Wichtig: Wenn als Ereignis ein Flux emitiert wird, dann sind diese ebenfalls
		//          z.B. Cold, und benötigen ihre eigenen Subscriptions um aktiv zu
		//          werden
		// flatMap, ermöglicht es aus Ereignissen ein oder mehrere zu erzeugen - ggf. sogar keine, wichtig es muss einen Publisher zurückgeben
//		Flux<Double> flux2 = Flux.fromArray(new Double[] {1.0, 5.0, 2.0, 3.0, 7.0, 2.0, 4.0, 9.0, 3.0});
//		Flux<List<Double>> buffer = flux2.buffer(3);
//		Flux<Flux<Double>> map = buffer.map(el -> Flux.fromIterable(el));
//		map.flatMap(fluxFromBuffer -> {
//			// etwas vom Typ Publisher, also Mono od. Flux
//			return fluxFromBuffer
//					.map(bufferEl -> Tuples.of(bufferEl, 1))
//					.reduce((a, b) ->
//						Tuples.of(a.getT1() + b.getT1(), a.getT2() + b.getT2()))
//					.map((t) -> t.getT1() / t.getT2());
//		}).subscribe(System.out::println);
//
//		Flux<Double> test = Flux.fromArray(new Double[] {1.0, 5.0, 2.0, 3.0, 7.0, 2.0, 4.0, 9.0, 3.0})
//			.buffer(3)
//			.flatMap(listFromBuffer ->
//				Flux.fromIterable(listFromBuffer)
//						.map(bufferEl -> Tuples.of(bufferEl, 1))
//						.reduce((a, b) ->
//								Tuples.of(a.getT1() + b.getT1(), a.getT2() + b.getT2()))
//						.map((t) -> t.getT1() / t.getT2())
//				);

		Hooks.onOperatorDebug();

		Flux.just("Orange", "Red", "Yellow")
				.filter(el -> el != null)
				.map(el -> 
					el.substring(0, 4))
				.subscribe(System.out::println);

		// Prevent stopping of main thread
		System.in.read();
	}

	static class Something {
		private int sum;
		private int count;
	}
}
