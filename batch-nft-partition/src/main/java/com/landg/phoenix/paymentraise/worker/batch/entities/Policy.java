package com.landg.phoenix.paymentraise.worker.batch.entities;

import lombok.Data;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;

/**
 * 
 * Policy entity
 * 
 */
@Data
public class Policy  implements Serializable {

	private Long policyComponentUid;

	private String policyNumber;

	private Date effectiveStartDate;

	private Date effectiveEndDate;

	private Timestamp currentTimestamp;

	private String status;
	
	private Date finalPremDueDate;
	
	@Override
	public String toString() {
		return "Policy [policyComponentUid=" + this.policyComponentUid + ", policyNumber=" + this.policyNumber
				+ ", effectiveStartDate=" + this.effectiveStartDate + ", effectiveEndDate=" + this.effectiveEndDate
				+ ", currentTimestamp=" + this.currentTimestamp + ", status=" + this.status + "]";
	}

}
