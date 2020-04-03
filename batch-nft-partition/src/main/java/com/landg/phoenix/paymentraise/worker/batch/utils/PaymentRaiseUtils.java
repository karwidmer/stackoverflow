package com.landg.phoenix.paymentraise.worker.batch.utils;

import java.sql.Date;
import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.landg.phoenix.dateutils.DateUtils;
import com.landg.phoenix.paymentraise.common.constants.Constants;
import com.landg.phoenix.paymentraise.worker.batch.entities.FinanceRecords;
import com.landg.phoenix.paymentraise.worker.batch.entities.PaymentDetail;
import com.landg.phoenix.paymentraise.worker.batch.entities.PremiumComponent;
import com.landg.phoenix.paymentraise.common.exception.CustomJsonMappingException;

/**
 * 
 * Payment raise utils
 * 
 */

@Component
public class PaymentRaiseUtils {

	private PaymentRaiseUtils() {

	}

	private static final Logger logger = LoggerFactory.getLogger(PaymentRaiseUtils.class);

	/**
	 * Calculate next premium due date
	 * 
	 * @String
	 * @Long
	 * @PremiumComponent
	 * @return String
	 */
	public static String calculateNextPremiumDueDate(String currentNextPremiumDueDate, Long premiumFrequency,
			PremiumComponent item) {
		/**
		 * Next premium due date of last record + 1 frequency is derived next premium
		 * due date
		 */
		String derivedNextPremiumDate = LocalDate.parse(currentNextPremiumDueDate).plusMonths(premiumFrequency)
				.toString();
		LocalDate nextPremiumDueDate = LocalDate.parse(currentNextPremiumDueDate).plusMonths(premiumFrequency);
		LocalDate effectiveStartDate = LocalDate.parse(item.getEffectiveStartDate().toString());
		/**
		 * Compare DD of derived next premium due date and day of policy start date. If
		 * matches then keep the derived next premium date else replace derived next
		 * premium DD by day of policy start date.
		 * 
		 * Check if date is valid then it is correct next premium due date else roll
		 * back to last valid date.
		 */
		if (nextPremiumDueDate.getDayOfMonth() == effectiveStartDate.getDayOfMonth()) {
			logger.debug("Next premium date and effective start date is same {}", nextPremiumDueDate);
			return nextPremiumDueDate.toString();
		} else {
			nextPremiumDueDate = DateUtils.rollBackAfterReplacingDay(derivedNextPremiumDate, effectiveStartDate);
		}

		logger.debug("Next premium due date after process {}", nextPremiumDueDate);
		return nextPremiumDueDate.toString();
	}

	/**
	 * Calculate premium payment date
	 * 
	 * @Param Date
	 * @Param PremiumComponent
	 * @return String
	 */

	public static String calculatePremiumPaymentDate(Date nextPremiumDueDateAfterProcess, PremiumComponent item) {
		int nextPremiumYear = nextPremiumDueDateAfterProcess.toLocalDate().getYear();
		int nextPremiumMonth = nextPremiumDueDateAfterProcess.toLocalDate().getMonthValue();
		int nextPremiumDay = nextPremiumDueDateAfterProcess.toLocalDate().getDayOfMonth();
		int pcd = Integer.parseInt(item.getCollectionDay());
		boolean isValidDate = false;

		int effStartDateDay = item.getEffectiveStartDate().toLocalDate().getDayOfMonth();

		/**
		 * Check DD(day) of Next Premium Due Date with PCD (Collection day), if not
		 * equal then, replace next premium day by pcd
		 */
		if (nextPremiumDay != pcd) {
			nextPremiumDay = pcd;
		}

		/**
		 * Compare next premium day with effective start date day, if next premium day
		 * is less than effective start day then increase the next premium month by 1
		 */
		if (nextPremiumDay < effStartDateDay) {
			if (nextPremiumMonth == 12) {
				nextPremiumMonth++;
				nextPremiumYear++;
			} else {
				nextPremiumMonth++;
			}
		}

		/**
		 * Check if the date is valid, if valid keep the date else roll forward to next
		 * valid date
		 */

		while (!isValidDate) {
			isValidDate = DateUtils.validateDate(nextPremiumYear, nextPremiumMonth, nextPremiumDay);
			if (!isValidDate) {
				if (nextPremiumDay == 31) {
					nextPremiumDay = 1;
					if (nextPremiumMonth < 12) {
						nextPremiumMonth++;
					} else {
						nextPremiumYear++;
						nextPremiumMonth = 1;
					}
				} else {
					nextPremiumDay++;
				}
			}
		}

		logger.debug("Payment due date after process {}",
				LocalDate.of(nextPremiumYear, nextPremiumMonth, nextPremiumDay));

		/**
		 * returning premium due date after final calculation
		 */
		return LocalDate.of(nextPremiumYear, nextPremiumMonth, nextPremiumDay).toString();
	}

	/**
	 * Calculate next payment due date using calculatePaymentDueDate method. This
	 * method will move forward to next valid working day
	 * 
	 * @Param Date
	 * @Param PremiumComponent
	 * @return String
	 */

	public static String calculateNextPaymentDueDate(Date nextPremiumDueDateAfterProcess, PremiumComponent item) {

		/**
		 * if not working day then roll forward to the valid working day
		 */

		return DateUtils.findNextWorkingDay(calculatePremiumPaymentDate(nextPremiumDueDateAfterProcess, item));
	}

	/**
	 * Generate reference number
	 * 
	 * @PremiumComponent
	 * @return String
	 */

	public static String getPaymentReferanceNumber(PremiumComponent item) {
		if (item.getPolicy() != null && item.getNextPaymentDueDate() != null) {
			return item.getPolicy() + "-" + (item.getNextPaymentDueDate().toLocalDate().getYear() % 100)
					+ (item.getNextPaymentDueDate().toLocalDate().getMonthValue() < 10
							? "0" + item.getNextPaymentDueDate().toLocalDate().getMonthValue()
							: item.getNextPaymentDueDate().toLocalDate().getMonthValue())
					+ (item.getNextPaymentDueDate().toLocalDate().getDayOfMonth() < 10
							? "0" + item.getNextPaymentDueDate().toLocalDate().getDayOfMonth()
							: item.getNextPaymentDueDate().toLocalDate().getDayOfMonth());
		}
		logger.debug("getPaymentReferanceNumber---- policy is {} or nextpayment due date is {}", item.getPolicy(),
				item.getNextPaymentDueDate());

		return null;
	}

	/**
	 * THis Util mapper maps Finance Trigger data for Trans Type PD
	 * 
	 * @param paymentDetail
	 * @param uid
	 * @return
	 */

	public static FinanceRecords getFinanceTransactionRecords(PaymentDetail paymentDetail, Long uid) {
		FinanceRecords record = new FinanceRecords();
		record.setTransactionType(Constants.TRANSACTION_TYPE);
		record.setCollectionType(Constants.PAYMENT_SOURCE_DIRECT_DEBIT);
		record.setDueDate(paymentDetail.getPremiumDueDate());
		record.setCollectionDate(paymentDetail.getOrigCollectionDate());
		record.setRaisedDate(new java.sql.Date(paymentDetail.getCurrentTimeStamp().getTime()));
		record.setPolicy(paymentDetail.getPolicy());
		record.setPaymentReference(paymentDetail.getPaymentReference());
		record.setPaymentAmount(paymentDetail.getPaymentAmount());
		return record;
	}

	/**
	 * Common JSON mapping
	 */
	public static String objectToJsonString(Object obj) {
		ObjectMapper mapper = new ObjectMapper();
		String jsonString = null;
		try {
			jsonString = mapper.writeValueAsString(obj);
		} catch (JsonProcessingException ex) {
			throw new CustomJsonMappingException("Policy end batch - Json Mapping Error : ", ex);
		}
		return jsonString;
	}
	
	public static boolean isWaiverPremium(PremiumComponent item) {
		if(Constants.PREMIUM_COMPONENT_STATUS_WAIVER.equalsIgnoreCase(item.getStatus().trim())) {
			return true;
		}
		return false;
	}
	
	public static boolean isLivePremium(PremiumComponent item) {
		if(Constants.PREMIUM_COMPONENT_STATUS_LIVE.equalsIgnoreCase(item.getStatus().trim())){
			return true;
		}
		return false;
	}
	
	public static boolean isAwaitReinstatementPremium(PremiumComponent item) {
		if(Constants.PREMIUM_COMPONENT_STATUS_AWAIT_REINSTATEMENT.equalsIgnoreCase(item.getStatus().trim())) {
			return true;
		}
		return false;
	}

	public static boolean isSuppressedPremium(PremiumComponent item) {
		if(Constants.PREMIUM_COMPONENT_STATUS_SUPPRESS_PREM.equalsIgnoreCase(item.getStatus().trim())) {
			return true;
		}
		return false;
	}
}
