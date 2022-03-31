package tests;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.lsmr.selfcheckout.Banknote;
import org.lsmr.selfcheckout.Barcode;
import org.lsmr.selfcheckout.BarcodedItem;
import org.lsmr.selfcheckout.Numeral;
import org.lsmr.selfcheckout.devices.DisabledException;
import org.lsmr.selfcheckout.devices.OverloadException;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;
import org.lsmr.selfcheckout.devices.SimulationException;
import org.lsmr.selfcheckout.products.BarcodedProduct;

import controller.BankStub;
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
    private ScanItem scanItem;

	

	 

    @Before
    public void setup() {
  
        currency = Currency.getInstance("USD");
        int[] ints = {5, 10, 20, 50};
        BigDecimal[] decs = {new BigDecimal(".05"), new BigDecimal(".1"), new BigDecimal(".25")};
        station = new SelfCheckoutStation(currency, ints, decs, 500, 1);
        checkoutTest = new CustomerCheckout(station, new BankStub());
        scanItem = new ScanItem(station);
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
	   	 checkoutTest.payWithBankNoteAndCoin(new BigDecimal(0));
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
    	 checkoutTest.payWithDebitOrCredit(new BigDecimal(0));
		 assertTrue(station.mainScanner.isDisabled());
	     assertFalse(station.cardReader.isDisabled());
	     assertTrue(station.coinSlot.isDisabled());
	     assertTrue(station.banknoteInput.isDisabled());
    }
    
    /* Test whether when a user decides to confirm they have completed 
     * all purchase that all relevant devices are disabled */
//    @Test
//    public void testConfirmPurchase() throws DisabledException, OverloadException {
//   
//    	BigDecimal moneyOwed = new BigDecimal(20);
//    	checkoutTest.setAmountOwed(moneyOwed);
//    	
//    	BigDecimal totalPayment = new BigDecimal(5);
//    	checkoutTest.setTotalPayment(totalPayment);
//    	assertFalse(checkoutTest.confirmPurchase());
//    
//    	
//    	totalPayment = new BigDecimal(20);
//     	checkoutTest.setTotalPayment(totalPayment);
//    	assertTrue(checkoutTest.confirmPurchase());
//    	
//    	
//    	totalPayment = new BigDecimal(40);
//    	checkoutTest.setTotalPayment(totalPayment);
//    	assertTrue(checkoutTest.confirmPurchase());
//    	
//    	
//    	//After all items paid for disable all relevant devices
//		 assertTrue(station.mainScanner.isDisabled());
//	     assertTrue(station.cardReader.isDisabled());
//	     assertTrue(station.coinSlot.isDisabled());
//	     assertTrue(station.banknoteInput.isDisabled());
//    	
//    }
//    
    
    /*Test whether you can add more item to scan*/
    @Test
    public void testAddItemToScanner() {
    	checkoutTest.addItemToScanner();
		 assertFalse(station.mainScanner.isDisabled());
	     assertTrue(station.cardReader.isDisabled());
	     assertTrue(station.coinSlot.isDisabled());
	     assertTrue(station.banknoteInput.isDisabled());
    }
    
    
//    //Test if receipt prints the correct message
//    @Test
//    public void testPrinterReceipt() {
//    	
//    	scanItem.addProduct(soupProd);
//    	scanItem.addProduct(doritoProd);
//    	
// 
//    	scanItem.addScanneditems(doritoItem);
//    	scanItem.addScanneditems(soupItem);
//
//  
//    	//Set amount owed and total payment
//    	checkoutTest.setAmountOwed(new BigDecimal(5));
//    	checkoutTest.setTotalPayment(new BigDecimal(5));
//    	
//    	//Prints Items onto the printer
//    	checkoutTest.confirmPurchase();
//    
//    	station.printer.cutPaper();
//    
//    	//Check if CustomerCheckout receipt message match the one from printer 	
//    	Assert.assertEquals(checkoutTest.getReceiptMessage(), 		station.printer.removeReceipt());
//    	
//    }
    
    
}