package de.thi.informatik.edi.flux;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
class Otherthing {
	public void doSomething(Integer i) {
		System.out.println("doSomething " + i);
	}
}
public class LambdaExample {
	public static void main(String[] args) {
		List<Integer> input = Arrays.asList(1, 2, 3, 4, 5);
		Consumer<Integer> example = (el) -> System.out.println(el);
		input.forEach(example);
		input.forEach(el -> System.out.println(el));
		input.forEach(System.out::println);
		Otherthing o = new Otherthing();
		input.forEach(o::doSomething);
		// input.forEach((el) -> o.doSomething(el));

		// Eta-Konvertierung erlaubt es
		// eine Methode anzugeben, an die die parameter als argumente weiter
		// gegeben werden können; die Anforderungen ergeben sich aus der Schnittstelle
		// des Datentyps der forEach-Methode
		// -> diese erwartet eine Methode mit einem parameter vom Typ Integer und
		//    einem Rückgabedatentyp String
		// -> println erwartet einen Object, Integer ist substituierbar
		//    auf Object und println gibt nichts zurück
	}
}
