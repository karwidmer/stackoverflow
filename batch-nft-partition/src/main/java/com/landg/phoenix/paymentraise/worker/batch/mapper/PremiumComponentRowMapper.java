package com.landg.phoenix.paymentraise.worker.batch.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.landg.phoenix.paymentraise.worker.batch.entities.PremiumComponent;

/**
 * 
 * Premium component row mapper 
 * 
 */

@Slf4j
@Component
public class PremiumComponentRowMapper implements RowMapper<PremiumComponent> {

	@Override
	public PremiumComponent mapRow(ResultSet resultSet, int i) throws SQLException {
		log.debug("PremiumComponentRowMapper : mapRow : i {}", i);

		PremiumComponent premiumComponent = new PremiumComponent();
		
		premiumComponent.setPremiumSum(resultSet.getBigDecimal("PREM_SUM"));
		premiumComponent.setPolicy(resultSet.getString("POLICY").trim());
		premiumComponent.setEffectiveStartDate(resultSet.getDate("EFF_START_DATE"));
		premiumComponent.setCollectionDay(resultSet.getString("COLLECTN_DAY").trim());
		premiumComponent.setBankDetailUid(resultSet.getLong("BANK_DETAIL_UID"));
		premiumComponent.setStatus(resultSet.getString("STATUS").trim());
		premiumComponent.setLastPaymentUid(resultSet.getLong("LAST_PAYMENT_UID"));
		premiumComponent.setNextPremiumDueDate(resultSet.getDate("NEXT_PREM_DUE_DATE"));
		premiumComponent.setNextPaymentDueDate(resultSet.getDate("NEXT_PAYT_DUE_DATE"));
		premiumComponent.setPremiumFrequency(resultSet.getString("PREM_FREQUENCY").trim());
		
		log.debug("Premium component row mapper {}", premiumComponent);

		return premiumComponent;
	}
}