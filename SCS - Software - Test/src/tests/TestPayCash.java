package tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import org.junit.*;
import org.lsmr.selfcheckout.Banknote;
import org.lsmr.selfcheckout.Coin;
import org.lsmr.selfcheckout.devices.DisabledException;
import org.lsmr.selfcheckout.devices.OverloadException;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;

import controller.PayCash;

public class TestPayCash {
	
	// Principle fields
	private PayCash objectUnderTest;
	private Currency c = Currency.getInstance("CAD");
	private BigDecimal[] coinDenominations = {new BigDecimal (0.01), new BigDecimal(0.05), new BigDecimal(0.10), new BigDecimal(0.25)};
	private int[] banknoteDenominations = {1, 5, 10, 20, 50, 100};
	SelfCheckoutStation scs;
	
	// Specific banknotes and coins
	private Banknote oneBanknote = new Banknote(c, 1);
	private Banknote fiveBanknote = new Banknote(c, 5);
	private Banknote tenBanknote = new Banknote(c, 10);
	private Banknote twentyBanknote = new Banknote(c, 20);
	private Banknote fiftyBanknote = new Banknote(c, 50);
	private Banknote hundredBanknote = new Banknote(c, 100);
	private Coin penny = new Coin(c, coinDenominations[0]);
	private Coin nickel = new Coin(c, coinDenominations[1]);
	private Coin dime = new Coin(c, coinDenominations[2]);
	private Coin quarter = new Coin(c, coinDenominations[3]);
	
	
	// Setup a PayCash object "objectUnderTest" to run tests on
	@Before
	public void setup() {
		BigDecimal b = new BigDecimal(50);
		scs = new SelfCheckoutStation(c, banknoteDenominations, coinDenominations, 10000, 1);
		objectUnderTest = new PayCash(scs, b);	
	}
	
	
	@Test (timeout = 500)
	public void testCheckEnough() {
		BigDecimal ten = new BigDecimal(10.00);
		BigDecimal twenty = new BigDecimal(20.00);
		objectUnderTest.setTotalPayment(ten);
		objectUnderTest.setAmountOwed(ten);
		assertTrue(objectUnderTest.checkEnough());	
		
		objectUnderTest.setTotalPayment(twenty);
		objectUnderTest.setAmountOwed(ten);
		assertTrue(objectUnderTest.checkEnough());
		
		objectUnderTest.setTotalPayment(ten);
		objectUnderTest.setAmountOwed(twenty);
		assertFalse(objectUnderTest.checkEnough());
	}
	
	
	@Test (timeout = 500)
	public void testDetermineChagne() {
		BigDecimal paid = new BigDecimal(40.00);
		BigDecimal cost = new BigDecimal(21.59);
		objectUnderTest.determineChange(paid, cost);
		int[] expectedBills = {1, 1, 1, 5, 10};
		double[] expectedCoins = {0.01, 0.05, 0.10, 0.25};
		
		Boolean b = true;
		for (int i = 0; i < objectUnderTest.billsDue.size() ; i++) {
			if (objectUnderTest.billsDue.get(i) != expectedBills[i]) {
				b = false;
				break;
			}
		}
		
		Boolean c = true;
		for (int i = 0; i < objectUnderTest.coinsDue.size() ; i++) {
			if (objectUnderTest.coinsDue.get(i).doubleValue() != expectedCoins[i]) {;
				c = false;
				break;
			}
		}
		
		assertTrue(b);
		assertTrue(c);
		
	}
	
	
	@Test (timeout = 500)
	public void testSeparateCoinsFromBills() {
		BigDecimal change = new BigDecimal(18.41);
		ArrayList<BigDecimal> result = objectUnderTest.separateCoinsFromBills(change);
		
		Boolean a = false;
		if ((result.get(0).intValue() == 18) & (result.get(1).setScale(2, RoundingMode.HALF_EVEN).doubleValue() == 0.41)) {
			a = true;
		}
		
		assertTrue(a);
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
		int var = 169;
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
	public void testBanknoteAdded() throws OverloadException, DisabledException{

		BigDecimal reset = new BigDecimal(0);
		
		objectUnderTest.setTotalPayment(reset);
		scs.banknoteValidator.accept(twentyBanknote);
		Assert.assertFalse(objectUnderTest.checkEnough());
		Assert.assertTrue(objectUnderTest.getTotalPayment().setScale(2, RoundingMode.HALF_EVEN).equals(BigDecimal.valueOf(twentyBanknote.getValue()).setScale(2, RoundingMode.HALF_EVEN)));
	}
	
	@Test
	public void testCoinAdded() throws OverloadException, DisabledException{

		BigDecimal reset = new BigDecimal(0);
		
		objectUnderTest.setTotalPayment(reset);
		scs.coinValidator.accept(quarter);
		scs.coinStorage.accept(quarter);
		Assert.assertFalse(objectUnderTest.checkEnough());
		Assert.assertTrue(objectUnderTest.getTotalPayment().equals(quarter.getValue()));
	}
	
	@Test
	public void testSetAmountOwed() {
		
		BigDecimal amount = new BigDecimal(50);
		
		objectUnderTest.setAmountOwed(amount);
		Assert.assertTrue(amount.equals(objectUnderTest.getAmountOwed()));
	}

	
	@Test
	public void testInserted() {
		objectUnderTest.getInsertedCoinValue();
		objectUnderTest.getInsertedNoteValue();
	}

}