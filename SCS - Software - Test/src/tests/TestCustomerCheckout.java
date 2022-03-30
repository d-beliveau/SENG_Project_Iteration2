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

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.lsmr.selfcheckout.Banknote;
import org.lsmr.selfcheckout.devices.DisabledException;
import org.lsmr.selfcheckout.devices.OverloadException;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;
import org.lsmr.selfcheckout.devices.SimulationException;

import controller.CustomerCheckout;
import controller.PayCash;
import controller.ScanItem;

import java.math.BigDecimal;
import java.util.Currency;

// Test suite for CustomerCheckout
public class TestCustomerCheckout {
    private CustomerCheckout checkoutTest;
    private SelfCheckoutStation station;
    private Currency currency;

    @Before
    public void setup() {
  
        currency = Currency.getInstance("USD");
        int[] ints = {5, 10, 20, 50};
        BigDecimal[] decs = {new BigDecimal(".05"), new BigDecimal(".1"), new BigDecimal(".25")};
        station = new SelfCheckoutStation(currency, ints, decs, 500, 1);
        checkoutTest = new CustomerCheckout(station);
    }
    
    /*Test whether certain devices are disabled 
      before customer uses checkout station */
    @Test
    public void testBeforeStartPurchase() {
       
            checkoutTest.beforeStartPurchase();
            assertTrue(station.mainScanner.isDisabled());
            assertTrue(station.cardReader.isDisabled());
            assertTrue(station.coinSlot.isDisabled());
            assertTrue(station.banknoteInput.isDisabled());
        
    }

    /*Test whether certain devices are enabled when 
    before customer chooses to use a checkout station */
    @Test
    public void testStartPurchase() {
    	 checkoutTest.startPurchase();
    	 assertFalse(station.mainScanner.isDisabled());
         assertTrue(station.cardReader.isDisabled());
         assertTrue(station.coinSlot.isDisabled());
         assertTrue(station.banknoteInput.isDisabled());
    }
    
    /*Test whether the banknote and coin slot are the only enabled
     * device when customer choose form of payment to be cash */
    @Test
    public void testPayWithBankNoteAndCoin(){
	   	 checkoutTest.payWithBankNoteAndCoin();
		 assertTrue(station.mainScanner.isDisabled());
	     assertTrue(station.cardReader.isDisabled());
	     assertFalse(station.coinSlot.isDisabled());
	     assertFalse(station.banknoteInput.isDisabled());	
    }
    
    @Test
    public void testUseMembershipCard() {
    	checkoutTest.useMembershipCard();
	    assertFalse(station.cardReader.isDisabled());
    }
    
    /*Test whether the card reader is the only enabled device when
     * customer choose form of payment to be debit or credit card */
    @Test 
    public void testPayWithDebitOrCredit() {
    	 checkoutTest.payWithDebitOrCredit();
		 assertTrue(station.mainScanner.isDisabled());
	     assertFalse(station.cardReader.isDisabled());
	     assertTrue(station.coinSlot.isDisabled());
	     assertTrue(station.banknoteInput.isDisabled());
    }
    
    /* Test whether when a user decides to confirm they have completed 
     * all purchase that all relevant devices are disabled */
    @Test
    public void testConfirmPurchase() throws DisabledException, OverloadException {
   
    	BigDecimal moneyOwed;
    	
    	moneyOwed = new BigDecimal(20);
    	assertFalse(checkoutTest.confirmPurchase(moneyOwed));
    	
    	
     	moneyOwed = new BigDecimal(0);
    	assertTrue(checkoutTest.confirmPurchase(moneyOwed));
    	
    	
    	moneyOwed = new BigDecimal(-1);
    	assertTrue(checkoutTest.confirmPurchase(moneyOwed));
    	
    	//After all items paid for disable all relevant devices
		 assertTrue(station.mainScanner.isDisabled());
	     assertTrue(station.cardReader.isDisabled());
	     assertTrue(station.coinSlot.isDisabled());
	     assertTrue(station.banknoteInput.isDisabled());
    	
    }
    
    
    /*Test whether you can add more item to scan*/
    @Test
    public void testAddItemToScanner() {
		 assertFalse(station.mainScanner.isDisabled());
	     assertTrue(station.cardReader.isDisabled());
	     assertTrue(station.coinSlot.isDisabled());
	     assertTrue(station.banknoteInput.isDisabled());
    }
    
    
}