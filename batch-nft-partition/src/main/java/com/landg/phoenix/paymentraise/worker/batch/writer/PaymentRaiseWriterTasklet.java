package com.landg.phoenix.paymentraise.worker.batch.writer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.landg.phoenix.batchexception.BatchUtils;
import com.landg.phoenix.batchexception.models.BatchException;
import com.landg.phoenix.paymentraise.common.constants.Constants;
import com.landg.phoenix.paymentraise.common.constants.SQLConstants;
import com.landg.phoenix.paymentraise.worker.batch.entities.*;
import com.landg.phoenix.paymentraise.worker.batch.producer.Producer;
import com.landg.phoenix.paymentraise.worker.batch.utils.PaymentRaiseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * Premium component Reader
 *
 */

@Slf4j
@Component
@StepScope
public class PaymentRaiseWriterTasklet {

    private DataSource dataSource;

    private Producer producer;

    private BatchUtils batchUtils;

    private JdbcOperations jdbcTemplate;

    private Long generatedKey;

    private PremiumComponent premiumComponent;

    public PaymentRaiseWriterTasklet(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void execute(StepContribution stepContribution, ChunkContext chunkContext, List<DatabaseEntry> databaseEntries) throws Exception {
        log.debug("PaymentRaiseWriterTasklet : execute");

        for (DatabaseEntry databaseEntry : databaseEntries) {
            writePremiumDetail(databaseEntry.getPaymentDetail(), databaseEntry.getPremiumComponent());
            if (this.generatedKey.longValue() > 0 && this.premiumComponent != null) {
                writePremiumComponent();
                writeDiaryEvent(databaseEntry.getDiaryEvent());
            }
        }

        log.debug("PaymentRaiseWriterTasklet : execute : FINISHED");
    }

    private void writePremiumDetail(PaymentDetail paymentDetail, PremiumComponent premiumComponent) throws Exception {
        log.debug("PaymentRaiseWriterTasklet : writePremiumDetail");

        this.generatedKey = Long.valueOf(0);
        this.premiumComponent = null;

        Map<Long,PremiumComponent> returnMap = new HashMap<Long,PremiumComponent>();
        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            Long uid;
            if (paymentDetail != null || premiumComponent != null) {
                String SQL_PAYMENT_DETAIL_INSERT_QUERY = SQLConstants.PAYMENT_DETAIL_INSERT_QUERY;
                jdbcTemplate.update(
                        new PreparedStatementCreator() {
                            public PreparedStatement createPreparedStatement(Connection connection)
                                    throws SQLException {
                                PreparedStatement statement = connection.prepareStatement(SQL_PAYMENT_DETAIL_INSERT_QUERY, new String[] {"payment_detail_uid"});
                                statement.setString(1, paymentDetail.getStatus());
                                statement.setString(2, paymentDetail.getPolicy());
                                statement.setLong(3, paymentDetail.getBankDetailUid());
                                statement.setTimestamp(4, paymentDetail.getCurrentTimeStamp());
                                statement.setBigDecimal(5, paymentDetail.getPaymentAmount());
                                statement.setDate(6, paymentDetail.getOrigCollectionDate());
                                statement.setString(7, paymentDetail.getCurrency());
                                statement.setString(8, paymentDetail.getBacsCode());
                                statement.setString(9, paymentDetail.getPaymentReference());
                                statement.setString(10, paymentDetail.getOrigPaymentSource());
                                statement.setDate(11, paymentDetail.getPremiumDueDate());
                                statement.setDate(12, paymentDetail.getPremiumPaymentDate());
                                statement.setString(13, paymentDetail.getCommissionProcessedIndicator());
                                return statement;
                            }
                        },
                        keyHolder);

                Integer genKey;
                if (keyHolder.getKeys().size() > 1) {
                    uid = (Long) keyHolder.getKeys().get("payment_detail_uid");
                    genKey = uid.intValue();
                } else {
                    uid = (Long) keyHolder.getKey().longValue();
                    genKey = uid.intValue();
                }
                ObjectMapper mapper = new ObjectMapper();
                FinanceRecords record = PaymentRaiseUtils.getFinanceTransactionRecords(paymentDetail, uid);
                log.debug("PaymentRaiseWriterTasklet : writePremiumDetail : Executed paymentDetail writer {}, generated key {}", paymentDetail, genKey);
                // Data to PTT-QUEUE
                String recordJSON = null;
                try {
                    recordJSON = mapper.writeValueAsString(record);
                    this.producer.send(recordJSON);
                    log.debug("PaymentRaiseWriterTasklet : writePremiumDetail : Data send to MQ{}", recordJSON);
                } catch (JsonProcessingException | JMSException e) {
                    log.error(e.getMessage());
                    log.debug("PaymentRaiseWriterTasklet : writePremiumDetail : Data send to MQ is failed{}", recordJSON);
                }
//                premiumComponent.setLastPaymentUid((long) genKey);
                this.generatedKey = new Long(genKey);
                this.premiumComponent = premiumComponent;
            }
        }
        catch(Exception e) {
            /**
             * If the process indicator is false then write to the exception handling table
             */
            BatchException batchException = this.batchUtils.setBatchException(paymentDetail, Constants.BUSINESS_EXCEPTION,
                    null, null, null, 0, Constants.TABLE_BATCH_NAME, Constants.CREATED_USER_ID);
            this.batchUtils.insertBatchException(batchException);
            log.error("PaymentRaiseWriterTasklet : writePremiumDetail : Data updated into table batch exception {}", batchException);
            if (Constants.isRaiseAlertIndicator()) {
                this.batchUtils.raiseAlertForBusinessException();
                Constants.setRaiseAlertIndicator(false);
            }
        }

    }

    private void writePremiumComponent() throws Exception{
        log.debug("PaymentRaiseWriterTasklet : writePremiumComponent ");

        Long lastPaymentUid = this.generatedKey;
        PremiumComponent premiumComponent = this.premiumComponent;
        log.debug("PaymentRaiseWriterTasklet : writePremiumComponent : lastPaymentUid {}", lastPaymentUid);
        try {
            String SQL_PREMIUM_COMPONENT_UPDATE_QUERY = SQLConstants.PREMIUM_COMPONENT_UPDATE_QUERY;

            Object[] premiumComponentParams = new Object[] { premiumComponent.getNextPremiumDueDate(), premiumComponent.getNextPaymentDueDate(), lastPaymentUid, premiumComponent.getLastPaymentUid(), premiumComponent.getStatus()};
            int[] premiumComponentTypes = new int[] { Types.DATE, Types.DATE, Types.INTEGER, Types.INTEGER, Types.VARCHAR };

            jdbcTemplate.update(SQL_PREMIUM_COMPONENT_UPDATE_QUERY, premiumComponentParams, premiumComponentTypes);

        }
        catch(Exception e) {
            /**
             * If the process indicator is false then write to the exception handling table
             */
            BatchException batchException = this.batchUtils.setBatchException(premiumComponent, Constants.BUSINESS_EXCEPTION,
                    null, null, null, 0, Constants.TABLE_BATCH_NAME, Constants.CREATED_USER_ID);
            this.batchUtils.insertBatchException(batchException);
            log.error("PaymentRaiseWriterTasklet : writePremiumComponent : Data updated into table batch exception {}", batchException);
            if (Constants.isRaiseAlertIndicator()) {
                this.batchUtils.raiseAlertForBusinessException();
                Constants.setRaiseAlertIndicator(false);
            }
        }

    }

    private void writeDiaryEvent(DiaryEvent diaryEvent) throws Exception {
        log.debug("PaymentRaiseWriterTasklet : writeDiaryEvent");
        try {

            String SQL_EVENT_UPDATE_QUERY = SQLConstants.DIARY_EVENT_INSERT_QUERY;

            Object[] diaryComponentParams = new Object[] { diaryEvent.getPolicy(), diaryEvent.getDiaryType(), diaryEvent.getDiaryTimestamp(), diaryEvent.getCreatedTimestamp(), diaryEvent.getDiarySource(), diaryEvent.getDiaryData(), diaryEvent.getStatus()};
            int[] diaryComponentTypes = new int[] { Types.VARCHAR, Types.VARCHAR, Types.TIMESTAMP, Types.TIMESTAMP, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR };

            jdbcTemplate.update(SQL_EVENT_UPDATE_QUERY, diaryComponentParams, diaryComponentTypes);
        }
        catch(Exception e) {
            /**
             * If the process indicator is false then write to the exception handling table
             */
            BatchException batchException = this.batchUtils.setBatchException(diaryEvent, Constants.BUSINESS_EXCEPTION,
                    null, null, null, 0, Constants.TABLE_BATCH_NAME, Constants.CREATED_USER_ID);
            this.batchUtils.insertBatchException(batchException);
            log.debug("PaymentRaiseWriterTasklet : writeDiaryEvent : Data updated into table batch exception {}", batchException);
            if (Constants.isRaiseAlertIndicator()) {
                this.batchUtils.raiseAlertForBusinessException();
                Constants.setRaiseAlertIndicator(false);
            }
        }

    }

    public void setProducer(Producer producer) {
        this.producer = producer;
    }

    public void setBatchUtils(BatchUtils batchUtils) {
        this.batchUtils = batchUtils;
    }

}