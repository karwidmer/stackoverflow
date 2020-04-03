package com.landg.phoenix.paymentraise.worker.batch.producer;

import javax.jms.JMSException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Producer {
	
	@Autowired
	JmsTemplate jmsTemplate;
	
	@Value("${finance.queue.destination}")
	String destinationQueue;
	
	public void send(String msg) throws JMSException {
		log.debug("MQ sent to {}", this.destinationQueue);
		log.debug("MQ sent to msg {}",msg);
		this.jmsTemplate.setSessionTransacted(true);
		this.jmsTemplate.convertAndSend(this.destinationQueue, msg);
	}
}