package com.landg.phoenix.paymentraise.worker.batch.entities.feed;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;

import lombok.Data;

@Data
public class PaymentRaiseWDHFeed implements Serializable{

private static final long serialVersionUID = 1L;

	private String policy;
	
	private String updatedIndicator;
	
	private Date nextPremiumDue;
	
	private Date dateNextDirectDebit;
	
	private BigDecimal outstandingMoneyDue;

	private BigDecimal currPrem;
}
