package de.thi.informatik.edi.pubsub.manager;

import java.util.ArrayList;
import java.util.List;

public class Topic {
    List<Subscriber> subscribers;

    public Topic() {
        this.subscribers = new ArrayList<>();
    }

    public void addSub(Subscriber s) {
        this.subscribers.add(s);
    }

    public void removeSub(Subscriber s) {
        this.subscribers.remove(s);
    }

    public void publish(String msg) {
        for(Subscriber s: this.subscribers) {
            s.receiveMessage(msg);
        }
    }
}
