package com.landg.phoenix.paymentraise.master.batch.configuration;

import com.landg.phoenix.batchexception.BatchUtils;
import com.landg.phoenix.paymentraise.common.configuration.PropertiesConfig;
import lombok.extern.slf4j.Slf4j;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.integration.config.annotation.EnableBatchIntegration;
import org.springframework.batch.integration.partition.RemotePartitioningMasterStepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.jms.dsl.Jms;

import com.landg.phoenix.paymentraise.master.batch.partition.CustomPartitioner;

import javax.sql.DataSource;
import java.time.Instant;

@Slf4j
@Configuration
@EnableBatchProcessing
@EnableBatchIntegration
public class PartitionMaster {

    @Autowired
    private PropertiesConfig propertiesConfig;

    @Autowired
    private BatchUtils batchUtils;

    @Autowired
    private DataSource datasource;

    @Autowired
    private JobRepository jobRepository;

    private JobBuilderFactory jobBuilderFactory;

    private RemotePartitioningMasterStepBuilderFactory masterStepBuilderFactory;

    public PartitionMaster(JobBuilderFactory jobBuilderFactory,
                           RemotePartitioningMasterStepBuilderFactory masterStepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.masterStepBuilderFactory = masterStepBuilderFactory;
    }

    // Configure Remote Job
    @Bean
    public Job jobRemotePartitioning() {
        log.debug("PartitionMaster : jobRemotePartitioning");

        return this.jobBuilderFactory
                .get("jobRemotePartitioning"+Instant.now().getEpochSecond())
                .repository(jobRepository)
                .incrementer(new RunIdIncrementer())
                .flow(paymentRaiseMasterStep())
                .end()
                .build();
    }

    /*
     * Configure outbound flow (requests going to workers)
     */
    @Bean
    public DirectChannel masterPaymentRaiseRequests() {
        return new DirectChannel();
    }

    @Bean
    public IntegrationFlow outboundJmsRequests(ActiveMQConnectionFactory connectionFactory) {
        log.debug("PartitionMaster : outboundJmsRequests");
        return IntegrationFlows
                .from(masterPaymentRaiseRequests())
                .handle(Jms.outboundAdapter(connectionFactory)
                .destination(propertiesConfig.getMasterToWorkerQueueName()))
                .get();
    }

    @Bean
    public Step paymentRaiseMasterStep() {
        log.debug("PartitionMaster : paymentRaiseMasterStep");
        CustomPartitioner customPartitioner = new CustomPartitioner(datasource, propertiesConfig.getTimeTravelDate(), propertiesConfig.getLeadTime());
        return this.masterStepBuilderFactory
                .get("paymentRaiseMasterStep")
                .partitioner("paymentRaiseWorkerStep", customPartitioner)
                .gridSize(propertiesConfig.getGrid())
                .outputChannel(masterPaymentRaiseRequests())
                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean
    public TaskExecutor taskExecutor(){
        return new SimpleAsyncTaskExecutor("paymentRaiseWorkerStep");
    }

}