package de.thi.inf.sesa.mqtt.services;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Service
public class PublisherService {
	@Value("${mqtt.topic:test/rnd}")
	private String topic;

	@Value("${mqtt.broker:tcp://localhost:1883}")
	private String broker;

	@Value("${mqtt.client:ExampleClient}")
	private String clientId;

	private int qos = 2;
	private MqttClient sampleClient;

	@PostConstruct
	public void afterConstruct() {
		MemoryPersistence persistence = new MemoryPersistence();

		try {
			sampleClient = new MqttClient(broker, clientId, persistence);
			MqttConnectOptions connOpts = new MqttConnectOptions();
			connOpts.setCleanSession(true);
			System.out.println("Connecting to broker: " + broker);
			sampleClient.connect(connOpts);
			System.out.println("Connected");
		} catch (MqttException me) {
			System.out.println("reason " + me.getReasonCode());
			System.out.println("msg " + me.getMessage());
			System.out.println("loc " + me.getLocalizedMessage());
			System.out.println("cause " + me.getCause());
			System.out.println("excep " + me);
			me.printStackTrace();
		}
		subscribe();
	}

	private int count = 0;

	public void subscribe() {
		try {
			sampleClient.subscribe(topic, (t, msg) -> {
				count ++;
				System.out.println("Received: " + msg);
			});
		} catch (MqttException e) {
			throw new RuntimeException(e);
		}
	}

	public int getCount() {
		return this.count;
	}

	public void publish(String topic, String content) {
		try {
			MqttMessage message = new MqttMessage(content.getBytes());
			message.setQos(qos);
			sampleClient.publish(topic, message);
			System.out.println("Send: " + content);
		} catch (MqttException me) {
			System.out.println("reason " + me.getReasonCode());
			System.out.println("msg " + me.getMessage());
			System.out.println("loc " + me.getLocalizedMessage());
			System.out.println("cause " + me.getCause());
			System.out.println("excep " + me);
			me.printStackTrace();

		}
	}

	public void publish(String content) {
		publish(this.topic, content);
	}
}