package com.landg.phoenix.paymentraise.worker.batch.entities;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 
 * Premium sum entity which holds sum ammount
 * 
 */
@Data
public class PremiumSum implements Serializable {
	
	private BigDecimal premiumSum;

}
