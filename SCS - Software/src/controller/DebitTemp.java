package controller;

import java.math.BigDecimal;
import java.util.HashMap;

import org.lsmr.selfcheckout.Card.CardData;
import org.lsmr.selfcheckout.devices.AbstractDevice;
import org.lsmr.selfcheckout.devices.CardReader;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;
import org.lsmr.selfcheckout.devices.observers.AbstractDeviceObserver;
import org.lsmr.selfcheckout.devices.observers.CardReaderObserver;

public class DebitTemp implements CardReaderObserver{
	private  HashMap<String , BigDecimal> debitCardMap = new HashMap<>();
	private BankStub bank = new BankStub();
	
	private SelfCheckoutStation station;
	private CardData cardData;
	protected String cardNumber;
	private String cardType;
	private boolean needPin = false;
	private BigDecimal amountOwed;
	private BigDecimal amountPaid;
		
	public DebitTemp(SelfCheckoutStation station) {
		this.station = station;
		station.cardReader.attach(this);
	}
	
	public void debitPay(CardData data) {
		cardData = data;
	}
	
	//Takes card data and customer's desired payment amount with this payment method
	public boolean payWithDebit(CardData cardData, BigDecimal payment) {
		boolean paymentSuccessful = false;
		BigDecimal funds = null;
		
		String cardNum = cardData.getNumber();
		if(bank.getAvailableDebitFunds(cardNum).compareTo(payment) < 0) {
			paymentSuccessful = false;
		}
		if(bank.getAvailableDebitFunds(cardNum).compareTo(payment) >= 0){
			paymentSuccessful = true;
			funds = bank.getAvailableDebitFunds(cardNum).subtract(payment);
			bank.setAvailableDebitFunds(cardNum, funds);
		}
		
		reset();
		
		return paymentSuccessful;
	}
	
	public void reset() {
		cardNumber = null;
		cardData = null;
	}
	
	//Set the hash table of debit Card Map
	public void setDebitMap(HashMap<String, BigDecimal> map) {
		debitCardMap = map;
	}

	@Override
	public void enabled(AbstractDevice<? extends AbstractDeviceObserver> device) {}

	@Override
	public void disabled(AbstractDevice<? extends AbstractDeviceObserver> device) {}
	
	@Override
	public void cardInserted(CardReader reader) {
		needPin = true;
	}

	@Override
	public void cardRemoved(CardReader reader) {}

	@Override
	public void cardTapped(CardReader reader) {}

	@Override
	public void cardSwiped(CardReader reader) {}


	@Override
	public void cardDataRead(CardReader reader, CardData data) {
		cardData = data;
		cardType = data.getType();
		cardNumber = data.getNumber();
		
		debitPay(data);
	}
}
