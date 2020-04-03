package com.landg.phoenix.paymentraise.worker.batch.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.landg.phoenix.paymentraise.worker.batch.entities.Policy;

/**
 * 
 * Policy row mapper 
 * 
 */
@Slf4j
@Component
public class PolicyRowMapper implements RowMapper<Policy> {

	@Override
	public Policy mapRow(ResultSet resultSet, int i) throws SQLException {
		
		Policy policy = new Policy();
		
		policy.setPolicyNumber(resultSet.getString("POLICY"));
		policy.setEffectiveStartDate(resultSet.getDate("EFF_START_DATE"));
		policy.setEffectiveEndDate(resultSet.getDate("EFF_END_DATE"));
		policy.setCurrentTimestamp(resultSet.getTimestamp("CRTN_TMSTMP"));
		policy.setStatus(resultSet.getString("STATUS"));
		policy.setFinalPremDueDate(resultSet.getDate("FINAL_PREM_DUE"));
		log.debug("Policy mapper {}", policy);
		return policy;
	}
}
