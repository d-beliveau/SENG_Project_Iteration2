/******************************************************************************
Program Authors:
    Dane Beliveau (Student ID: 30131121)
    Jesse Desmarais (Student ID: 00292117)
    Ekhonmu Egbase (Student ID: 30102937)
    Junyi Li (Student ID: 30113375)
    Richi Patel (Student ID: 30125178)
    Kevin Van (Student ID: 30087130)
E-mails:
    dane.beliveau@ucalgary.ca
    jesse.desmarais@ucalgary.ca
    ekhonmu.egbase@ucalgary.ca
    junyi.li@ucalgary.ca
    richi.patel@ucalgary.ca
    kevin.van@ucalgary.ca
Class: SENG 300
Instructor: Robert Walker
Date: 20 March 2022
Assignment: Project, Iteration 01
******************************************************************************/

package tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.Random;
import org.junit.*;
import org.lsmr.selfcheckout.Banknote;
import org.lsmr.selfcheckout.Coin;
import org.lsmr.selfcheckout.devices.DisabledException;
import org.lsmr.selfcheckout.devices.OverloadException;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;

import controller.PayCash;

public class TestPayCash {

	private PayCash objectUnderTest;
	private Currency c = Currency.getInstance("CAD");
	private BigDecimal[] coinDenominations = {new BigDecimal (0.01), new BigDecimal(0.05), new BigDecimal(0.10), new BigDecimal(0.25)};
	private int[] banknoteDenominations = {1, 5, 10, 20, 50, 100};
	Random rand = new Random();
	SelfCheckoutStation scs;
	private Banknote oneBanknote = new Banknote(c, 1);
	private Banknote[] oneBanknotes = createBanknoteArray(oneBanknote);
	private Banknote fiveBanknote = new Banknote(c, 5);
	private Banknote[] fiveBanknotes = createBanknoteArray(fiveBanknote);
	private Banknote tenBanknote = new Banknote(c, 10);
	private Banknote[] tenBanknotes = createBanknoteArray(tenBanknote);
	private Banknote twentyBanknote = new Banknote(c, 20);
	private Banknote[] twentyBanknotes = createBanknoteArray(twentyBanknote);
	private Banknote fiftyBanknote = new Banknote(c, 50);
	private Banknote[] fiftyBanknotes = createBanknoteArray(fiftyBanknote);
	private Banknote hundredBanknote = new Banknote(c, 100);
	private Banknote[] hundredBanknotes = createBanknoteArray(hundredBanknote);
	private Banknote[][] banknotes = {oneBanknotes, fiveBanknotes, tenBanknotes, twentyBanknotes, fiftyBanknotes, hundredBanknotes};
	private Coin penny = new Coin(c, coinDenominations[0]);
	private Coin[] pennies = createCoinArray(penny);
	private Coin nickel = new Coin(c, coinDenominations[1]);
	private Coin[] nickels = createCoinArray(nickel);
	private Coin dime = new Coin(c, coinDenominations[2]);
	private Coin[] dimes = createCoinArray(dime);
	private Coin quarter = new Coin(c, coinDenominations[3]);
	private Coin[] quarters = createCoinArray(quarter);
	private Coin[][] coins = {pennies, nickels, dimes, quarters};
	
	
	// Setup a Self Checkout Station and PayCash object to run tests with and on
	@Before
	public void setup() {
		BigDecimal b = new BigDecimal(50);
		scs = new SelfCheckoutStation(c, banknoteDenominations, coinDenominations, 10000, 1);
		objectUnderTest = new PayCash(scs, b);
		
	}
	
	@Test (timeout = 500)
	public void testCheckEnough() {
		BigDecimal one = new BigDecimal(10.00);
		BigDecimal two = new BigDecimal(20.00);
		assertTrue(objectUnderTest.checkEnough(two, one));
		assertTrue(objectUnderTest.checkEnough(one, one));
		assertFalse(objectUnderTest.checkEnough(one, two));
	}
	
	
	@Test (timeout = 500)
	public void testCalcCoinsChange() {
		boolean equals = false;		
		BigDecimal provided = new BigDecimal(1.05);
		ArrayList<BigDecimal> expected = new ArrayList<BigDecimal>(Arrays.asList(new BigDecimal(0.25), new BigDecimal(0.25),
				new BigDecimal(0.25), new BigDecimal(0.25), new BigDecimal(0.05)));
		ArrayList<BigDecimal> change = objectUnderTest.calcCoinsChange(provided);
		
		Collections.sort(expected);
		Collections.sort(change);
		
		for(int i = 0; i < expected.size(); i++) {
			
			if(expected.get(i).equals(change.get(i))) {
				
				equals = true;
			}
			else {
				
				equals = false;
			}
		}
		
		Assert.assertTrue(equals);
	}
	
	
	@Test (timeout = 500)
	public void testCalcBillsChange() {
		// Add RNG for var
		int var = rand.nextInt(501);
		BigDecimal dollars = new BigDecimal(var);
		ArrayList<Integer> result = objectUnderTest.calcBillsChange(dollars);
		
		int sum = 0;
		for (int bill : result) {
			sum += bill;
		}
		
		Boolean v = false;
		if (var == sum) {
			v = true;
		}
		
		assertTrue(v);
	}
	
	
	@Test
	public void testValidBanknoteInserted() throws DisabledException{

		scs.banknoteValidator.accept(twentyBanknote);
		Assert.assertTrue(objectUnderTest.getInsertedNoteValue() == twentyBanknote.getValue());
	}
	
	@Test
	public void testValidCoinInserted() throws DisabledException{

		scs.coinValidator.accept(quarter);
		Assert.assertTrue(objectUnderTest.getInsertedCoinValue().setScale(2, RoundingMode.HALF_EVEN).equals(quarter.getValue().setScale(2, RoundingMode.HALF_EVEN)));
	}
	
	@Test
	public void testBanknoteAdded() throws OverloadException, DisabledException{

		BigDecimal reset = new BigDecimal(0);
		
		objectUnderTest.setTotalPayment(reset);
		scs.banknoteValidator.accept(twentyBanknote);
		Assert.assertFalse(objectUnderTest.checkEnough(objectUnderTest.getTotalPayment(), objectUnderTest.getAmountOwed()));
		Assert.assertTrue(objectUnderTest.getTotalPayment().setScale(2, RoundingMode.HALF_EVEN).equals(BigDecimal.valueOf(twentyBanknote.getValue()).setScale(2, RoundingMode.HALF_EVEN)));
	}
	
	@Test
	public void testCoinAdded() throws OverloadException, DisabledException{

		BigDecimal reset = new BigDecimal(0);
		
		objectUnderTest.setTotalPayment(reset);
		scs.coinValidator.accept(quarter);
		scs.coinStorage.accept(quarter);
		Assert.assertFalse(objectUnderTest.checkEnough(objectUnderTest.getTotalPayment(), objectUnderTest.getAmountOwed()));
		Assert.assertTrue(objectUnderTest.getTotalPayment().equals(quarter.getValue()));
	}
	
	@Test
	public void testSetAmountOwed() {
		
		BigDecimal amount = new BigDecimal(50);
		
		objectUnderTest.setAmountOwed(amount);
		Assert.assertTrue(amount.equals(objectUnderTest.getAmountOwed()));
	}
	
	@Test
	public void testValidBanknoteDetected() throws DisabledException{
		
		scs.banknoteValidator.accept(tenBanknote);
		Assert.assertTrue(objectUnderTest.getInsertedNoteValue() == tenBanknote.getValue());
	}
	
	@Test 
	public void testValidCoinDetected() throws DisabledException{
		
		scs.coinValidator.accept(dime);
		Assert.assertTrue(objectUnderTest.getInsertedCoinValue().equals(dime.getValue()));
	}
	
	
	@Test (timeout = 500)
	public void testObserverMethods() {
		objectUnderTest.enabled(null);
		objectUnderTest.disabled(null);
		objectUnderTest.invalidCoinDetected(null);
		objectUnderTest.invalidBanknoteDetected(null);
		objectUnderTest.banknotesLoaded(null);
		objectUnderTest.banknotesUnloaded(null);
		objectUnderTest.coinsLoaded(null);
		objectUnderTest.coinsUnloaded(null);
		objectUnderTest.banknotesFull(null);
		objectUnderTest.coinsFull(null);
	}
	
	// HELPER METHODS
	
	private Banknote[] createBanknoteArray(Banknote note) {
		
		Banknote[] array = new Banknote[100];
		for(int i = 0; i < array.length; i++) {
			
			array[i] = note;
		}
		
		return array;
	}
	
	private Coin[] createCoinArray(Coin coin) {
		
		Coin[] array = new Coin[100];
		for(int i = 0; i < array.length; i++) {
			
			array[i] = coin;
		}
		
		return array;
	}

//*** KEEP THESE METHODS FOR LATER IMPLENTATIONS***//
//*** PLEASE DON'T DELETE YET ***//
	
//	private Banknote[] createGiantNoteArray(Banknote note) {
//		
//		Banknote[] array = new Banknote[1000];
//		for(int i = 0; i < array.length; i++) {
//			
//			array[i] = note;
//		}
//		
//		return array;
//	}
	
//	// Fails due to decimal arithmetic problems
//	@Test (timeout = 500)
//	public void testSeparateCoinsFromBills() {
//		BigDecimal dollars = new BigDecimal(rand.nextInt(501));
//		BigDecimal cents = (new BigDecimal(rand.nextDouble())).setScale(2, RoundingMode.FLOOR);
//		BigDecimal total = dollars.add(cents);
//		
//		ArrayList<BigDecimal> result = objectUnderTest.separateCoinsFromBills(total);
//		BigDecimal result_total = result.get(0).add(result.get(1));
//		
//		Boolean v = false;
//		if (total.compareTo(result_total) == 0) {
//			v = true;
//		}
//		
//		assertTrue(v);
//	}
	
//	// Does not completely cover getNoteKey(), work in progress
//	@Test (timeout = 500)
//	public void testGetNoteKey() {
//		BanknoteDispenser bnd = scs.banknoteDispensers.get(banknoteDenominations[2]);
//		objectUnderTest.banknotesEmpty(bnd);
//	}
//	
//	
//	// Does not completely cover getCoinKey(), work in progress
//	@Test (timeout = 500)
//	public void testGetCoinKey() {
//		CoinDispenser cd = scs.coinDispensers.get(coinDenominations[2]);
//		objectUnderTest.coinsEmpty(cd);
//	}
	
//	@Test
//	public void testDeliverChange() {
//		
//		BigDecimal cost = new BigDecimal(55.25);
//		BigDecimal payed = new BigDecimal(60.00);
//		BigDecimal zero = new BigDecimal(0);
//		BigDecimal change = payed.subtract(cost);
//		int iterator = 0;
//		
//		for(BanknoteDispenser dispenser : scs.banknoteDispensers.values()) {
//			
//			try {
//				dispenser.load(banknotes[iterator]);
//				iterator++;
//			}
//			catch(OverloadException e) {}
//		}
//		
//		iterator = 0;
//		
//		for(CoinDispenser dispenser : scs.coinDispensers.values()) {
//			
//			try {
//				dispenser.load(coins[iterator]);
//				iterator++;
//			}
//			catch(OverloadException e) {}
//		}
//		
//		BigDecimal result = objectUnderTest.deliverChange(cost, payed);
//		
//		Assert.assertTrue(result.equals(zero));
//	}
	
//	@Rule 
//	public ExpectedException exceptionRule = ExpectedException.none();
//	
//	@Test
//	public void testBanknotesFull() throws OverloadException, DisabledException{
//		
//		exceptionRule.expect(OverloadException.class);
//		exceptionRule.expectMessage("This terminal is unable accept more bank notes. Bank note storage full.");
//		scs.banknoteDispensers.get(5).load(createGiantNoteArray(fiveBanknote));
//		scs.banknoteValidator.accept(fiveBanknote);
//	}
//	
//	@Test (expected = OverloadException.class)
//	public void testBanknotesFull() throws OverloadException, DisabledException{
//		
//		scs.banknoteDispensers.get(5).load(createGiantNoteArray(fiveBanknote));
//		scs.banknoteValidator.accept(fiveBanknote);
//	}
}