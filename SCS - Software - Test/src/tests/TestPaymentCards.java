package tests;

import static org.junit.Assert.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Currency;

import org.junit.*;
import org.lsmr.selfcheckout.*;
import org.lsmr.selfcheckout.devices.*;

import controller.*;

public class TestPaymentCards {
	private CustomerCheckout checkoutTest;
    private SelfCheckoutStation station;
    private Card debitCard = new Card("Debit", "12345678", "tester", "123", "1234", true, true);
    private Card creditCard = new Card("Credit", "87654321", "tester", "123", "1234", true, true);
    private Card memberCard = new Card("Member", "55555555", "tester", "123", "1234", true, true);
    private Banknote banknote20 = new Banknote(Currency.getInstance("USD"), 20);
    private BankStub bank = new BankStub();
	
	 @Before
	 public void setup() {
		 Currency currency = Currency.getInstance("USD");
	     int[] ints = {5, 10, 20, 50};
	     BigDecimal[] decs = {new BigDecimal(".05"), new BigDecimal(".1"), new BigDecimal(".25")};
	     station = new SelfCheckoutStation(currency, ints, decs, 500, 1);
	     checkoutTest = new CustomerCheckout(station);
	     bank.setAvailableCreditFunds("87654321", new BigDecimal(100));
	     bank.setAvailableDebitFunds("12345678", new BigDecimal(100));
	     checkoutTest.getCardLogic().setBank(bank);
	 }
	 
	 /*
	  * Tests that member number is correctly printed on receipt
	  */
	 @Test
	 public void memberReceipt() {
		 
	 }
	 
	 /*
	  * Tests to see if regular use case of paying with different methods works
	  */
	 @Test
	 public void payMix() throws IOException, DisabledException, OverloadException {
		 checkoutTest.setAmountOwed(new BigDecimal(100));
		 checkoutTest.payWithDebitOrCredit(new BigDecimal(30));
		 station.cardReader.swipe(debitCard);
		 checkoutTest.payWithDebitOrCredit(new BigDecimal(30));
		 station.cardReader.swipe(creditCard);
		 checkoutTest.payWithBankNoteAndCoin(new BigDecimal(40));
		 station.banknoteInput.accept(banknote20);
		 station.banknoteInput.accept(banknote20);
		 assertTrue(checkoutTest.confirmPurchase());
	 }
	 
	 /*
	  * Tests to see if the payment will fail if a payment method fails
	  */

	 @Test
	 public void payMixFail() throws IOException, DisabledException, OverloadException {
		 bank.setAvailableDebitFunds("87654321", new BigDecimal(10));
		 checkoutTest.getCardLogic().setBank(bank);
		 checkoutTest.setAmountOwed(new BigDecimal(100));
		 checkoutTest.payWithDebitOrCredit(new BigDecimal(30));
		 station.cardReader.swipe(debitCard);
		 checkoutTest.payWithDebitOrCredit(new BigDecimal(30));
		 station.cardReader.swipe(creditCard);
		 checkoutTest.payWithBankNoteAndCoin(new BigDecimal(40));
		 station.banknoteInput.accept(banknote20);
		 station.banknoteInput.accept(banknote20);
		 assertFalse(checkoutTest.confirmPurchase());
		 
	 }
}
