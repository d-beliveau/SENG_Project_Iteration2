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
	BigDecimal creditLimitBefore;
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
		creditLimitBefore = bank.getAvailableCreditLimit("12345678");
		
		cardRead.resetPaymentTotal();
	}
	
	//checking available credit limit after payment
	@Test
	public void TestCreditLimitAfterPurchase() {
		BigDecimal payment = new BigDecimal(500.00);
		
		checkout.payWithDebitOrCredit(payment);
		
		try {
			station.cardReader.tap(credit);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		assertEquals(creditLimitBefore, bank.getAvailableCreditLimit("12345678").add(payment));
		
		bank.setAvailableCreditLimit("12345678", creditLimitBefore);
		
	}
	

	@Test
	public void TestWhenEnoughFundsToPayTap() {
		BigDecimal payment = new BigDecimal(355.67);
		
		checkout.payWithDebitOrCredit(payment);
		
		//simulating a transaction with tap
		
		try {
			station.cardReader.tap(credit);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		assertEquals(payment, cardRead.getPaymentTotal());
		
		bank.setAvailableCreditLimit("12345678", creditLimitBefore);
		
	}
	
	@Test
	public void TestWhenEnoughFundsToPaySwipe() {
		BigDecimal payment = new BigDecimal(789.32);
		
		checkout.payWithDebitOrCredit(payment);
		
		//simulating a transaction with swipe
		
		try {
			station.cardReader.swipe(credit);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		assertEquals(payment, cardRead.getPaymentTotal());
		
		
		bank.setAvailableCreditLimit("12345678", creditLimitBefore);
		
	}
	
	@Test
	public void TestWhenEnoughFundsToPayInsert() {
		BigDecimal payment = new BigDecimal(100.00);
		
		checkout.payWithDebitOrCredit(payment);
		
		//simulating a transaction with tap
		
		try {
			station.cardReader.insert(credit, "5555");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		assertEquals(payment, cardRead.getPaymentTotal());
		
		bank.setAvailableCreditLimit("12345678", creditLimitBefore);
		
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