package de.thi.informatik.edi.pubsub;

import de.thi.informatik.edi.pubsub.manager.Subscriber;
import de.thi.informatik.edi.pubsub.manager.Topic;
import de.thi.informatik.edi.pubsub.manager.TopicHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/topics")
public class ChannelController {

    private final TopicHandler handler;
    private final Map<Integer, SubscriptionHelper> helpers = new HashMap<>();
    private int counter = 0;

    private class SubscriptionHelper implements Subscriber {
        private Deque<String> messages = new ArrayDeque<>();
        private int id = counter ++;
        public int getId() {
            return id;
        }
        public void receiveMessage(String message) {
            messages.add(message);
        }
    }

    public ChannelController(TopicHandler handler) {
        this.handler = handler;
    }

    public static class ChannelIdentifier {
        private int id;

        public ChannelIdentifier() {
        }

        public ChannelIdentifier(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    // subscription
    @GetMapping("/register/{name}")
    public ChannelIdentifier register(@PathVariable String name) {
        Topic t = getOrCreate(name);
        SubscriptionHelper s = new SubscriptionHelper();
        t.addSub(s);
        helpers.put(s.getId(), s);
        return new ChannelIdentifier(s.getId());
    }

    private Topic getOrCreate(String name) {
        Topic t = handler.getByName(name);
        if(t == null) {
            t = handler.register(name);
        }
        return t;
    }

    // nachrichten verteilen
    @GetMapping("/broadcast/{name}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void broadcast(@PathVariable String name, @RequestParam String message) {
        Topic t = getOrCreate(name);
        t.publish(message);
    }

    // nachrichten abholen (req-resp, muss bei HTTP)
    @GetMapping("/receive/{id}")
    public List<String> receive(@PathVariable String id) {
        SubscriptionHelper helper = helpers.get(Integer.parseInt(id));
        List<String> messages = new ArrayList<>();
        while(helper.messages.size() > 0) {
            messages.add(helper.messages.poll());
        }
        return messages;
    }

    @GetMapping("/subscribe/{name}")
    public SseEmitter subscribe(@PathVariable String name) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE); // Lange Timeout-Zeit

        Topic t = getOrCreate(name);
        Subscriber sseSubscriber = new Subscriber() {
            public void receiveMessage(String message) {
                try {
                    emitter.send(SseEmitter.event().name("message").data(message));
                } catch(IOException e) {
                    emitter.completeWithError(e);
                }
            }
        };
        t.addSub(sseSubscriber);

        // Beim Abbruch der Verbindung den Subscriber entfernen
        emitter.onCompletion(() -> t.removeSub(sseSubscriber));
        emitter.onTimeout(() -> t.removeSub(sseSubscriber));
        emitter.onError(e -> t.removeSub(sseSubscriber));

        // Optional: Sende ein initiales Event, um zu bestätigen, dass die Verbindung steht
        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("Connected to topic: " + name));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }

        return emitter;
    }

}
