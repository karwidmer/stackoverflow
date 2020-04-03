package com.landg.phoenix.paymentraise.master;

import javax.annotation.PostConstruct;
import javax.jms.Connection;

import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.task.configuration.EnableTask;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;

/**
 * 
 * Payment raise batch entry point 
 * 
 */

/**
 * @SpringBootApplication - Class path scanning and auto configuration
 * @EnableBatchProcessing - It provides all infrastructures which spring batch needs to run
 */

@Slf4j
@EnableBatchProcessing
@SpringBootApplication
@EnableTask
@Profile("partitionmaster")
@ComponentScan({"com.landg.phoenix.paymentraise.master.batch", "com.landg.phoenix.batchexception", "com.landg.phoenix.paymentraise.common.configuration"})
public class MasterApplication {

	@Autowired
	public ActiveMQConnectionFactory connectionFactory;

	@PostConstruct
	public void init() throws Exception {
		connectionFactory.createConnection();
		final Connection connection = connectionFactory.createConnection();
		connection.start();
	}

	public static void main(String[] args) {
		log.debug("MasterApplication : HERE WE GO WITH SPRING BOOT MASTER");
		SpringApplication.run(MasterApplication.class, args);
	}
}
