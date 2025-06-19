package de.thi.informatik.edi.stream;

import java.time.Duration;
import java.util.UUID;

import de.thi.informatik.edi.stream.messages.PaymentMessage;
import de.thi.informatik.edi.stream.messages.ShoppingOrderMessage;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.kstream.Suppressed.BufferConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.thi.informatik.edi.stream.messages.ArticleAddedToCartMessage;
import de.thi.informatik.edi.stream.messages.CartMessage;

public class ShoppingTopology {
  private static final Logger logger = LoggerFactory.getLogger(ShoppingTopology.class);

  public static Topology build() {
    StreamsBuilder builder = new StreamsBuilder();

    // Wie könnten wir zählen, wieviele gezahlte Bestellungen vorliegen?
    // -> payment, Generator.PAYMENT_TOPIC
    // -> Daten-Beispiel:
    //   {"id":"6ae75052-e39f-474d-a722-c701b4958e68","orderRef":"29a66b85-2f99-4c98-8726-55193b647078",
    //    "status":"PAYABLE","statusBefore":"PAYABLE"}
    //   {"id":"bd430b57-42fe-414e-a877-a5ff7647872f","orderRef":"fe2bf97b-127a-43f3-91b9-1adaaf0b23a6",
    //    "status":"PAYED","statusBefore":"PAYABLE"}

    // Ansätze: etwas wie reduce mit hochzählen, in Kafka-Streams ist es aggregate
    // Status filtern -> PAYED

    KStream<UUID, PaymentMessage> stream = builder.stream(Generator.PAYMENT_TOPIC,
      Consumed.with(Serdes.UUID(), JsonSerdes.paymentMessage()));

    // Count Payments (Overall)
    stream.filter((key, value) -> "PAYED".equals(value.getStatus()))
      .groupBy((key, value) -> "all-payed", Grouped.with(Serdes.String(), JsonSerdes.paymentMessage())) // <- schlecht skalierend! besser zwischensummen
      //.aggregate(() -> 0, (key, value, aggregate) -> aggregate + 1) // zzgl. Serdes-Config, hier wäre dann string + long
      .count() // erzeugt eine tabelle mit den string key (all-payed) und dem long wert für die Zählung
      .toStream() // wandelt die tabelle in einen Stream um, jetzt werden pro eintrag in der tabelle und jede änderung der einträge
      // ereignisse emittiert
      .print(Printed.toSysOut());

    // Count Payments (in 60s)
    // Erweiterung um Window (vgl. windowedBy)
    stream.filter((key, value) -> "PAYED".equals(value.getStatus()))
      .groupBy((key, value) -> "all-payed", Grouped.with(Serdes.String(), JsonSerdes.paymentMessage()))
      .windowedBy(TimeWindows.of(Duration.ofMinutes(1)))
      .count()
      .toStream()
      .print(Printed.toSysOut());

    KStream<UUID, PaymentMessage> payed = builder.stream(Generator.PAYMENT_TOPIC,
      Consumed.with(Serdes.UUID(), JsonSerdes.paymentMessage()))
      .filter((key, value) -> "PAYED".equals(value.getStatus()))

      // Wie hat man Informationen aus KStreams auf dem selben Knoten/Prozess bekommen, damit diese ausgewertet werden können?
      // -> noch eine Idee was selectKey gemacht hat?
      // -> wir müssen bei joins darauf achten, dass die elemente die gejoined werden den selben schlüssel und die
      //    selbe menge an partitionen nutzen
      .selectKey((key, value) -> value.getOrderRef());

    // Beispiel-Daten:
    // [KSTREAM-SOURCE-0000000021]: 682fb249-9013-4e0f-9966-3045f5f664ed, ShoppingOrderMessage [id=bae7b055-a6ae-4711-830b-08efe3210a94,
    // firstName=Erna, lastName=Musterfrau, street=Somewhere 123, zipCode=12345, city=Exampletown, status=CREATED, price=3.99,
    // items=[ShoppingOrderItemMessage [article=433fe831-3401-4e28-b550-4b6efa425431, name=1kg Äpfel, price=3.99, count=1]]]
    KStream<UUID, ShoppingOrderMessage> orders = builder.stream(Generator.ORDER_TOPIC,
      Consumed.with(Serdes.UUID(), JsonSerdes.shoppingOrderMessage()));

    orders.join(payed, (a, b) -> a.getPrice(), JoinWindows.of(Duration.ofMinutes(1)),
      StreamJoined.with(Serdes.UUID(), JsonSerdes.shoppingOrderMessage(),JsonSerdes.paymentMessage()))
      .groupBy((key, value) -> "all-buyed",
        Grouped.with(Serdes.String(), Serdes.Double()))
      .aggregate(() -> 0.0, (key, value, aggregate) -> aggregate + value,
        Materialized.with(Serdes.String(), Serdes.Double()))
      .toStream()
      .print(Printed.toSysOut());

    // Umsatz von bestimmten Produkten?
    // -> mit flatMap Streams erzeugen für Einzelelemente
    orders.filter((key, value) -> "PAYED".equals(value.getStatus()))
        .flatMap((key, value) -> {
          // Java Stream-API für Liste in KeyValue umwandeln
          return value.getItems().stream()
            .map(el -> KeyValue.pair(el.getArticle(), el.getCount() * el.getPrice()))
            .toList();
          // List<KeyValue<UUID, Double>> list = new ArrayList<>();
          // for(ShoppingOrderItemMessage el : value.getItems()) {
          //   list.add(KeyValue.pair(el.getArticle(), el.getCount() * el.getPrice()))
          // }
        })
      .groupBy((key, value) -> key, Grouped.with(Serdes.UUID(), Serdes.Double()))
      .aggregate(() -> 0.0, (key, value, aggregate) -> value + aggregate,
        Materialized.with(Serdes.UUID(), Serdes.Double()))
      .toStream()
      .print(Printed.toSysOut());


    orders.print(Printed.toSysOut());



    return builder.build();
  }
}
