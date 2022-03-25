package controller;

import java.math.BigDecimal;
import java.util.Random;

import org.lsmr.selfcheckout.Card.CardData;

public class BankStub {
	private BigDecimal availableFunds;
	private double creditLimit;
	
	
	public BigDecimal getCreditRemaining(CardData data) {
		//obtain information from third party financial institution
		
		//obtaining funds remaining
		availableFunds = new BigDecimal(((new Random().nextDouble() * (creditLimit)) + 0.01));
		availableFunds.setScale(2);
		
		return availableFunds;
	}
	
	/*
	 * Needed utility:
	 * Credit limit
	 * Debit funds
	 */
	
}
