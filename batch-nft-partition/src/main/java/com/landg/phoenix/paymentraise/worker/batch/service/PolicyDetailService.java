package com.landg.phoenix.paymentraise.worker.batch.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.landg.phoenix.paymentraise.worker.batch.entities.Policy;
import com.landg.phoenix.paymentraise.worker.batch.entities.PremiumComponent;
import com.landg.phoenix.paymentraise.worker.batch.mapper.PolicyRowMapper;

/**
 * 
 * Policy detail row mapper 
 * 
 * @author Vinod Singh ( 880960 )
 *
 * @Created on 2018-01-18
 * 
 */

@Service
public class PolicyDetailService {
	
	private static final Logger logger = LoggerFactory.getLogger(PolicyDetailService.class);
	
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	PolicyRowMapper policyRowMapper;
	
	public Policy policyService(PremiumComponent item) {
		return veryfiPolicyStatus(item);
	}
	
	public Policy veryfiPolicyStatus(PremiumComponent item) {
		try {
			String query = "select * from PHOENIX_POLICY.POLICY where POLICY =" + "'" + item.getPolicy() + "'"
					+ " and STATUS in ('00', '01', '02')";
			logger.debug("Policy table select query to verify policy status {}", query);
			return getJdbcTemplate().queryForObject(query, policyRowMapper);
		} catch (EmptyResultDataAccessException e) {
			logger.warn("No record found in policy table {}", e);
			return null;
		}
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
}