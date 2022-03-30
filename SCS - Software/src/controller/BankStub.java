package controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Random;

import org.lsmr.selfcheckout.Card;
import org.lsmr.selfcheckout.Card.CardData;

public class BankStub {
	private BigDecimal availableFunds;
	private double creditLimit;
	
	private  HashMap<String , BigDecimal> creditLimitAvailable = new HashMap<>();
	private  HashMap<String , BigDecimal> debitFundsAvailableMap = new HashMap<>();

	
	public BankStub() {}
	
	public void setAvailableCreditLimit(String cardNumber, BigDecimal funds) {
		creditLimitAvailable.put(cardNumber, funds);
	}
	
	public BigDecimal getAvailableCreditLimit(String cardNumber) {
		return creditLimitAvailable.get(cardNumber);
	}
	
	public void setAvailableDebitFunds(String cardNumber, BigDecimal funds) {
		debitFundsAvailableMap.put(cardNumber, funds);
	}
	
	public BigDecimal getAvailableDebitFunds(String cardNumber) {
		return debitFundsAvailableMap.get(cardNumber);
	}
	
	//to get a randomized amount of funds available (for testing if needed)
	public BigDecimal getFundsRemaining(String cardNumber) {
		//obtain information from third party financial institution
		
		//obtaining funds remaining
		availableFunds = new BigDecimal(((new Random().nextDouble() * (creditLimit)) + 0.01)).setScale(2, RoundingMode.HALF_DOWN);
		creditLimitAvailable.put(cardNumber, availableFunds);
		return availableFunds;
	}
	
	public void makePurchase(String cardNumber, BigDecimal amount) {
		availableFunds.subtract(amount);
	}
	

	/*
	 * Needed utility:
	 * Credit limit
	 * Debit funds
	 */
	



}


