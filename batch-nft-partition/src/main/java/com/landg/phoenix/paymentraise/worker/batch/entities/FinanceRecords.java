package com.landg.phoenix.paymentraise.worker.batch.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;

import lombok.Data;

@Data
public class FinanceRecords implements Serializable {

  private String transactionType;
  private String collectionType;
  private Date raisedDate;
  private Date dueDate;
  private Date collectionDate;
  private String policy;
  private String paymentReference;
  private String commissionType;
  private BigDecimal paymentAmount;
  private String agentNumber;
  private String paymentMonths;

}
