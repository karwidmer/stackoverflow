package com.landg.phoenix.paymentraise.worker.batch.processor;

import com.landg.phoenix.batchexception.BatchUtils;
import com.landg.phoenix.dateutils.DateUtils;
import com.landg.phoenix.paymentraise.common.constants.Constants;
import com.landg.phoenix.paymentraise.worker.batch.entities.*;
import com.landg.phoenix.paymentraise.worker.batch.entities.feed.PaymentRaiseWDHFeed;
import com.landg.phoenix.paymentraise.worker.batch.service.PaymentDetailService;
import com.landg.phoenix.paymentraise.worker.batch.service.PolicyDetailService;
import com.landg.phoenix.paymentraise.worker.batch.utils.PaymentRaiseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * Premium component Reader
 *
 */

@Slf4j
@Component
@StepScope
public class PaymentRaiseProcessorTasklet {

    private BatchUtils batchUtils;

    private PolicyDetailService policyDetailService;

    private PaymentDetailService paymentDetailService;

    private JobExplorer jobExplorer;

    private JdbcOperations jdbcTemplate;

    private static final String MONTHLY = "M";

    private static final String ANNUALLY = "A";

    public PaymentRaiseProcessorTasklet(DataSource dataSource, JobExplorer jobExplorer) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.jobExplorer = jobExplorer;
    }

    public List<DatabaseEntry> execute(StepContribution stepContribution, ChunkContext chunkContext, List<PremiumComponent> premiumComponents, String timeTravelDate, int leadTime) throws Exception {
        log.debug("PaymentRaiseProcessorTasklet : execute : premiumComponents.size() : {}", premiumComponents.size());

        List<DatabaseEntry> databaseEntries = new ArrayList<DatabaseEntry>();

        for (PremiumComponent premiumComponent : premiumComponents) {
            DatabaseEntry databaseEntry =  process(premiumComponent, timeTravelDate, leadTime);
            databaseEntries.add(databaseEntry);
        }

        return databaseEntries;
    }

    private DatabaseEntry process(PremiumComponent item, String timeTravelDate, int leadTime) throws Exception {
        log.debug("PaymentRaiseProcessor : process : item.getPolicy() {}", item.getPolicy());

        final DatabaseEntry databaseEntry = new DatabaseEntry();
        String nextPremiumDueDateAfterProcess = null;
        try {
            if (this.paymentDetailService == null) {
                log.error("PaymentRaiseProcessor : process : this.paymentDetailService NOT INJECTED");
            }
            PaymentDetail paymentDetail = this.paymentDetailService.checkExistingPaymentDetail(item);
            Policy policyDetails = this.policyDetailService.policyService(item);

            if (paymentDetail != null && policyDetails != null) {
                log.debug("*** PaymentRaiseProcessor : process : Inside payment raise processor, PremiumComponent: {} ***", item);
                databaseEntry.setLastPaymentUid(item.getLastPaymentUid());
                boolean finalPayment = this.paymentDetailService.checkIfFinalPayment(item, policyDetails);

                /**
                 * Status 01 for ready for collection and 04 for arrears
                 */
                if (PaymentRaiseUtils.isLivePremium(item)) {
                    paymentDetail.setStatus(Constants.PAYMENT_DETAIL_STATUS_READY_FOR_COLLECTION);
                } else if (PaymentRaiseUtils.isAwaitReinstatementPremium(item)) {
                    paymentDetail.setStatus(Constants.PAYMENT_DETAIL_STATUS_ARREARS);
                } else if(PaymentRaiseUtils.isWaiverPremium(item)) {
                    /*
                     * If the waiver of premium has started and premium component status shows as Waiver then change the Payment Detail status to Waiver for Collection - 11
                     */
                    paymentDetail.setStatus(Constants.PAYMENT_DETAIL_STATUS_WAIVER_FOR_COLLECTION);
                }

                paymentDetail.setPolicy(item.getPolicy());
                paymentDetail.setBankDetailUid(item.getBankDetailUid());
                paymentDetail
                        .setCurrentTimeStamp(DateUtils.getTimetravelTimestamp(timeTravelDate));
                paymentDetail.setPaymentAmount(item.getPremiumSum());
                paymentDetail.setOrigCollectionDate(item.getNextPaymentDueDate());
                paymentDetail.setCurrency(Constants.CURRENCY);
                /* paymentDetail.setBacsCode(Constants.BACS_CODE_DD); */
                paymentDetail.setBankDetailUid(item.getBankDetailUid());
                paymentDetail.setPaymentReference(PaymentRaiseUtils.getPaymentReferanceNumber(item));
                paymentDetail.setOrigPaymentSource(Constants.PAYMENT_SOURCE_DIRECT_DEBIT);
                paymentDetail.setCommissionProcessedIndicator(Constants.COMMISSION_PROCESSED_IND);

                /**
                 * Calculate and set premium due date (PREM_DUE_DATE) field in payment detail
                 * table. Target collection date is input to calculate premium due date
                 * (PREM_DUE_DATE).
                 */
                paymentDetail.setPremiumDueDate(item.getNextPremiumDueDate());

                log.debug("PaymentRaiseProcessor : process : Processed premium due (PREM_DUE_DATE) date {}", item.getNextPremiumDueDate());

                /**
                 * Calculate and set premium payment date (PREM_PAYMENT_DATE) in payment details
                 * table. Processed next premium due date ( nextPremiumDueDateAfterProcess ) is
                 * input to calculate premium payment date (PREM_PAYMENT_DATE)
                 */
                String premiumPaymentDate = PaymentRaiseUtils.calculatePremiumPaymentDate(item.getNextPremiumDueDate(), item);
                paymentDetail
                        .setPremiumPaymentDate(DateUtils.stringToSqlDate(premiumPaymentDate, Constants.DATE_FORMAT));

                log.debug("PaymentRaiseProcessor : process : Premium payment date (PREM_PAYMENT_DATE) after process {}", premiumPaymentDate);

                /**
                 * Calculate and set next premium due date (NEXT_PREM_DUE_DATE) in premium
                 * component table. Next premium due date (Target collection date) and premium
                 * frequency is the input to calculate next premium due date
                 */
                if (item.getPremiumFrequency().equalsIgnoreCase(MONTHLY)) {
                    nextPremiumDueDateAfterProcess = PaymentRaiseUtils.calculateNextPremiumDueDate(
                            item.getNextPremiumDueDate().toLocalDate().toString(), Constants.PREMIUM_FREQUENCY_BY_MONTH,
                            item);
                } else if (item.getPremiumFrequency().equalsIgnoreCase(ANNUALLY)) {
                    nextPremiumDueDateAfterProcess = PaymentRaiseUtils.calculateNextPremiumDueDate(
                            item.getNextPremiumDueDate().toLocalDate().toString(), Constants.PREMIUM_FREQUENCY_BY_YEAR,
                            item);
                }

                log.debug("PaymentRaiseProcessor : process : Processed next premium due date (NEXT_PREM_DUE_DATE)  {}", nextPremiumDueDateAfterProcess);

                /**
                 * Calculate and set next payment due date (NEXT_PAYT_DUE_DATE) in premium
                 * component table. Processed next premium due date
                 * (nextPremiumDueDateAfterProcess) is the input to calculate next payment due
                 * date
                 */
                String nextPaymentDueDateAfterProcess = PaymentRaiseUtils.calculateNextPaymentDueDate(
                        DateUtils.stringToSqlDate(nextPremiumDueDateAfterProcess, Constants.DATE_FORMAT), item);
                log.debug("PaymentRaiseProcessor : process : Before process next prem due date {}", item.getNextPremiumDueDate());
                log.debug("PaymentRaiseProcessor : process : Before process final prem due date {}", policyDetails.getFinalPremDueDate());
                if (finalPayment) {
                    log.debug("PaymentRaiseProcessor : process : Inside final Payment");
                    paymentDetail.setBacsCode(Constants.BACS_CODE_FINAL_PAYMENT);
                    item.setNextPaymentDueDate(null);
                    item.setNextPremiumDueDate(null);
                } else {

                    log.debug("PaymentRaiseProcessor : process : Bacs code update to 17");
                    paymentDetail.setBacsCode(Constants.BACS_CODE_DD);
                    item.setNextPaymentDueDate(
                            DateUtils.stringToSqlDate(nextPaymentDueDateAfterProcess, Constants.DATE_FORMAT));
                    item.setNextPremiumDueDate(
                            DateUtils.stringToSqlDate(nextPremiumDueDateAfterProcess, Constants.DATE_FORMAT));

                }
                log.debug("PaymentRaiseProcessor : process : Processed next premium due date (NEXT_PREM_DUE_DATE)  {}", nextPremiumDueDateAfterProcess);

                log.debug("PaymentRaiseProcessor : process : Next payment due date (NEXT_PAYT_DUE_DATE) after process {}",
                        nextPaymentDueDateAfterProcess);

                databaseEntry.setPremiumComponent(item);
                databaseEntry.setPaymentDetail(paymentDetail);

                /**
                 * Update RADATAFEEDUP diary event
                 */
                databaseEntry.setDiaryEvent(paymentRaiseWdhFeed(item, paymentDetail));

            }
            databaseEntry.setErrorHandling(this.batchUtils.sendProcessIndicator());
        } catch (Exception e) {
            /**
             * Check if it is a business exception or not If it is a business exception then
             * write the error to the item and increment the number of business exception
             * count If the count of business exception is more than 20 then abend the job.
             */
            databaseEntry.setErrorHandling(this.batchUtils.checkExceptionType(e, this.jobExplorer,
                    Constants.BATCH, Constants.getNoOfBusinessException(),
                    Constants.BATCH_NAME, Constants.STEP_NAME1));
            Constants.setNoOfBusinessException(Constants.getNoOfBusinessException() + 1);

        }
        return databaseEntry;
    }

    /**
     * Update PaymentRaiseWdhFeed to populate data to a new staging table
     * TO3_PHOENIX_PAYMENT whenever a premium is raised as due in the Phoenix
     * system.
     * @param paymentDetail
     */
    private DiaryEvent paymentRaiseWdhFeed(PremiumComponent premiumComponent, PaymentDetail paymentDetail) {
        log.debug("PaymentRaiseProcessor : paymentRaiseWdhFeed : premiumComponent.getPolicy() : {}", premiumComponent.getPolicy());
        log.debug("PaymentRaiseProcessor : paymentRaiseWdhFeed : premiumComponent.getPolicy() : {}", paymentDetail.getPolicy());

        PaymentRaiseWDHFeed paymentRaiseWDHFeed = new PaymentRaiseWDHFeed();
        DiaryEvent diaryEvent = new DiaryEvent();

        Timestamp currentTimeStamp = DateUtils.getTimestamp();

        paymentRaiseWDHFeed.setPolicy(premiumComponent.getPolicy());
        paymentRaiseWDHFeed.setUpdatedIndicator(Constants.WDH_FEED_UPDATED_INDICATOR);
        paymentRaiseWDHFeed.setNextPremiumDue(premiumComponent.getNextPremiumDueDate());
        paymentRaiseWDHFeed.setDateNextDirectDebit(premiumComponent.getNextPaymentDueDate());
        paymentRaiseWDHFeed.setCurrPrem(paymentDetail.getPaymentAmount());
        /**
         * If policy is in reinstatement then outstanding money due = (sum of arrears +
         * current month payment)
         */
        if (premiumComponent.getStatus().equals(Constants.PREMIUM_COMPONENT_STATUS_LIVE)) {
            paymentRaiseWDHFeed.setOutstandingMoneyDue(premiumComponent.getPremiumSum());
        } else if (premiumComponent.getStatus().equals(Constants.PREMIUM_COMPONENT_STATUS_AWAIT_REINSTATEMENT)) {
            BigDecimal totalArrearAmount = this.paymentDetailService.getSumAmountOfArrears(premiumComponent.getPolicy(),
                    Constants.PAYMENT_DETAIL_STATUS_ARREAR);
            if (totalArrearAmount != null) {
                paymentRaiseWDHFeed.setOutstandingMoneyDue(totalArrearAmount.add(premiumComponent.getPremiumSum()));
            } else {
                paymentRaiseWDHFeed.setOutstandingMoneyDue(premiumComponent.getPremiumAmount());
            }
        }

        diaryEvent.setPolicy(premiumComponent.getPolicy());
        diaryEvent.setDiaryTimestamp(currentTimeStamp);
        diaryEvent.setCreatedTimestamp(currentTimeStamp);
        diaryEvent.setDiarySource(Constants.DIARY_SOURCE);
        diaryEvent.setDiaryType(Constants.DIARY_TYPE_RADATAFEEDUP);
        diaryEvent.setStatus(Constants.DIARY_EVENT_STATUS_LOADED);

        diaryEvent.setDiaryData(PaymentRaiseUtils.objectToJsonString(paymentRaiseWDHFeed));

        log.debug("PaymentRaiseProcessor : paymentRaiseWdhFeed : Payment raise WDH feed diary event: {}", diaryEvent);

        return diaryEvent;
    }

    public void setGetPolicyDetailService(PolicyDetailService policyDetailService) {
        this.policyDetailService = policyDetailService;
    }

    public void setPaymentDetailService(PaymentDetailService paymentDetailService) {
        this.paymentDetailService = paymentDetailService;
    }

    public void setBatchUtils(BatchUtils batchUtils) {
        this.batchUtils = batchUtils;
    }

}