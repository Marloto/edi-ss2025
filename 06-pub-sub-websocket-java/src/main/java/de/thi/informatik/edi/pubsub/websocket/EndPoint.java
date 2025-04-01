package de.thi.informatik.edi.pubsub.websocket;

import java.io.IOException;

import de.thi.informatik.edi.pubsub.manager.Subscriber;
import de.thi.informatik.edi.pubsub.manager.Topic;
import de.thi.informatik.edi.pubsub.manager.TopicHandler;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;


@ServerEndpoint("/{topic}")
public class EndPoint {
	
	// Application Server creates on instance per connection, thus, manage changes
	// in a global way
	private static TopicHandler handler = new TopicHandler();

    private Topic getOrCreate(String name) {
        Topic t = handler.getByName(name);
        if(t == null) {
            t = handler.register(name);
        }
        return t;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("topic") String topic) throws IOException {
    	System.out.println("Connection created for " + topic);

    	// Handle subscribe

        getOrCreate(topic).addSub(new Subscriber() {
            public void receiveMessage(String msg) {
                if(session.isOpen()) { // <- über Parameter der Methode onOpen
                    try {
                        session.getBasicRemote().sendText(msg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @OnMessage
    public void echo(String message, @PathParam("topic") String topic) {
    	// Handle publish
    	getOrCreate(topic).publish(message);
    }

    @OnError
    public void onError(Throwable t) {
        t.printStackTrace();
    }

    @OnClose
    public void onClose(Session session) {
        // NOTE: Subscribe required
    }
}