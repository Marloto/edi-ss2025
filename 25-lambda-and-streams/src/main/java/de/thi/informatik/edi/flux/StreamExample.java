package de.thi.informatik.edi.flux;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StreamExample {
	public static void main(String[] args) {
		List<String> list = List.of("1", "21", "2", "4", "3", "42");


		list.stream()// Stream-Erzeugung
				.map(el -> Integer.valueOf(el)) // Operationen
				.filter(el -> el > 2) // Operation
				.forEach(System.out::println); // Terminal-Element

		Stream<String> stream1 = list.stream();
		Stream<Integer> stream2 = stream1.map(el -> Integer.valueOf(el));
		Stream<Integer> stream3 = stream2.filter(el -> el > 2);
		stream3.forEach(System.out::println);

		Integer summe = list.stream()// Stream-Erzeugung
				.map(el -> Integer.valueOf(el)) // Operationen
				.filter(el -> el > 2) // Operation
				.reduce(0, (a, el) -> a + el); // <- sind reduce-Operationen bekannt?
		// reduce(initialwert, lambda-ausdruck der zwei elemente bekommt und einen wert zurückgibt)
		// -> a ist was bisher als ergebnis entstanden ist, initial 0, b ist der aktuelle wert
		System.out.println(summe);

		// Wie könnte man den maximalen/minimal wert mit reduce ermitteln?
		Integer max = list.stream()// Stream-Erzeugung
				.map(el -> Integer.valueOf(el)) // Operationen
				.filter(el -> el > 2) // Operation
				.reduce(0, (a, el) -> Math.max(a, el));

		Integer min = list.stream()// Stream-Erzeugung
				.map(el -> Integer.valueOf(el)) // Operationen
				.filter(el -> el > 2) // Operation
				.reduce(0, (a, el) -> Math.min(a, el));

		System.out.println(max);
		System.out.println(min);

		List<Integer> result = list.stream()// Stream-Erzeugung
				.map(el -> Integer.valueOf(el)) // Operationen
				.filter(el -> el > 2)
				.collect(Collectors.toList());



		// Aufgabe: Verwenden Sie die Stream-API, um die Abteilung aller
		// Mitarbeiter die älter als 30 und weniger als 50.000 verdienen zu finden.
		List<Employee> employees = Arrays.asList(
		    new Employee("Alice", 25, "Marketing", 55000.0),
		    new Employee("Bob", 30, "Sales", 60000.0),
		    new Employee("Charlie", 35, "Engineering", 75000.0),
		    new Employee("Dave", 40, "Engineering", 80000.0),
		    new Employee("Emily", 28, "Marketing", 45000.0),
		    new Employee("Frank", 33, "Sales", 65000.0)
		);

		// Variante 1
		employees.stream()
				.filter(el -> el.getAge() > 30)
				.filter(el -> el.getIncome() < 50000)
				.forEach(el -> System.out.println(el.getDepartment()));

		// Variante 1.2:
		employees.stream()
				.filter(el -> el.getAge() > 30)
				.filter(el -> el.getIncome() < 50000)
				.map(el -> el.getDepartment())
				.collect(Collectors.toList());

		// Variante 2: Menge erzeugen
		employees.stream()
				.filter(el -> el.getAge() > 30)
				.filter(el -> el.getIncome() < 50000)
				.map(el -> el.getDepartment())
				.reduce(new HashSet<String>(), (a, b) -> a, );

		// Variante 3
		employees.stream()
				.filter(el -> el.getAge() > 30 && el.getIncome() < 50000)
				.forEach(el -> System.out.println(el.getDepartment()));
	}
}

class Employee {

	private double income;
	private String department;
	private int age;
	private String name;

	public Employee(String name, int age, String department, double income) {
		this.name = name;
		this.age = age;
		this.department = department;
		this.income = income;
	}
	

	public double getIncome() {
		return income;
	}

	public void setIncome(double income) {
		this.income = income;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
