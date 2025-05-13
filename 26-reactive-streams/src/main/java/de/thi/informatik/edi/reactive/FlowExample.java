package de.thi.informatik.edi.reactive;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Flow;
import java.util.concurrent.Flow.Processor;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FlowExample {

	static class ToStringSubscriber<T> implements Subscriber<T> {

		private Subscription subscription;

		public void onSubscribe(Subscription subscription) {
			this.subscription = subscription;
			this.subscription.request(1);
		}

		public void onNext(T item) {
			System.out.println(item);
			this.subscription.request(1);
		}

		public void onError(Throwable throwable) {

		}

		public void onComplete() {}
	}

	// vgl. typische "map"-Funktion, z.B. [1,2,3].map(el -> "Result: " + el);
	static class TransformProcessor<T, R> extends SubmissionPublisher<R> implements Processor<T, R> {
		private final Function<T,R> func;
		private Subscription subscription;

		public TransformProcessor(Function<T, R> func) {
			this.func = func;
		}

		public void onSubscribe(Subscription subscription) {
			this.subscription = subscription;
			this.subscription.request(1);
		}

		public void onNext(T item) {
			// was müsste hier passieren?
			R val = this.func.apply(item);
			this.submit(val);
			this.subscription.request(1);
		}

		public void onError(Throwable throwable) {

		}

		public void onComplete() {}
	}

	// [1,2,3].filter(el => el >= 2); ergibt [2,3]
	static class FilterProcessor extends SubmissionPublisher<Integer> implements Processor<Integer, Integer> {
		private Subscription subscription;
		public void onSubscribe(Subscription subscription) {
			this.subscription = subscription;
			this.subscription.request(1);
		}

		public void onNext(Integer item) {
			// was müsste hier passieren?
			if(item >= 2) {
				this.submit(item);
			}
			this.subscription.request(1);
		}

		public void onError(Throwable throwable) {

		}

		public void onComplete() {}
	}

	public static void main(String[] args) throws Exception {
		// Publisher 1 erzeugt Strings
		SubmissionPublisher<String> publisher = new SubmissionPublisher<String>();

		TransformProcessor<String, Integer> processor = new TransformProcessor<>(Integer::valueOf); // wie kann dieser korrekt verwendet werden?
		publisher.subscribe(processor);

		FilterProcessor processor2 = new FilterProcessor(); // wie kann dieser korrekt verwendet werden?
		processor.subscribe(processor2);

		ToStringSubscriber<Integer> subscriber = new ToStringSubscriber<>();
		processor2.subscribe(subscriber);

		//List.of("1", "21", "2", "4", "3", "42").forEach((el) -> publisher.submit(el));

		Stream.iterate(1, i -> i + 1)
				.limit(1_000_000)
				.map(String::valueOf)
				.forEach((el) -> publisher.submit(el));

		publisher.close();

		//do {
		//	Thread.sleep(1000);
		//} while(publisher.estimateMaximumLag() > 0);
	}
}





