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

	private CardFromCardReader debitCardRead;
	private CardFromCardReader creditCardRead;
	private BigDecimal creditLimitBefore;
	private BigDecimal previousFunds;
	private CustomerCheckout checkoutTest;
	private SelfCheckoutStation station;
	private Card debitCard = new Card("Debit", "12345678", "tester", "123", "2222", true, true);
	private Card creditCard = new Card("Credit", "87654321", "tester", "123", "5555", true, true);
	private Card memberCard = new Card("Member", "55555555", "tester", "123", "1234", true, true);
	private Banknote banknote20 = new Banknote(Currency.getInstance("USD"), 20);
	private BankStub bank = new BankStub();
	private boolean readSuccessful;

	@Before
	public void setup() {
		Currency currency = Currency.getInstance("USD");
		int[] ints = {5, 10, 20, 50};
		BigDecimal[] decs = {new BigDecimal(".05"), new BigDecimal(".1"), new BigDecimal(".25")};

		station = new SelfCheckoutStation(currency, ints, decs, 500, 1);
		checkoutTest = new CustomerCheckout(station, bank);

		bank.setAvailableCreditLimit("87654321", new BigDecimal(1000.00));
		bank.setAvailableDebitFunds("12345678", new BigDecimal(2500.00));
		checkoutTest.getCardLogic().setBank(bank);
		previousFunds = bank.getAvailableDebitFunds("12345678");
		debitCardRead = checkoutTest.getCardLogic();
		debitCardRead.resetPaymentTotal();

		checkoutTest.getCardLogic().setBank(bank);
		creditLimitBefore = bank.getAvailableCreditLimit("87654321");
		creditCardRead = checkoutTest.getCardLogic();
		creditCardRead.resetPaymentTotal();

		readSuccessful = false;
		
		station.banknoteInput.enable();
		station.banknoteOutput.enable();
	}

	/*
	 * Tests that member number is correctly printed on receipt
	 */
	@Test
	public void memberReceipt() throws IOException{
		station.printer.addInk(100);
		station.printer.addPaper(100);
		checkoutTest.useMembershipCard();
		while(!readSuccessful) {
			try {
				station.cardReader.swipe(memberCard);
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
		assertTrue(checkoutTest.confirmPurchase());
	}

	@Test
	public void debitPayWithInsufficientFunds() throws IOException {
		checkoutTest.startPurchase();
		checkoutTest.setAmountOwed(new BigDecimal(112.00));
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
		implementSwipe(debitCard);

		station.cardReader.remove();
		assertEquals(payment, debitCardRead.getPaymentTotal());
	}

	@Test
	public void TestWhenEnoughFundsToPayTapDebit() {
		BigDecimal payment = new BigDecimal(222.22);
		checkoutTest.payWithDebitOrCredit(payment);

		implementTap(debitCard);

		station.cardReader.remove();
		assertEquals(payment, debitCardRead.getPaymentTotal());
	}

	@Test
	public void TestWhenEnoughFundsToPayInsertDebit() {
		BigDecimal payment = new BigDecimal(100.00);
		checkoutTest.payWithDebitOrCredit(payment);

		implementInsertDebit(debitCard);

		station.cardReader.remove();
		assertEquals(payment, debitCardRead.getPaymentTotal());
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
		assertEquals(creditLimitBefore, bank.getAvailableCreditLimit("87654321").add(payment));
		bank.setAvailableCreditLimit("12345678", creditLimitBefore);
	}

	//tests credit card payment via tap
	@Test
	public void TestWhenEnoughFundsToPayTap() {
		BigDecimal payment = new BigDecimal(355.67);
		checkoutTest.payWithDebitOrCredit(payment);
		
		//simulates someone retrying tap if read unsuccessful
		implementTap(creditCard);

		station.cardReader.remove();
		//checks cardReader controller payment total
		assertEquals(payment, creditCardRead.getPaymentTotal());
	}
	
	//tests credit card payment via swipe
	@Test
	public void TestWhenEnoughFundsToPaySwipe() {
		BigDecimal payment = new BigDecimal(789.32);
		checkoutTest.payWithDebitOrCredit(payment);
		
		//if swipe does not read data, simulates customer trying again
		implementSwipe(creditCard);

		station.cardReader.remove();
		assertEquals(payment, creditCardRead.getPaymentTotal());
	}

	//tests credit card payment via insert
	@Test
	public void TestWhenEnoughFundsToPayInsert() {
		BigDecimal payment = new BigDecimal(100.00);
		checkoutTest.payWithDebitOrCredit(payment);

		implementInsertCredit(creditCard);

		station.cardReader.remove();
		assertEquals(payment, creditCardRead.getPaymentTotal());
	}

	//payment should go through when credit card limit available is barely enough
	@Test
	public void TestCloseToCreditLimitBelow() {
		BigDecimal payment = new BigDecimal(999.99);
		checkoutTest.payWithDebitOrCredit(payment);

		implementTap(creditCard);

		station.cardReader.remove();
		//checks cardReader controller payment total
		assertEquals(payment, creditCardRead.getPaymentTotal());
	}

	//payment should not go through if available credit limit is extremely close to enough (but not enough)
	@Test
	public void TestCloseToCreditLimitAbove() {
		BigDecimal payment = new BigDecimal(1000.01);
		checkoutTest.payWithDebitOrCredit(payment);

		implementTap(creditCard);

		station.cardReader.remove();
		//checks cardReader controller payment total
		assertEquals(new BigDecimal(0), creditCardRead.getPaymentTotal());
	}

	//payment should not go through if available credit limit is not enough for the purchase
	@Test
	public void TestWhenNotEnoughFunds() {
		BigDecimal payment = new BigDecimal(1500.00);
		checkoutTest.payWithDebitOrCredit(payment);

		implementTap(creditCard);

		station.cardReader.remove();
		assertEquals(new BigDecimal(0), creditCardRead.getPaymentTotal());
	}

	//card reader should be enabled it card is removed after a successful payment
	@Test
	public void TestWhenCustomerRemovesCardAfterPayment() {
		BigDecimal payment = new BigDecimal(50.00);
		checkoutTest.payWithDebitOrCredit(payment);

		implementInsertCredit(creditCard);

		station.cardReader.remove();
		assertFalse(station.cardReader.isDisabled());	
	}

	//card reader should be disabled when card is not removed after payment is successful
	@Test
	public void TestWhenCustomerDoesNotRemoveCardAfterPayment() {
		BigDecimal payment = new BigDecimal(50.00);
		checkoutTest.payWithDebitOrCredit(payment);

		implementInsertCredit(creditCard);

		assertTrue(station.cardReader.isDisabled());
		station.cardReader.remove();
	}

	/*
	 * Tests to see if regular use case of paying with different methods works
	 */
	@Test
	public void payMix() throws IOException, DisabledException, OverloadException {
		checkoutTest.setAmountOwed(new BigDecimal(80));
		
		checkoutTest.payWithDebitOrCredit(new BigDecimal(30));
		while(!readSuccessful) {
			try {
				station.cardReader.swipe(debitCard);
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
		readSuccessful = false;
		
		checkoutTest.payWithDebitOrCredit(new BigDecimal(30));
		while(!readSuccessful) {
			try {
				station.cardReader.swipe(creditCard);
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
		
		checkoutTest.payWithBankNoteAndCoin(new BigDecimal(20));
		station.banknoteInput.accept(banknote20);
		//station.banknoteInput.accept(banknote20);
		assertTrue(checkoutTest.confirmPurchase());
	
	}
	
	/*
	 * Tests to see if the multi-method payment will fail if a payment method fails
	 */
	@Test
	public void payMixFail() throws IOException, DisabledException, OverloadException {
		bank.setAvailableDebitFunds("12345678", new BigDecimal(10));
		checkoutTest.getCardLogic().setBank(bank);
		checkoutTest.setAmountOwed(new BigDecimal(80));
		
		checkoutTest.payWithDebitOrCredit(new BigDecimal(30));
		while(!readSuccessful) {
			try {
				station.cardReader.swipe(debitCard);
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
		readSuccessful = false;
		
		checkoutTest.payWithDebitOrCredit(new BigDecimal(30));
		while(!readSuccessful) {
			try {
				station.cardReader.swipe(creditCard);
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
		
		checkoutTest.payWithBankNoteAndCoin(new BigDecimal(40));
		station.banknoteInput.accept(banknote20);
		//station.banknoteInput.accept(banknote20);
		
		assertFalse(checkoutTest.confirmPurchase());

	}
	//simulates someone re-inserting the card upon an unsuccessful card read
	public void implementInsertCredit(Card card) {

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
	
	//simulates someone re-inserting the card upon an unsuccessful card read
	public void implementInsertDebit(Card card) {

		while(!readSuccessful) {
			try {
				station.cardReader.insert(card, "2222");
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

	//simulates someone retrying tap if read unsuccessful
	public void implementTap(Card card) {

		while(!readSuccessful) {
			try {
				station.cardReader.tap(card);
				readSuccessful = true;
			} catch (IOException e) {
				/**the CardReader class throws a ChipFailureException for tap but the Card class throws a TapFailureException
				hence why there are both here **/
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

	//if swipe does not read data, simulates customer trying again
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
