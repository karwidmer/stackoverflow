package com.landg.phoenix.paymentraise.worker.batch.service;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import com.landg.phoenix.dateutils.DateUtils;
import com.landg.phoenix.paymentraise.worker.batch.entities.PaymentDetail;
import com.landg.phoenix.paymentraise.worker.batch.entities.PremiumComponent;
import com.landg.phoenix.paymentraise.worker.batch.mapper.PaymentDetailRowMapper;

@RunWith(SpringRunner.class)
public class PaymentDetailServiceTest {
	
	@Autowired
	PaymentDetailService paymentDetailService;
	
	@MockBean
	JdbcTemplate jdbcTemplate;
	
	@MockBean
	PaymentDetailRowMapper paymentDetailRowMapper;
	
	@MockBean
	PolicyDetailService policyDetailservice;
	
	@Test
	public void checkIfPolicyExist_test() {
		
		PremiumComponent premiumComponent = new PremiumComponent();
		
		premiumComponent.setNextPremiumDueDate(DateUtils.localDateToSqlDate("2019-06-30"));
		PaymentDetail paymentDetail = new PaymentDetail();
		paymentDetail.setPremiumDueDate(DateUtils.localDateToSqlDate("2019-06-30"));
		paymentDetail.setStatus("01");
		assertEquals(true, paymentDetailService.checkIfPolicyExist(paymentDetail,premiumComponent));
		
		paymentDetail.setPremiumDueDate(DateUtils.localDateToSqlDate("2019-07-01"));
		
		assertEquals(true, paymentDetailService.checkIfPolicyExist(paymentDetail,premiumComponent));
		
		paymentDetail.setPremiumDueDate(DateUtils.localDateToSqlDate("2019-06-01"));
		
		assertEquals(false, paymentDetailService.checkIfPolicyExist(paymentDetail,premiumComponent));
		
	}
	
	@Configuration
	static class Config {
		
		@Bean		
		PaymentDetailService getPaymentService() {
			
			return new PaymentDetailService();
		}
		
		
	}
	
}
	
	

