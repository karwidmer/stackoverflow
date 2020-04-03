package com.landg.phoenix.paymentraise.worker.batch.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.landg.phoenix.paymentraise.worker.batch.entities.PaymentDetail;

/**
 * 
 * Payment detail row mapper 
 * 
 */

@Slf4j
@Component
public class PaymentDetailRowMapper implements RowMapper<PaymentDetail>{
	
	@Override
	public PaymentDetail mapRow(ResultSet resultSet, int i) throws SQLException {
		
		PaymentDetail paymentDetail = new PaymentDetail();
		
		paymentDetail.setPaymentDetailUid(resultSet.getLong("PAYMENT_DETAIL_UID"));
		paymentDetail.setPolicy(resultSet.getString("POLICY"));
		paymentDetail.setBankDetailUid(resultSet.getLong("BANK_DETAIL_UID"));
		paymentDetail.setCurrentTimeStamp(resultSet.getTimestamp("CRTN_TMSTMP"));
		paymentDetail.setPaymentReference(resultSet.getString("PAYMENT_REF"));
		paymentDetail.setOrigPaymentSource(resultSet.getString("ORIG_PAYT_SOURCE"));
		paymentDetail.setPremiumDueDate(resultSet.getDate("PREM_DUE_DATE"));
		paymentDetail.setStatus(resultSet.getString("STATUS").trim());
		paymentDetail.setPaymentAmount(resultSet.getBigDecimal("PAYMENT_AMOUNT"));
		paymentDetail.setPremiumPaymentDate(resultSet.getDate("PREM_PAYMENT_DATE"));
		paymentDetail.setOrigCollectionDate(resultSet.getDate("ORIG_COLLECTN_DATE"));
		paymentDetail.setActualCollection(resultSet.getDate("ACTUAL_COLLECTION"));
		paymentDetail.setActualPaymentSource(resultSet.getString("ACTUAL_PAYT_SOURCE"));
		paymentDetail.setCurrency(resultSet.getString("CURRENCY"));
		paymentDetail.setBacsCode(resultSet.getString("BACS_CODE"));
		
		log.debug("Payment details mapper {}", paymentDetail);
		
		return paymentDetail;
	}

}
