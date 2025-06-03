package de.thi.informatik.edi.highscore;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.Joined;
import org.apache.kafka.streams.kstream.KGroupedStream;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;

import de.thi.informatik.edi.highscore.model.Enriched;
import de.thi.informatik.edi.highscore.model.Leaderboard;
import de.thi.informatik.edi.highscore.model.Player;
import de.thi.informatik.edi.highscore.model.Product;
import de.thi.informatik.edi.highscore.model.ScoreEvent;
import de.thi.informatik.edi.highscore.model.ScoreWithPlayer;

public class LeaderboardTopology {
    public static Topology build() {
        StreamsBuilder builder = new StreamsBuilder();

        // Ziel: irgendwann sagt player 1 und spiel 1 -> 999 punkte hat
        // -> Wie oft werden players und products Nachrichten verteilen?
        // -> Nicht so oft, ehr selten
        // -> Vgl. Score-Events: immer dann wenn man neue Punkte erhält
        // Häufig wird man auf Daten treffen, die "konstant" sind, und sich selten ändern (vgl. player / products)
        // vs. Daten die regelmäßig aufkommen (vgl. score-events)
        // Tabellen in Kafka Streams als Möglichkeit Daten als eine Art relationale Datenbank
        // darzustellen -> Keys des Records können zum abrufen genutzt werden, und man erhält
        // den Value des Records. Gibt es ein neues Event/Nachricht für den selben Key
        // aktualisiert sich der Eintrag

        // KTable -> je Partitionierung und Prozesse werden teile der Spieler-Nachrichten als Tabelle in diesem Prozess verfügbar
        KTable<String, Player> players = builder.table("players", Consumed.with(Serdes.String(), JsonSerdes.player()));
        // GlobalKTable -> alle Produkte werden in diesem Prozess verfügbar
        GlobalKTable<String, Product> products = builder.globalTable("products", Consumed.with(Serdes.String(), JsonSerdes.product()));
        // KStream -> je Partitionierung und Prozesse werden teile der Events hier verfügbar
        KStream<String, ScoreEvent> scoreEvents = builder.stream("score-events", Consumed.with(Serdes.String(), JsonSerdes.scoreEvent()))
          .selectKey((key, value) -> value.getPlayerId().toString());

        // scoreEvents.merge <- zu klären am Donnerstag
        // vgl. (scoreEvent, player) -> new ScoreWithPlayer(scoreEvent, player)
        KStream<String, ScoreWithPlayer> scoreWithPlayers = scoreEvents.join(players, ScoreWithPlayer::new, Joined.with(Serdes.String(), JsonSerdes.scoreEvent(), JsonSerdes.player()));// <- was ist das? Eta-Konvertierung

        KStream<String, Enriched> withProducts = scoreWithPlayers.join(
          products,
          (leftKey, scoreWithPlayer) -> String.valueOf(scoreWithPlayer.getScoreEvent().getProductId()),
          Enriched::new); // <- kombiniert die beiden Values

        withProducts
          .groupBy((key, value) -> value.getProductId().toString(), Grouped.with(Serdes.String(), JsonSerdes.enriched()))
            // Für reduce aus Rx gibt es in KafkaStream aggregate;
          .aggregate(Leaderboard::new, (key, value, state) -> state.update(value), Materialized.with(Serdes.String(), JsonSerdes.leaderboard()))
          .toStream()
          .to("leader-boards");

        return builder.build();
    }
}
