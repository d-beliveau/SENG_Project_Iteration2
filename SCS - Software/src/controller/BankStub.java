package controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

import org.lsmr.selfcheckout.Card;
import org.lsmr.selfcheckout.Card.CardData;

public class BankStub {
	private BigDecimal availableFunds;
	private double creditLimit;
	
	
	public BankStub() {}
	
	public BigDecimal getCreditRemaining(CardData data) {
		//obtain information from third party financial institution
		
		//obtaining funds remaining
		availableFunds = new BigDecimal(((new Random().nextDouble() * (creditLimit)) + 0.01)).setScale(2, RoundingMode.HALF_DOWN);
		return availableFunds;
	}

	
	/*
	 * Needed utility:
	 * Credit limit
	 * Debit funds
	 */
	



}


