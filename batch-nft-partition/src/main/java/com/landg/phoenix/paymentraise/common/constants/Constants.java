package com.landg.phoenix.paymentraise.common.constants;



public class Constants {

	/**
	 * 
	 * Payment raise batch constants
	 * 
	 */

	private Constants() {

	}

	/**
	 * Payment detail table Status
	 */

	public static final String PAYMENT_DETAIL_STATUS_READY_FOR_COLLECTION = "01";

	public static final String PAYMENT_DETAIL_STATUS_SUBMITTED = "02";

	public static final String PAYMENT_DETAIL_STATUS_COLLECTED = "03";

	public static final String PAYMENT_DETAIL_STATUS_ARREARS = "04";
	
	public static final String PAYMENT_DETAIL_STATUS_WAIVER_FOR_COLLECTION = "11";

	public static final String PAYMENT_DETAIL_STATUS_ARREARS_FOR_COLLECTION = "05";

	public static final String PAYMENT_DETAIL_STATUS_SUPPRESS_PREMIUM = "09";

	/**
	 * Premium component table status
	 * 
	 */

	public static final String PREMIUM_COMPONENT_STATUS_RAISED_NOT_ACTIVE = "00";

	public static final String PREMIUM_COMPONENT_STATUS_LIVE = "01";

	public static final String PREMIUM_COMPONENT_STATUS_CLOSED = "03";

	public static final String PREMIUM_COMPONENT_STATUS_AWAIT_REINSTATEMENT = "04";

	public static final String PREMIUM_COMPONENT_STATUS_WAIVER = "05";

	public static final String PREMIUM_COMPONENT_STATUS_SUPPRESS_PREM = "09";

	/**
	 * Policy table Status
	 */

	/**
	 * BACS CODE constant
	 */

	public static final String BACS_CODE_FIRST_DD_PAYMENT = "01";

	public static final String BACS_CODE_DD = "17";

	public static final String BACS_CODE_REPRESENTATION_OF_DD = "18";

	public static final String BACS_CODE_FINAL_PAYMENT = "19";

	/**
	 * Payment source constants
	 */

	public static final String PAYMENT_SOURCE_DIRECT_DEBIT = "DD";

	public static final String PAYMENT_SOURCE_WORLD_PAY = "WP";

	public static final String PAYMENT_SOURCE_ECKOPAY = "EP";

	public static final String PAYMENT_SOURCE_CHEQUE = "CQ";

	public static final Long PREMIUM_FREQUENCY_BY_MONTH = 1L;

	public static final Long PREMIUM_FREQUENCY_BY_QUATER = 3L;

	public static final Long PREMIUM_FREQUENCY_BY_YEAR = 12L;

	public static final String DATE_FORMAT = "yyyy-MM-dd";

	public static final String CURRENCY = "GBP";

	public static final String COMMISSION_PROCESSED_IND = "N";
	
	public static final String PAYMENT_DETAIL_STATUS_ARREAR = "04";

	/**
	 * Diary Event details
	 */
	public static final String DIARY_EVENT_STATUS_LOADED = "00";
	
	public static final String DIARY_SOURCE = "RAISEPAYMENT";

	public static final String DIARY_TYPE_RADATAFEEDUP = "RADATAFEEDUP";

	public static final String WDH_FEED_UPDATED_INDICATOR = "Y";

	public static final String TARGET_SYSTEM_GFAS = "GFAS";

	public static final String TRANSACTION_TYPE = "PD";
	
	public static final String CREATED_USER_ID = "Batch";
		
	public static final String BUSINESS_EXCEPTION = "98";
		
	public static final String TECHNICAL_EXCEPTION = "99";
		
	public static final String BATCH_NAME = "Paymentraise_Batch";
		    
	public static final String BATCH = "jobRemotePartitioning";
	
	public static final String STEP_NAME1 = "paymentRaiseWorkerStep";
	
	public static final String TABLE_BATCH_NAME = "Payraise_batch";
	
	private static int noOfBusinessException=0;
	
	public static int getNoOfBusinessException() {
		return noOfBusinessException;
	}

	public static void setNoOfBusinessException(int noOfBusinessException) {
		Constants.noOfBusinessException = noOfBusinessException;
	}
	
	private static boolean raiseAlertIndicator = true;

	public static void setRaiseAlertIndicator(boolean raiseAlertIndicator) {
		Constants.raiseAlertIndicator = raiseAlertIndicator;
	}
	public static boolean isRaiseAlertIndicator() {
		return raiseAlertIndicator;
	}

}