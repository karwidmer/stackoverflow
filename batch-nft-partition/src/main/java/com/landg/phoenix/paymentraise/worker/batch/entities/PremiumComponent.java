package com.landg.phoenix.paymentraise.worker.batch.entities;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

/**
 * 
 * Premium component entity
 * 
 */
@Data
public class PremiumComponent implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Long premiumComponentUid;

	private Long policyComponentUid;

	private String policy;

	private String benefitType;

	private Long bankDetailUid;

	private Date effectiveStartDate;

	private Date effectiveEndDate;

	private Timestamp currentTimeStamp;

	private String status;

	private Date nextPaymentDueDate;

	private String paymenyMechanism;

	private BigDecimal premiumAmount;

	private String premiumFrequency;

	private String collectionDay;

	private String collectionMonth;

	private String commisionIndicator;

	private String reassuranceIndicator;

	private String iptIndicator;

	private String currency;

	private Long lastPaymentUid;

	private Date nextPremiumDueDate;
	
	private BigDecimal premiumSum;

	@Override
	public String toString() {
		return "PremiumComponent [premiumComponentUid=" + this.premiumComponentUid + ", policyComponentUid="
				+ this.policyComponentUid + ", policy=" + this.policy + ", benefitType=" + this.benefitType + ", bankDetailUid="
				+ this.bankDetailUid + ", effectiveStartDate=" + this.effectiveStartDate + ", effectiveEndDate="
				+ this.effectiveEndDate + ", currentTimeStamp=" + this.currentTimeStamp + ", status=" + this.status
				+ ", nextPaymentDueDate=" + this.nextPaymentDueDate + ", paymenyMechanism=" + this.paymenyMechanism
				+ ", premiumAmount=" + this.premiumAmount + ", premiumFrequency=" + this.premiumFrequency + ", collectionDay="
				+ this.collectionDay + ", collectionMonth=" + this.collectionMonth + ", commisionIndicator=" + this.commisionIndicator
				+ ", reassuranceIndicator=" + this.reassuranceIndicator + ", iptIndicator=" + this.iptIndicator + ", currency="
				+ this.currency + ", lastPaymentUid=" + this.lastPaymentUid + ", nextPremiumDueDate=" + this.nextPremiumDueDate + "]";
	}
}
