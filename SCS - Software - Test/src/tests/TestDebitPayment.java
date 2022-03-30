package tests;

import static org.junit.Assert.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Currency;

import org.junit.*;
import org.lsmr.selfcheckout.*;
import org.lsmr.selfcheckout.devices.*;

import controller.*;
public class TestDebitPayment {
	
	private BigDecimal previousFunds;
	private CustomerCheckout checkoutTest;
    private SelfCheckoutStation station;
    private Card debitCard = new Card("Debit", "12345678", "tester", "123", "1234", true, true);
    private Card creditCard = new Card("Credit", "87654321", "tester", "123", "1234", true, true);
    private Card memberCard = new Card("Member", "55555555", "tester", "123", "1234", true, true);
    private Banknote banknote20 = new Banknote(Currency.getInstance("USD"), 20);
    private BankStub bank = new BankStub();
    
	@Before
	public void setUp() {
		Currency currency = Currency.getInstance("USD");
	     int[] ints = {5, 10, 20, 50};
	     BigDecimal[] decs = {new BigDecimal(".05"), new BigDecimal(".1"), new BigDecimal(".25")};
	     station = new SelfCheckoutStation(currency, ints, decs, 500, 1);
	     checkoutTest = new CustomerCheckout(station);
	     bank.setAvailableCreditLimit("87654321", new BigDecimal(100));
	     bank.setAvailableDebitFunds("12345678", new BigDecimal(100));
	     checkoutTest.getCardLogic().setBank(bank);
	     previousFunds = bank.getAvailableDebitFunds("12345678");
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
	 
	 @After
	 public void after() {
		 BigDecimal newFunds = bank.getAvailableDebitFunds("12345678");
	 }

}