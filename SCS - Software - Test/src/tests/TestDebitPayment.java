package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Currency;

import org.junit.Before;
import org.junit.Test;
import org.lsmr.selfcheckout.Card;
import org.lsmr.selfcheckout.ChipFailureException;
import org.lsmr.selfcheckout.MagneticStripeFailureException;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;

import controller.BankStub;
import controller.CardFromCardReader;
import controller.CustomerCheckout;

import org.junit.*;
import org.lsmr.selfcheckout.*;
import org.lsmr.selfcheckout.devices.*;

import controller.*;

public class TestDebitPayment {
	
	private CardFromCardReader cardRead;
	private BigDecimal previousFunds;
	private CustomerCheckout checkoutTest;
    private SelfCheckoutStation station;
    private Card debitCard = new Card("Debit", "12345678", "tester", "123", "2222", true, true);
    private Card creditCard = new Card("Credit", "87654321", "tester", "123", "1234", true, true);
    private Card memberCard = new Card("Member", "55555555", "tester", "123", "1234", true, true);
    private Banknote banknote20 = new Banknote(Currency.getInstance("USD"), 20);
    private BankStub bank = new BankStub();
    private boolean readSuccessful;
    
	@Before
	public void setUp() {
		Currency currency = Currency.getInstance("USD");
	    int[] ints = {5, 10, 20, 50};
	    BigDecimal[] decs = {new BigDecimal(".05"), new BigDecimal(".1"), new BigDecimal(".25")};
	    station = new SelfCheckoutStation(currency, ints, decs, 500, 1);
	    checkoutTest = new CustomerCheckout(station, bank);
	    bank.setAvailableCreditLimit("87654321", new BigDecimal(100));
	    bank.setAvailableDebitFunds("12345678", new BigDecimal(2500));
	    checkoutTest.getCardLogic().setBank(bank);
	    previousFunds = bank.getAvailableDebitFunds("12345678");
		cardRead = checkoutTest.getCardLogic();
		cardRead.resetPaymentTotal();
		readSuccessful = false;
	}
	
	 @Test
	 public void debitFundsAvailable() throws IOException {
		 checkoutTest.startPurchase();
		 checkoutTest.setAmountOwed(new BigDecimal(100));
		 checkoutTest.payWithDebitOrCredit(new BigDecimal(100));
		 station.cardReader.tap(debitCard);

		 assertTrue(checkoutTest.confirmPurchase());
	 }
	 
	 @Test
	 public void debitFundsNotAvailable() throws IOException {
		 checkoutTest.startPurchase();
		 checkoutTest.setAmountOwed(new BigDecimal(100));
		 checkoutTest.payWithDebitOrCredit(new BigDecimal(50));
		 station.cardReader.tap(debitCard);

		 assertFalse(checkoutTest.confirmPurchase());
	 }
	 
	 @Test
	 public void fundsAfterPurchase() {
		 BigDecimal payment = new BigDecimal(50.00);
		 checkoutTest.payWithDebitOrCredit(payment);
			
		 try {
			 station.cardReader.tap(debitCard);
		 } catch (IOException e) {
			 e.printStackTrace();
		 }
	
		 BigDecimal newFunds = previousFunds.subtract(payment);
		 assertEquals(newFunds,bank.getAvailableDebitFunds("12345678"));
	 }
	 
	@Test
	public void TestWhenEnoughFundsToPaySwipeDebit() {
		BigDecimal payment = new BigDecimal(789.32);
		checkoutTest.payWithDebitOrCredit(payment);
		//simulating a transaction with swipe
		//if swipe does not read data, simulates customer trying again
		while(!readSuccessful) {
			try {
				station.cardReader.swipe(debitCard);
				readSuccessful = true;
			} catch (IOException e) {
				if(e instanceof MagneticStripeFailureException) {
					continue;
				}
				else {
					e.printStackTrace();
					break;
				}
			}
		}
			
		station.cardReader.remove();
		assertEquals(payment, cardRead.getPaymentTotal());
	}
	
	@Test
	public void TestWhenEnoughFundsToPayTapDebit() {
		BigDecimal payment = new BigDecimal(222.22);
		checkoutTest.payWithDebitOrCredit(payment);
		//simulating a transaction with swipe
		//if swipe does not read data, simulates customer trying again
		while(!readSuccessful) {
			try {
				station.cardReader.tap(debitCard);
				readSuccessful = true;
			} catch (IOException e) {
				if(e instanceof MagneticStripeFailureException) {
					continue;
				}
				else {
					e.printStackTrace();
					break;
				}
			}
		}
		
		station.cardReader.remove();
		assertEquals(payment, cardRead.getPaymentTotal());
	}
	
	@Test
	public void TestWhenEnoughFundsToPayInsertDebit() {
		BigDecimal payment = new BigDecimal(100.00);
		checkoutTest.payWithDebitOrCredit(payment);
		
		//simulating a transaction with tap
		while(!readSuccessful) {
			try {
				station.cardReader.insert(debitCard, "2222");
				readSuccessful = true;
			} catch (IOException e) {
				if(e instanceof ChipFailureException) {
					continue;
				}
				else {
					e.printStackTrace();
					break;
				}
			}
		}
		
		station.cardReader.remove();
		assertEquals(payment, cardRead.getPaymentTotal());
	}
	 
	 @After
	 public void after() {
		 BigDecimal newFunds = bank.getAvailableDebitFunds("12345678");
	 }

}