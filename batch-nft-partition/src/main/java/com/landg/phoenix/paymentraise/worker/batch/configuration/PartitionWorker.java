package com.landg.phoenix.paymentraise.worker.batch.configuration;

import com.landg.phoenix.batchexception.BatchUtils;
import com.landg.phoenix.paymentraise.common.configuration.PropertiesConfig;
import com.landg.phoenix.paymentraise.worker.batch.entities.DatabaseEntry;
import com.landg.phoenix.paymentraise.worker.batch.entities.PremiumComponent;
import com.landg.phoenix.paymentraise.worker.batch.processor.PaymentRaiseProcessorTasklet;
import com.landg.phoenix.paymentraise.worker.batch.producer.Producer;
import com.landg.phoenix.paymentraise.worker.batch.reader.PaymentRaiseReaderTasklet;
import com.landg.phoenix.paymentraise.worker.batch.service.PaymentDetailService;
import com.landg.phoenix.paymentraise.worker.batch.service.PolicyDetailService;
import com.landg.phoenix.paymentraise.worker.batch.writer.PaymentRaiseWriterTasklet;
import lombok.extern.slf4j.Slf4j;

import org.apache.activemq.ActiveMQConnectionFactory;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.integration.config.annotation.EnableBatchIntegration;
import org.springframework.batch.integration.partition.RemotePartitioningWorkerStepBuilderFactory;

import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.jms.dsl.Jms;

import javax.sql.DataSource;
import java.util.List;

@Slf4j
@Configuration
@EnableBatchProcessing
@EnableBatchIntegration
public class PartitionWorker {

    @Autowired
    private PropertiesConfig propertiesConfig;

    @Autowired
    private DataSource datasource;

    @Autowired
    private JobExplorer jobExplorer;

    @Autowired
    private BatchUtils batchUtils;

    @Autowired
    Producer producer;

    @Autowired
    private PolicyDetailService policyDetailService;

    @Autowired
    private PaymentDetailService paymentDetailService;

    private PaymentRaiseReaderTasklet paymentRaiseReaderTasklet;

    private PaymentRaiseProcessorTasklet paymentRaiseProcessorTasklet;

    private PaymentRaiseWriterTasklet paymentRaiseWriterTasklet;

    private final RemotePartitioningWorkerStepBuilderFactory workerStepBuilderFactory;

    public PartitionWorker(RemotePartitioningWorkerStepBuilderFactory workerStepBuilderFactory) {
        this.workerStepBuilderFactory = workerStepBuilderFactory;
    }

    /*
     * Configure inbound flow (requests coming from the master)
     */
    @Bean
    public DirectChannel consumePaymentRaiseMasterRequests() {
        return new DirectChannel();
    }

    @Bean
    public IntegrationFlow inboundFlow(ActiveMQConnectionFactory connectionFactory) {
        log.debug("PartitionWorker : inboundFlow");
        return IntegrationFlows
                .from(Jms.messageDrivenChannelAdapter(connectionFactory)
                .destination(propertiesConfig.getMasterToWorkerQueueName()))
                .channel(consumePaymentRaiseMasterRequests())
                .get();
    }

    // Configure Worker Step
    @Bean
    public Step paymentRaiseWorkerStep() {
        log.debug("PartitionWorker : paymentRaiseWorkerStep");

        return this.workerStepBuilderFactory
                .get("paymentRaiseWorkerStep")
                .inputChannel(consumePaymentRaiseMasterRequests())
                .tasklet(taskletProcessing(null, null))
                .build();
    }

    @Bean
    public TaskExecutor taskExecutor(){
        return new SimpleAsyncTaskExecutor("paymentRaiseWorkerStep");
    }

    @Bean
    @StepScope
    public Tasklet taskletProcessing(@Value("#{stepExecutionContext['minValue']}") Long minValue, @Value("#{stepExecutionContext['maxValue']}") Long maxValue) {
        log.debug("PartitionWorker : taskletProcessing :  minValue {} : maxValue {}", minValue, maxValue);
        log.debug("PartitionWorker : taskletProcessing :  propertiesConfig.getTimeTravelDate() {} : propertiesConfig.getLeadTime() {}", propertiesConfig.getTimeTravelDate(), propertiesConfig.getLeadTime());

        return (contribution, chunkContext) -> {
            log.debug("PartitionWorker : taskletProcessing :  LETS READ");
            PaymentRaiseReaderTasklet paymentRaiseReaderTasklet = new PaymentRaiseReaderTasklet(datasource);
            PaymentRaiseProcessorTasklet paymentRaiseProcessorTasklet = new PaymentRaiseProcessorTasklet(datasource, jobExplorer);
            paymentRaiseProcessorTasklet.setGetPolicyDetailService(this.policyDetailService);
            paymentRaiseProcessorTasklet.setPaymentDetailService(this.paymentDetailService);
            paymentRaiseProcessorTasklet.setBatchUtils(this.batchUtils);
            List<PremiumComponent> premiumComponents = paymentRaiseReaderTasklet.execute(contribution, chunkContext, propertiesConfig.getTimeTravelDate(), propertiesConfig.getLeadTime(), minValue, maxValue);
            log.debug("PartitionWorker : taskletProcessing :  LETS PROCESS");
            PaymentRaiseWriterTasklet paymentRaiseWriterTasklet = new PaymentRaiseWriterTasklet(datasource);
            paymentRaiseWriterTasklet.setBatchUtils(this.batchUtils);
            paymentRaiseWriterTasklet.setProducer(this.producer);
            List<DatabaseEntry> databaseEntries = paymentRaiseProcessorTasklet.execute(contribution, chunkContext, premiumComponents, propertiesConfig.getTimeTravelDate(), propertiesConfig.getLeadTime());
            log.debug("PartitionWorker : taskletProcessing :  LETS WRITE");
            paymentRaiseWriterTasklet.execute(contribution, chunkContext,databaseEntries);
            return RepeatStatus.FINISHED;
        };
    }

}