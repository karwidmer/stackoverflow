package com.landg.phoenix.paymentraise.worker.batch.entities;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

/**
 * 
 * Payment detail Bank detail model 
 * 
 */

@Data
public class PaymentDetail implements Serializable {

	private Long paymentDetailUid;

	private String policy;

	private Long bankDetailUid;

	private Timestamp currentTimeStamp;

	private String paymentReference;

	private String origPaymentSource;

	private Date premiumDueDate;

	private String status;

	private BigDecimal paymentAmount;

	private Date premiumPaymentDate;

	private Date origCollectionDate;

	private Date actualCollection;

	private String actualPaymentSource;

	private String currency;

	private String bacsCode;
	
	private String commissionProcessedIndicator;

	@Override
	public String toString() {
		return "PaymentDetail [paymentDetailUid=" + this.paymentDetailUid + ", policy=" + this.policy + ", bankDetailUid="
				+ this.bankDetailUid + ", currentTimeStamp=" + this.currentTimeStamp + ", paymentReference=" + this.paymentReference
				+ ", origPaymentSource=" + this.origPaymentSource + ", policyMonthYear=" + this.premiumDueDate + ", status="
				+ this.status + ", paymentAmount=" + this.paymentAmount + ", paymentDueDate=" + this.premiumPaymentDate
				+ ", origCollectionDate=" + this.origCollectionDate + ", actualCollection=" + this.actualCollection
				+ ", actualPaymentSource=" + this.actualPaymentSource + ", currency=" + this.currency + ", bacsCode=" + this.bacsCode
				+ ", commissionProcessedIndicator=" + this.commissionProcessedIndicator + "]";
	}

}
