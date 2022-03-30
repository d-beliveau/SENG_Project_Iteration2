package tests;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Currency;

import org.junit.Before;
import org.junit.Test;
import org.lsmr.selfcheckout.Card;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;

import controller.BankStub;
import controller.CardFromCardReader;
import controller.CustomerCheckout;

public class TestCreditPayment {
	
	CardFromCardReader cardRead;
	Card credit;
	BankStub bank = new BankStub();
	
	Currency currency = Currency.getInstance("CAD");
	int[] banknoteDenominations = {1, 2, 5, 10};
	BigDecimal[] coinDenominations = {BigDecimal.TEN};
	
	SelfCheckoutStation station = new SelfCheckoutStation(currency, banknoteDenominations, coinDenominations, 10, 2);
	CustomerCheckout checkout = new CustomerCheckout(station);
	
	
	@Before
	public void setUp() {
		cardRead = new CardFromCardReader(station);
		
		station.cardReader.attach(cardRead);
		station.cardReader.enable();
		
		
		credit = new Card("Credit", "12345678", "A Person", "123", "5555", true, true);
		bank.setAvailableCreditLimit("12345678", new BigDecimal(1000.00));
	}
	
	@Test
	public void TestWhenEnoughFundsToPayTap() {
		BigDecimal payment = new BigDecimal(999.99);
		
		checkout.payWithDebitOrCredit(payment);
		
		//simulating a transaction with tap
		
		try {
			station.cardReader.tap(credit);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		assertEquals(payment, cardRead.getPaymentTotal());
		
		BigDecimal funds = bank.getAvailableCreditLimit("12345678");
		bank.setAvailableCreditLimit(null, funds.add(payment));
		
	}
	
	@Test
	public void TestWhenNotEnoughFunds() {
		
	}
	
	@Test
	public void TestWhenDataNotRead() {
		
	}
	
	@Test
	public void TestWhenDataRead() {
		
	}

}