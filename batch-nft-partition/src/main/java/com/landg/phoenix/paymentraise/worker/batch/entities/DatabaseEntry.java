package com.landg.phoenix.paymentraise.worker.batch.entities;

import java.io.Serializable;

import com.landg.phoenix.batchexception.models.ErrorHandling;

import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * 
 * Payment raise DatabaseEntry entity consist Payment detail, Premium component
 * and PaymentRaiseWDHFeed model
 * 
 */

@Component
@Data
public class DatabaseEntry implements Serializable {

	private static final long serialVersionUID = 1L;

	private PaymentDetail paymentDetail;

	private PremiumComponent premiumComponent;

	private Long lastPaymentUid;

	private DiaryEvent diaryEvent;
	
	private ErrorHandling errorHandling;

}
