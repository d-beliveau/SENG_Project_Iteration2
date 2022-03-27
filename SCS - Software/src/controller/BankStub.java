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
	
	private  HashMap<String , Double> creditLimitMap = new HashMap<>();
	private  HashMap<String , BigDecimal> creditFundsAvailableMap = new HashMap<>();

	
	public BankStub() {}
	
	public void setCreditLimit(String cardNumber, double limit) {
		creditLimitMap.put(cardNumber, limit);
	}
	
	public void setAvailableFunds(String cardNumber, BigDecimal funds) {
		creditFundsAvailableMap.put(cardNumber, funds);
	}
	
	public double getCreditLimit(String cardNumber) {
		return creditLimitMap.get(cardNumber);
	}
	
	public BigDecimal getAvailableFunds(String cardNumber) {
		return creditFundsAvailableMap.get(cardNumber);
	}
	
	//to get a randomized amount of funds available
	public BigDecimal getFundsRemaining(String cardNumber) {
		//obtain information from third party financial institution
		
		//obtaining funds remaining
		availableFunds = new BigDecimal(((new Random().nextDouble() * (creditLimit)) + 0.01)).setScale(2, RoundingMode.HALF_DOWN);
		creditFundsAvailableMap.put(cardNumber, availableFunds);
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


