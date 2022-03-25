package controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

import org.lsmr.selfcheckout.Card;
import org.lsmr.selfcheckout.Card.CardData;

public class BankStub {
	private BigDecimal availableFunds;
	private double creditLimit = 1500;
	
	
	
	private Card card = new Card("debit", "1732785", "Person", "890","7483", true, true);
	
	public BankStub() {}
	
	public BigDecimal getCreditRemaining(CardData data) {
		//obtain information from third party financial institution
		
		//obtaining funds remaining
		availableFunds = new BigDecimal(((new Random().nextDouble() * (1500)) + 0.01)).setScale(2, RoundingMode.HALF_DOWN);
		return availableFunds;
	}

	
	/*
	 * Needed utility:
	 * Credit limit
	 * Debit funds
	 */
	



}


