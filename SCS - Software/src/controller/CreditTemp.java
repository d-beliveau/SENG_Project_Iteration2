package controller;

import java.math.BigDecimal;
import java.util.HashMap;

import org.lsmr.selfcheckout.Card.CardData;
import org.lsmr.selfcheckout.devices.AbstractDevice;
import org.lsmr.selfcheckout.devices.CardReader;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;
import org.lsmr.selfcheckout.devices.observers.AbstractDeviceObserver;
import org.lsmr.selfcheckout.devices.observers.CardReaderObserver;

public class CreditTemp implements CardReaderObserver{
	private  HashMap<String , BigDecimal> creditCardMap = new HashMap<>();
	
	private SelfCheckoutStation station;
	private CardData cardData;
	protected String cardNumber;
	private String cardType;
	private boolean needPin = false;
	
	private boolean dataRead = false;
	
	private BigDecimal paymentAmount;
	
	
	private BankStub bank = new BankStub();
	
	public CreditTemp(SelfCheckoutStation station, BigDecimal payment) {
		this.station = station;
		station.cardReader.attach(this);
		
		paymentAmount = payment;
	}
	
	
	
	public void creditPay(CardData data) {
		cardData = data;
		cardType = data.getType();
		cardNumber = data.getNumber();
		payWithCredit(cardData, paymentAmount);
		
	}
	
	//Takes card data and customer's desired payment amount with this payment method
	public boolean payWithCredit(CardData cardData, BigDecimal payment) {
		boolean paymentSuccessful = false;
		BigDecimal funds = null;
		
		String cardNum = cardData.getNumber();
		if(bank.getAvailableCreditFunds(cardNum).compareTo(payment) < 0) {
			paymentSuccessful = false;
		}
		if(bank.getAvailableCreditFunds(cardNum).compareTo(payment) >= 0){
			paymentSuccessful = true;
			funds = bank.getAvailableCreditFunds(cardNum).subtract(payment);
			bank.setAvailableCreditFunds(cardNum, funds);
		}
		
		reset();
		
		return paymentSuccessful;		
		
	}
	
	public void reset() {
		cardNumber = null;
		cardData = null;
	}
	

	@Override
	public void enabled(AbstractDevice<? extends AbstractDeviceObserver> device) {}

	@Override
	public void disabled(AbstractDevice<? extends AbstractDeviceObserver> device) {}
	
	@Override
	public void cardInserted(CardReader reader) {
		dataRead = false;
		needPin = true;
	}

	@Override
	public void cardRemoved(CardReader reader) {}

	@Override
	public void cardTapped(CardReader reader) {
		dataRead = false;
		
	}

	@Override
	public void cardSwiped(CardReader reader) { 
		dataRead = false;
	}
		

	@Override
	public void cardDataRead(CardReader reader, CardData data) {
		dataRead = true;
		creditPay(data);
	}
}
