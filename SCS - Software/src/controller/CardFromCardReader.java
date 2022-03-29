package controller;

import java.math.BigDecimal;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;


import org.lsmr.selfcheckout.Barcode;
import org.lsmr.selfcheckout.Card.CardData;
import org.lsmr.selfcheckout.devices.AbstractDevice;
import org.lsmr.selfcheckout.devices.CardReader;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;
import org.lsmr.selfcheckout.devices.observers.AbstractDeviceObserver;
import org.lsmr.selfcheckout.devices.observers.CardReaderObserver;
import org.lsmr.selfcheckout.products.BarcodedProduct;
import org.lsmr.selfcheckout.Card;

public class CardFromCardReader implements CardReaderObserver{
	
	private SelfCheckoutStation station;
	private CardData cardData;
	private String cardNumber;
	private String cardType;
	private boolean success = false;
	private BankStub bank = new BankStub();
	
	protected BigDecimal paymentAmount;
	protected BigDecimal paymentTotal;
	protected String memberNumber;
	
	public CardFromCardReader(SelfCheckoutStation station) {
		this.station = station;
		station.cardReader.attach(this);
	}

	//DEBIT CARD METHOD
	public boolean payWithDebit(CardData cardData) {
		boolean paymentSuccessful = false;
		BigDecimal funds = null;
		
		String cardNum = cardData.getNumber();
		if(bank.getAvailableDebitFunds(cardNum).compareTo(paymentAmount) < 0) {
			paymentSuccessful = false;
		}
		if(bank.getAvailableDebitFunds(cardNum).compareTo(paymentAmount) >= 0){
			paymentSuccessful = true;
			funds = bank.getAvailableDebitFunds(cardNum).subtract(paymentAmount);
			bank.setAvailableDebitFunds(cardNum, funds);
		}
		
		return paymentSuccessful;
	}
	
	//CREDIT CARD METHOD
	//Takes card data and customer's desired payment amount with this payment method
	public boolean payWithCredit(CardData cardData) {
		boolean paymentSuccessful = false;
		BigDecimal funds = null;
		
		String cardNum = cardData.getNumber();
		if(bank.getAvailableCreditFunds(cardNum).compareTo(paymentAmount) < 0) {
			paymentSuccessful = false;
		}
		if(bank.getAvailableCreditFunds(cardNum).compareTo(paymentAmount) >= 0){
			paymentSuccessful = true;
			funds = bank.getAvailableCreditFunds(cardNum).subtract(paymentAmount);
			bank.setAvailableCreditFunds(cardNum, funds);
		}
		
		return paymentSuccessful;		
		
	}
	
	public void reset() {
		cardNumber = null;
		cardData = null;
		success = false;
	}
	

	//MEMBERSHIP CARD METHOD
	public void membershipCard(CardData cardData) {
		memberNumber = cardData.getNumber();
	}
	
	public String getMemberNum() {
		return memberNumber;
	}
	
	
	@Override
	public void cardDataRead(CardReader reader, CardData data) {
		cardData = data;
		cardType = data.getType();
		switch(cardType) {
		case "Debit":
			success = payWithDebit(cardData);
			break;
			
		case "Credit":
			success = payWithCredit(cardData);
			break;
			
		case "Member":
			membershipCard(cardData);
			break;
		}
		
		if (success == true) {
			paymentTotal.add(paymentAmount);
		}
		reset();
	}
	
	/*
	 * Unused implementations
	 */

	@Override
	public void enabled(AbstractDevice<? extends AbstractDeviceObserver> device) {}

	@Override
	public void disabled(AbstractDevice<? extends AbstractDeviceObserver> device) {}

	@Override
	public void cardRemoved(CardReader reader) {}

	@Override
	public void cardTapped(CardReader reader) {}
	
	@Override
	public void cardInserted(CardReader reader) {}

	@Override
	public void cardSwiped(CardReader reader) {}
	
	// Not needed for now, later implementation maybe
		/*
		 * //Set the hash table of loyalty point Map
		public void setLoyaltyPointMap(HashMap<String, Integer> map) {
			loyaltyPointMap = map;
		}
		
		//Return the cash discount you get
		public int cashDiscount(int points) {
			int userPoint = loyaltyPointMap.get(cardNumber);
			int discount = userPoint/100;
			int newLoyaltyPoint = userPoint - (discount * 100);
			loyaltyPointMap.put(cardNumber, newLoyaltyPoint);
			return discount;
		}
		

		
		//Set customer points
		public void setPoints(String cardNum, int points) {
			loyaltyPointMap.put(cardNum, points);
		}
		
		//Get the customer points
		public int getPoints() {
			return loyaltyPointMap.get(cardNumber);
		}
		
		
		public void gainLoyaltyPoints(BigDecimal price) {
			int addedPoint = (price.intValue()/10) * 100;
			int newLoyaltyPoint = loyaltyPointMap.get(cardNumber) + addedPoint;
			loyaltyPointMap.put(cardNumber, newLoyaltyPoint);
		}
		*/


}
