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

public class MembershipCard implements CardReaderObserver{

	
	private SelfCheckoutStation station;
	
	//Map card number to loyalty point
	private  HashMap<String, Integer> loyaltyPointMap = new HashMap<>();
	private String cardNumber;
	
	public MembershipCard(SelfCheckoutStation station) {
		this.station = station;
		station.cardReader.attach(this);
	}
	
	//Set the hash table of loyalty point map
	public void  setLoyaltyPointMap(HashMap<String, Integer> map) {
		loyaltyPointMap = map;
	}
	
	//Set customer points
	public void setPoints(String cardNum, int points) {
		loyaltyPointMap.put(cardNum, points);
	}
	
	//Get the customer points
	public int getPoints() {
		return loyaltyPointMap.get(cardNumber);
	}
	
	//Return the cash discount you get
	public int cashDiscount(int points) {
		int userPoint = loyaltyPointMap.get(cardNumber);
		int discount = userPoint/100;
		int newLoyaltyPoint = userPoint - (discount * 100);
		loyaltyPointMap.put(cardNumber, newLoyaltyPoint);
		return discount;
	}
	
	public void gainLoyaltyPoints(BigDecimal price) {
		int addedPoint = (price.intValue()/10) * 100;
		int newLoyaltyPoint = loyaltyPointMap.get(cardNumber) + addedPoint;
		loyaltyPointMap.put(cardNumber, newLoyaltyPoint);
	}
	
	
	@Override
	public void enabled(AbstractDevice<? extends AbstractDeviceObserver> device) {
		// Ignore
		
	}

	@Override
	public void disabled(AbstractDevice<? extends AbstractDeviceObserver> device) {
		// Ignore
		
	}

	@Override
	public void cardInserted(CardReader reader) {
		// Ignore
		
	}

	@Override
	public void cardRemoved(CardReader reader) {
		//Ignore
	}

	@Override
	public void cardTapped(CardReader reader) {
		// Ignore
		
	}

	@Override
	public void cardSwiped(CardReader reader) {
		// Ignore
		
	}


	@Override
	public void cardDataRead(CardReader reader, CardData data) {
		//Retrieve the card number
		if(data.getType().equals("Membership")) {
			cardNumber = data.getNumber();
		}
	}
	
	

}
