package com.landg.phoenix.paymentraise.worker.batch.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.landg.phoenix.paymentraise.common.constants.Constants;
import com.landg.phoenix.paymentraise.worker.batch.entities.PremiumComponent;

public class PaymentRaiseUtilsTest {

	@Test
	public void TestIsWaiverPremium() {
		PremiumComponent item = new PremiumComponent();
		item.setStatus(Constants.PREMIUM_COMPONENT_STATUS_WAIVER);
		
		assertEquals(true, PaymentRaiseUtils.isWaiverPremium(item));
	}
	
	@Test
	public void TestIsLivePremium() {
		PremiumComponent item = new PremiumComponent();
		item.setStatus(Constants.PREMIUM_COMPONENT_STATUS_LIVE);
		
		assertEquals(true, PaymentRaiseUtils.isLivePremium(item));
	}
	
	@Test
	public void TestIsAwaitReinstatementPremium() {
		PremiumComponent item = new PremiumComponent();
		item.setStatus(Constants.PREMIUM_COMPONENT_STATUS_AWAIT_REINSTATEMENT);
		
		assertEquals(true, PaymentRaiseUtils.isAwaitReinstatementPremium(item));
	}

	@Test
	public void isSuppressedPremium() {
		PremiumComponent item = new PremiumComponent();
		item.setStatus(Constants.PREMIUM_COMPONENT_STATUS_SUPPRESS_PREM);

		assertEquals(true, PaymentRaiseUtils.isSuppressedPremium(item));
	}
	
}
