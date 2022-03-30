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
	
	CardFromCardReader cardRead;
	Card credit;
	BigDecimal creditLimitBefore;
	BankStub bank;
	
	Currency currency = Currency.getInstance("CAD");
	int[] banknoteDenominations = {1, 2, 5, 10};
	BigDecimal[] coinDenominations = {BigDecimal.TEN};
	

	
	SelfCheckoutStation station = new SelfCheckoutStation(currency, banknoteDenominations, coinDenominations, 10, 2);
	CustomerCheckout checkout;
	
	boolean readSuccessful;
	
	@Before
	public void setUp() {
		bank = new BankStub();
		checkout = new CustomerCheckout(station, bank);
		cardRead = checkout.getCardLogic();
		
		station.cardReader.attach(cardRead);
		station.cardReader.enable();
		
		credit = new Card("Credit", "12345678", "A Person", "123", "5555", true, true);
		bank.setAvailableCreditLimit("12345678", new BigDecimal(1000.00));
		creditLimitBefore = bank.getAvailableCreditLimit("12345678");
		
		cardRead.resetPaymentTotal();
		readSuccessful = false;
	}
	
	

	//checking available credit limit after payment
	@Test
	public void TestCreditLimitAfterPurchase() {
		BigDecimal payment = new BigDecimal(500.00);
		checkout.payWithDebitOrCredit(payment);
		//simulates someone retrying tap if read unsuccessful
		while(!readSuccessful) {
			try {
				station.cardReader.tap(credit);
				readSuccessful = true;
			} catch (IOException e) {
				
				if(e instanceof TapFailureException) {
					continue;
				}
				else { 
					e.printStackTrace();
					break;
				}
			}
		}
		
		station.cardReader.remove();
		//checks bank credit limit
		assertEquals(creditLimitBefore, bank.getAvailableCreditLimit("12345678").add(payment));
		bank.setAvailableCreditLimit("12345678", creditLimitBefore);
	}
	

	@Test
	public void TestWhenEnoughFundsToPayTap() {
		BigDecimal payment = new BigDecimal(355.67);
		checkout.payWithDebitOrCredit(payment);
		//simulating a transaction with tap
		
		while(!readSuccessful) {
			try {
				station.cardReader.tap(credit);
				readSuccessful = true;
			} catch (IOException e) {
				if(e instanceof TapFailureException) {
					continue;
				}
				else {
					e.printStackTrace();
					break;
				}
			}
		}
		
		station.cardReader.remove();
		//checks cardReader controller payment total
		assertEquals(payment, cardRead.getPaymentTotal());
	}
	
	@Test
	public void TestWhenEnoughFundsToPaySwipe() {
		BigDecimal payment = new BigDecimal(789.32);
		checkout.payWithDebitOrCredit(payment);
		//simulating a transaction with swipe
		//if swipe does not read data, simulates customer trying again
		while(!readSuccessful) {
			try {
				station.cardReader.swipe(credit);
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
	public void TestWhenEnoughFundsToPayInsert() {
		BigDecimal payment = new BigDecimal(100.00);
		checkout.payWithDebitOrCredit(payment);
		
		//simulating a transaction with tap
		while(!readSuccessful) {
			try {
				station.cardReader.insert(credit, "5555");
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
	
	
	@Test
	public void TestCloseToCreditLimitBelow() {
		BigDecimal payment = new BigDecimal(999.99);
		checkout.payWithDebitOrCredit(payment);
		
		//simulating a transaction with tap
		while(!readSuccessful) {
			try {
				station.cardReader.tap(credit);
				readSuccessful = true;
			} catch (IOException e) {
				if(e instanceof TapFailureException) {
					continue;
				}
				else {
					e.printStackTrace();
					break;
				}
			}
		}
		
		station.cardReader.remove();
		//checks cardReader controller payment total
		assertEquals(payment, cardRead.getPaymentTotal());
	}
	
	
	@Test
	public void TestCloseToCreditLimitAbove() {
		BigDecimal payment = new BigDecimal(1000.01);
		checkout.payWithDebitOrCredit(payment);
		
		//simulating a transaction with tap
		while(!readSuccessful) {
			try {
				station.cardReader.tap(credit);
				readSuccessful = true;
			} catch (IOException e) {
				if(e instanceof TapFailureException) {
					continue;
				}
				else {
					e.printStackTrace();
					break;
				}
			}
		}
		
		station.cardReader.remove();
		//checks cardReader controller payment total
		assertEquals(new BigDecimal(0), cardRead.getPaymentTotal());
	}
	
	
	@Test
	public void TestWhenNotEnoughFunds() {
		BigDecimal payment = new BigDecimal(1500.00);
		checkout.payWithDebitOrCredit(payment);
				
		while(!readSuccessful) {
			try {
				station.cardReader.tap(credit);
				readSuccessful = true;
			} catch (IOException e) {
				if(e instanceof TapFailureException) {
					continue;
				}
				else {
					e.printStackTrace();
					break;
				}
			}
		}
		
		station.cardReader.remove();
		assertEquals(new BigDecimal(0), cardRead.getPaymentTotal());
	}
	
	
	@Test
	public void TestWhenCustomerRemovesCardAfterPayment() {
		BigDecimal payment = new BigDecimal(50.00);
		checkout.payWithDebitOrCredit(payment);
		
		while(!readSuccessful) {
			try {
				station.cardReader.insert(credit, "5555");
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
		assertFalse(station.cardReader.isDisabled());	
	}
	
	
	@Test
	public void TestWhenCustomerDoesNotRemoveCardAfterPayment() {
		BigDecimal payment = new BigDecimal(50.00);
		checkout.payWithDebitOrCredit(payment);
		
		while(!readSuccessful) {
			try {
				station.cardReader.insert(credit, "5555");
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
		
		assertTrue(station.cardReader.isDisabled());
		station.cardReader.remove();
	}
	
	
}