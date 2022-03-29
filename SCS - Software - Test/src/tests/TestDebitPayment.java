package tests;

import java.math.BigDecimal;
import java.util.Currency;

import org.junit.Before;
import org.junit.Test;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;

import controller.CardFromCardReader;

public class TestDebitPayment {
	
	CardFromCardReader cardRead;
	
	Currency currency = Currency.getInstance("CAD");
	int[] banknoteDenominations = {1, 2, 5, 10};
	BigDecimal[] coinDenominations = {BigDecimal.TEN};
	
	SelfCheckoutStation station = new SelfCheckoutStation(currency, banknoteDenominations, coinDenominations, 10, 2);
	
	@Before
	public void setUp() {
		cardRead = new CardFromCardReader(station);
		
		station.cardReader.attach(cardRead);
		station.cardReader.enable();
	}
	
	 @Test
	 public void debitFundsAvailable() {
		 cardRead = new CardFromCardReader(station);
		 
		 station.cardReader.attach(cardRead);
	 }

}