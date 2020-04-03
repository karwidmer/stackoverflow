package com.landg.phoenix.paymentraise.worker.batch.entities;

import lombok.Data;

import java.io.Serializable;
import java.sql.Date;

/**
 * 
 * Payment raise Bank detail model 
 * 
 */
@Data
public class BankDetail implements Serializable {

	private Long bankDetailUid;

	private String paymentSource;

	private Date effctiveSrartDate;

	private Date effectiveEndDate;

	private String status;

	private String sortCode;

	private String accountNumber;

	private String accountName;

	private String bankName;

	private String payeePartyId;

	private String bacsAccountType;

	private String cancellationType;

}
