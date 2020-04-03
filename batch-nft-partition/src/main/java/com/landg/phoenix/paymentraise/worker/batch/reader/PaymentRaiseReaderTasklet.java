package com.landg.phoenix.paymentraise.worker.batch.reader;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import com.landg.phoenix.paymentraise.worker.batch.mapper.PremiumComponentRowMapper;
import lombok.extern.slf4j.Slf4j;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.landg.phoenix.paymentraise.worker.batch.entities.PremiumComponent;
import com.landg.phoenix.dateutils.DateUtils;
import com.landg.phoenix.paymentraise.common.configuration.PropertiesConfig;
import com.landg.phoenix.paymentraise.common.constants.SQLConstants;

/**
 *
 * Premium component Reader
 *
 */

@Slf4j
@Component
@StepScope
public class PaymentRaiseReaderTasklet {

    @Autowired
    private PropertiesConfig propertiesConfig;

    private JdbcOperations jdbcTemplate;

    private List<PremiumComponent> premiumComponents;

    public PaymentRaiseReaderTasklet(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public List<PremiumComponent> execute(StepContribution stepContribution, ChunkContext chunkContext, String timeTravelDate, int leadTime, Long minValue, Long maxValue) throws Exception {

        log.debug("PaymentRaiseReaderTasklet : execute : TimeTravelDate={} : LeadTime={} : minValue={} : maxValue={}", timeTravelDate, leadTime, minValue, maxValue);
        List<String> targetDateRange = DateUtils.getCollectionTargetDate(timeTravelDate, leadTime);

        premiumComponents = new ArrayList<PremiumComponent>();

        String SQL_PREMIUM_COMPONENT_QUERY = SQLConstants.PREMIUM_COMPONENT_SELECT_CLAUSE +
                SQLConstants.PREMIUM_COMPONENT_FROM_CLAUSE +
                SQLConstants.PREMIUM_COMPONENT_WHERE_CLAUSE_WHERE +
                SQLConstants.PREMIUM_COMPONENT_WHERE_CLAUSE_RANGE_MIN + Long.toString(minValue) +
                SQLConstants.PREMIUM_COMPONENT_WHERE_CLAUSE_RANGE_MAX + Long.toString(maxValue) +
                SQLConstants.PREMIUM_COMPONENT_WHERE_CLAUSE_PART1 + "'" + targetDateRange.get(0) + "'" +
                SQLConstants.PREMIUM_COMPONENT_WHERE_CLAUSE_PART2 + "'" + targetDateRange.get(1) + "'" +
                SQLConstants.PREMIUM_COMPONENT_WHERE_CLAUSE_PART3 +
                SQLConstants.PREMIUM_COMPONENT_WHERE_CLAUSE_PART4 +
                SQLConstants.PREMIUM_COMPONENT_WHERE_CLAUSE_PART5 +
                SQLConstants.PREMIUM_COMPONENT_GROUP_BY_CLAUSE +
                SQLConstants.PREMIUM_COMPONENT_ORDER_BY_CLAUSE;
        log.debug("PaymentRaiseReaderTasklet : execute : SQL_PREMIUM_COMPONENT_QUERY : {}", SQL_PREMIUM_COMPONENT_QUERY);

        premiumComponents = this.jdbcTemplate.query(SQL_PREMIUM_COMPONENT_QUERY, new PremiumComponentRowMapper());

        log.debug("PaymentRaiseReaderTasklet : execute : premiumComponents.size() : {}", premiumComponents.size());

        return premiumComponents;
    }

    @Autowired
    public void setPropertiesConfig(PropertiesConfig propertiesConfig) {
        this.propertiesConfig = propertiesConfig;
    }

}