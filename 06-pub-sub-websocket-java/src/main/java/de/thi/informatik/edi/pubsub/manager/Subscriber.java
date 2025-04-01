package de.thi.informatik.edi.pubsub.manager;

public interface Subscriber {
    void receiveMessage(String message);
}
