package de.thi.informatik.edi.pubsub;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class TopicHandler {
    Map<String, Topic> topics;

    public TopicHandler() {
        this.topics = new HashMap<>();
    }

    public Topic register(String name) {
        Topic topic = new Topic();
        this.topics.put(name, topic);
        return topic;
    }

    public Topic getByName(String name) {
        return this.topics.get(name);
    }

    public static void main(String[] args) {
        TopicHandler handler = new TopicHandler();
        Subscriber a = (msg) -> {
            System.out.println("a: " + msg);
        };

        Subscriber b = (msg) -> {
            System.out.println("b: " + msg);
        };

        Subscriber c = (msg) -> {
            System.out.println("c: " + msg);
        };

        Topic t = handler.register("example");
        t.addSub(a);
        t.addSub(b);

        Topic t2 = handler.register("other");
        t2.addSub(a);
        t2.addSub(c);

        t.publish("Hello, World!");
        t2.publish("Hello, Universe!");
    }
}
