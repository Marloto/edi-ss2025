package de.thi.informatik.edi.shop.checkout.services;

import java.net.InetAddress;
import java.net.UnknownHostException;

import jakarta.annotation.PostConstruct;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.thi.informatik.edi.shop.checkout.services.messages.ShippingMessage;

@Service
public class ShippingMessageConsumerService implements MessageConsumerService.MessageConsumerServiceHandler {

	private static Logger logger = LoggerFactory.getLogger(ShippingMessageConsumerService.class);

	@Value("${kafka.shippingTopic:shipping}")
	private String topic;
	
	private ShoppingOrderService orders;
    private final MessageConsumerService consumer;

    public ShippingMessageConsumerService(@Autowired ShoppingOrderService orders, @Autowired MessageConsumerService consumer) {
		this.orders = orders;
        this.consumer = consumer;
    }

	@PostConstruct
	private void init() {
		this.consumer.register(topic, this);
	}

	@Override
	public void handle(String topic, String key, String value) {
		logger.info("Received message " + value);
		try {
			ShippingMessage message = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).readValue(value, ShippingMessage.class);
			logger.info("Update order " + message.getOrderRef());
			if("SHIPPED".equals(message.getStatus())) {
				this.orders.updateOrderIsShipped(message.getOrderRef());
			} else {
				logger.info("Unknown shipping status " + message.getStatus() + " for order " + message.getOrderRef());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
