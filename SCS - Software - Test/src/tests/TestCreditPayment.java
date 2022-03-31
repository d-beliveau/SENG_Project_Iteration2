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
import org.lsmr.selfcheckout.TapFailureException;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;

import controller.BankStub;
import controller.CardFromCardReader;
import controller.CustomerCheckout;

public class TestCreditPayment {
	
	CardFromCardReader creditCardRead;
	Card creditCard;
	BigDecimal creditLimitBefore;
	BankStub bank;
	
	Currency currency = Currency.getInstance("USD");
	int[] banknoteDenominations = {1, 2, 5, 10};
	BigDecimal[] coinDenominations = {BigDecimal.TEN};
	

	SelfCheckoutStation station = new SelfCheckoutStation(currency, banknoteDenominations, coinDenominations, 10, 2);
	CustomerCheckout checkoutTest;
	
	boolean readSuccessful;
	
	@Before
	public void setUp() {
		bank = new BankStub();
		checkoutTest = new CustomerCheckout(station, bank);
		creditCardRead = checkoutTest.getCardLogic();
		
		station.cardReader.attach(creditCardRead);
		station.cardReader.enable();
		
		creditCard = new Card("Credit", "12345678", "A Person", "123", "5555", true, true);
		bank.setAvailableCreditLimit("12345678", new BigDecimal(1000.00));
		creditLimitBefore = bank.getAvailableCreditLimit("12345678");
		
		creditCardRead.resetPaymentTotal();
		readSuccessful = false;
	}
	
	

	//checking available credit limit after payment
	@Test
	public void TestCreditLimitAfterPurchase() {
		BigDecimal payment = new BigDecimal(500.00);
		checkoutTest.payWithDebitOrCredit(payment);
		//simulates someone retrying tap if read unsuccessful
		implementTap(creditCard);
		
		station.cardReader.remove();
		//checks bank credit limit
		assertEquals(creditLimitBefore, bank.getAvailableCreditLimit("12345678").add(payment));
		bank.setAvailableCreditLimit("12345678", creditLimitBefore);
	}
	

	@Test
	public void TestWhenEnoughFundsToPayTap() {
		BigDecimal payment = new BigDecimal(355.67);
		checkoutTest.payWithDebitOrCredit(payment);
		//simulating a transaction with tap
		
		implementTap(creditCard);
		
		station.cardReader.remove();
		//checks cardReader controller payment total
		assertEquals(payment, creditCardRead.getPaymentTotal());
	}
	
	@Test
	public void TestWhenEnoughFundsToPaySwipe() {
		BigDecimal payment = new BigDecimal(789.32);
		checkoutTest.payWithDebitOrCredit(payment);
		//simulating a transaction with swipe
		//if swipe does not read data, simulates customer trying again
		implementSwipe(creditCard);
		
		station.cardReader.remove();
		assertEquals(payment, creditCardRead.getPaymentTotal());
	}
	
	
	@Test
	public void TestWhenEnoughFundsToPayInsert() {
		BigDecimal payment = new BigDecimal(100.00);
		checkoutTest.payWithDebitOrCredit(payment);
		
		implementInsert(creditCard);
		
		station.cardReader.remove();
		assertEquals(payment, creditCardRead.getPaymentTotal());
	}
	
	
	@Test
	public void TestCloseToCreditLimitBelow() {
		BigDecimal payment = new BigDecimal(999.99);
		checkoutTest.payWithDebitOrCredit(payment);
		
		implementTap(creditCard);
		
		station.cardReader.remove();
		//checks cardReader controller payment total
		assertEquals(payment, creditCardRead.getPaymentTotal());
	}
	
	
	@Test
	public void TestCloseToCreditLimitAbove() {
		BigDecimal payment = new BigDecimal(1000.01);
		checkoutTest.payWithDebitOrCredit(payment);
		
		implementTap(creditCard);
		
		station.cardReader.remove();
		//checks cardReader controller payment total
		assertEquals(new BigDecimal(0), creditCardRead.getPaymentTotal());
	}
	
	
	@Test
	public void TestWhenNotEnoughFunds() {
		BigDecimal payment = new BigDecimal(1500.00);
		checkoutTest.payWithDebitOrCredit(payment);
				
		implementTap(creditCard);
		
		station.cardReader.remove();
		assertEquals(new BigDecimal(0), creditCardRead.getPaymentTotal());
	}
	
	
	@Test
	public void TestWhenCustomerRemovesCardAfterPayment() {
		BigDecimal payment = new BigDecimal(50.00);
		checkoutTest.payWithDebitOrCredit(payment);
		
		implementInsert(creditCard);
		
		station.cardReader.remove();
		assertFalse(station.cardReader.isDisabled());	
	}
	
	
	@Test
	public void TestWhenCustomerDoesNotRemoveCardAfterPayment() {
		BigDecimal payment = new BigDecimal(50.00);
		checkoutTest.payWithDebitOrCredit(payment);
		
		implementInsert(creditCard);
		
		assertTrue(station.cardReader.isDisabled());
		station.cardReader.remove();
	}
	
	public void implementInsert(Card card) {
		
		while(!readSuccessful) {
			try {
				station.cardReader.insert(card, "5555");
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
	}
	
	public void implementTap(Card card) {
		
		while(!readSuccessful) {
			try {
				station.cardReader.tap(card);
				readSuccessful = true;
			} catch (IOException e) {
				if(e instanceof TapFailureException || e instanceof ChipFailureException) {
					continue;
				}
				else {
					e.printStackTrace();
					break;
				}
			}
		}
	}
	
	public void implementSwipe(Card card) {
		
		while(!readSuccessful) {
			try {
				station.cardReader.swipe(creditCard);
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
	}
	
}