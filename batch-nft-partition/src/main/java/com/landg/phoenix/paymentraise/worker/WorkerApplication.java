package com.landg.phoenix.paymentraise.worker;

import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.task.configuration.EnableTask;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;

import javax.annotation.PostConstruct;
import javax.jms.Connection;

@Slf4j
@EnableBatchProcessing
@SpringBootApplication
@EnableTask
@Profile("partitionworker")
@ComponentScan({"com.landg.phoenix.paymentraise.worker.batch", "com.landg.phoenix.batchexception", "com.landg.phoenix.paymentraise.common.configuration"})
public class WorkerApplication {

    @Autowired
    public ActiveMQConnectionFactory connectionFactory;

    @PostConstruct
    public void init() throws Exception {
        connectionFactory.createConnection();
        final Connection connection = connectionFactory.createConnection();
        connection.start();
    }

    public static void main(String[] args) {
        log.debug("WorkerApplication : HERE WE GO WITH SPRING BOOT WORKER");
        SpringApplication.run(WorkerApplication.class, args);
    }

}