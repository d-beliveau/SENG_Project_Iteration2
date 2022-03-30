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
	}

	/*
	 * Tests that member number is correctly printed on receipt
	 */
	@Test
	public void memberReceipt() throws IOException{
		checkoutTest.useMembershipCard();
		station.cardReader.swipe(memberCard);
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

		implementInsertCredit(creditCard);

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

		implementInsertCredit(creditCard);

		station.cardReader.remove();
		assertFalse(station.cardReader.isDisabled());	
	}


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
	 * Tests to see if the multi-method payment will fail if a payment method fails
	 */
	@Test
	public void payMixFail() throws IOException, DisabledException, OverloadException {
		bank.setAvailableDebitFunds("12345678", new BigDecimal(10));
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

	/*
	 @After
	 public void after() {
		 BigDecimal newFunds = bank.getAvailableDebitFunds("12345678");
		 BigDecimal remainingLimit = bank.getAvailableCreditLimit("87654321");
	 } */
}
