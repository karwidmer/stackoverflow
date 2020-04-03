package com.landg.phoenix.paymentraise.worker.batch.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.landg.phoenix.paymentraise.common.constants.SQLConstants;
import com.landg.phoenix.paymentraise.worker.batch.entities.PaymentDetail;
import com.landg.phoenix.paymentraise.worker.batch.entities.Policy;
import com.landg.phoenix.paymentraise.worker.batch.entities.PremiumComponent;
import com.landg.phoenix.paymentraise.worker.batch.mapper.PaymentDetailRowMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * Payment detail service
 * 
 */
@Slf4j
@Service
public class PaymentDetailService {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	PaymentDetailRowMapper paymentDetailRowMapper;
	@Autowired
	PolicyDetailService getPolicyDetail;

	public PaymentDetail checkExistingPaymentDetail(PremiumComponent item) {
		log.debug("PaymentDetailService : checkExistingPaymentDetail : item.getPolicy() : {}", item.getPolicy());
		PaymentDetail paymentDetail = getPaymentDetail(item);

		if (paymentDetail == null) {
			return new PaymentDetail();
		} else if (checkIfPolicyExist(paymentDetail, item)) {
			return null;
		} else {
			return paymentDetail;
		}

	}

	public PaymentDetail getPaymentDetail(PremiumComponent item) {
		log.debug("PaymentDetailService : getPaymentDetail : item.getPolicy() {}", item.getPolicy());
		try {
			String query = SQLConstants.PAYMENT_DETAIL_SELECT_QUERY_TO_VERIFY_EXISTING_RECORD + "'"
					+ item.getLastPaymentUid() + "'";
			log.debug("PaymentDetailService : getPaymentDetail : Payment detail table read query to check existing record {}", query);
			return getJdbcTemplate().queryForObject(query, this.paymentDetailRowMapper);
		} catch (EmptyResultDataAccessException e) {
			log.warn("There is no exixting record found in payment detail table {}", e);
			return null;
		}
	}

	public boolean checkIfPolicyExist(PaymentDetail paymentDetail, PremiumComponent item) {
		return ((paymentDetail.getPremiumDueDate().getTime() 
				>= item.getNextPremiumDueDate().getTime())
				&& paymentDetail.getStatus().equalsIgnoreCase("01"));
	}

	public boolean checkIfFinalPayment(PremiumComponent item, Policy policyDetails) {
		log.debug("PaymentDetailService : checkIfFinalPayment : Inside paymentdetail service : check final payment details");
		log.debug("PaymentDetailService : checkIfFinalPayment : policy final prem due date {} :", policyDetails.getFinalPremDueDate());
		log.debug("PaymentDetailService : checkIfFinalPayment : prem component next pre due date {}", item.getNextPremiumDueDate());
		if (policyDetails.getFinalPremDueDate().compareTo(item.getNextPremiumDueDate()) == 0) {
			log.debug("Satisfied final payment details condition {}");
			return true;
		}
		return false;
	}

	/**
	 * Get sum amount of all arrears payment of the policy
	 */
	public BigDecimal getSumAmountOfArrears(String policyNumber, String status) {

		return this.jdbcTemplate.query(SQLConstants.PAYMENT_DETAIL_ARRAER_SUM_PAYMENT_AMOUNT,
				new Object[] { policyNumber, status }, rs -> {
					if (rs.next()) {
						log.debug("Sum of arrear amount: {}", rs.getBigDecimal("PAYMENT_AMOUNT"));
						return rs.getBigDecimal("PAYMENT_AMOUNT");
					} else {
						return null;
					}
				});
	}

	public JdbcTemplate getJdbcTemplate() {
		return this.jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

}
