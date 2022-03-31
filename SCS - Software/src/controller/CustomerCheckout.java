package controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.lsmr.selfcheckout.Barcode;
import org.lsmr.selfcheckout.BarcodedItem;
import org.lsmr.selfcheckout.Numeral;
import org.lsmr.selfcheckout.devices.*;
import org.lsmr.selfcheckout.devices.observers.AbstractDeviceObserver;
import org.lsmr.selfcheckout.devices.observers.ReceiptPrinterObserver;
import org.lsmr.selfcheckout.products.*;

/**
 * In this stage there's no way to interact with the user,
 * so for this use case we simply enable all related devices.
 */

// Control software for 'customer wishes to checkout' use case 

public class CustomerCheckout{
	
	private SelfCheckoutStation station;
	private CardFromCardReader cardLogic;
	private PayCash cashLogic;
	private BigDecimal amountOwed = new BigDecimal(0);
	private ScanItem scan;
	private ReceiptPrinter printer;
	private String receiptMessage = "";
	private BigDecimal totalPayment;

	
	
	
	public CustomerCheckout(SelfCheckoutStation station, BankStub bank) {
		this.station = station;
		cashLogic = new PayCash(station, new BigDecimal(0));
		cardLogic = new CardFromCardReader(station, bank);
		printer = station.printer;
		scan = new ScanItem(station);
		
		//Add ink and paper to the printer
		station.printer.addPaper(8);
		station.printer.addInk(15);
		
	}
	
	public void setScanItemController(ScanItem scan) {
		this.scan = scan;
	}
	
	
	//Checkout station state before customer starts using the station
	public void beforeStartPurchase() {
		station.mainScanner.disable();
		station.coinSlot.disable();
		station.banknoteInput.disable();
		station.cardReader.disable();
	}
	
	//Checkout station state after customer press start purchase button
	public void startPurchase() {
		station.mainScanner.enable();
		station.coinSlot.disable();
		station.banknoteInput.disable();
		station.cardReader.disable();
	}
	
	//Customer choose to pay with bank note and coin
	public void payWithBankNoteAndCoin(BigDecimal payment) {
		cashLogic.setAmountOwed(payment);
		
		station.mainScanner.disable();
		station.cardReader.disable();
		
		station.coinSlot.enable();
		station.banknoteInput.enable();
	}
	
	//Customer chooses to use membership card
	public void useMembershipCard() {
		station.cardReader.enable();
	}

	//Customer choose to use debit or credit card for payment
	public void payWithDebitOrCredit() {
		station.cardReader.enable();
		
		station.mainScanner.disable();
		station.coinSlot.disable();
		station.banknoteInput.disable();
	}
	
	
	//Customer choose this as final option they are done with all payment, prints reciept
	public boolean confirmPurchase() {
		 int res;
	     res = getAmountOwed().compareTo(getTotalPayment());

	     //Return false if customer has not paid for everything
	     if(res == 1) {
	         return false;
	     }  
	     
    	 if (scan.getScanneditems().size() > 0) {
    	
    		 printReceiptItems();
	     }else {
	    	 
	 		station.mainScanner.disable();
			station.coinSlot.disable();
			station.banknoteInput.disable();
			station.cardReader.disable();
			
	    	 return true;
	     }
	    	   
		//receiptMessage = printer.removeReceipt();
		

		station.mainScanner.disable();
		station.coinSlot.disable();
		station.banknoteInput.disable();
		station.cardReader.disable();
		
		//Returns true if everything is paid for
		return true;
		
	}
	
	public String getReceiptMessage() {
		return receiptMessage;
	}
	
	public void printReceiptItems() {	

		
		Dictionary<Barcode, BarcodedProduct> Products = scan.getProducts();
		List<BarcodedItem> barcodedItemList = scan.getScanneditems();
		receiptMessage = "";
		
		
		for(BarcodedItem someItem: barcodedItemList) {
			BarcodedProduct theProduct = Products.get(someItem.getBarcode());
			
			receiptMessage = receiptMessage +
					theProduct.getDescription() + " " 
					+ theProduct.getPrice() + "\n";
		}
		
		receiptMessage = receiptMessage + "Total Price: " + getTotalPayment();
		
		for(int i =0; i < receiptMessage.length(); i++) {
			station.printer.addInk(1);
			station.printer.print(receiptMessage.charAt(i));		
		}
	}
	
	//Customer wishes to add more items even after partial payment
	public void addItemToScanner() {
		station.mainScanner.enable();
		
		station.coinSlot.disable();
		station.banknoteInput.disable();
		station.cardReader.disable();
	}


	public BigDecimal getAmountOwed() {
		return amountOwed;
	}


	public void setAmountOwed(BigDecimal amountOwed) {
		this.amountOwed = amountOwed;
	}
	
	public void setTotalPayment(BigDecimal payment) {
		this.totalPayment = payment;
	}
	
	public BigDecimal getTotalPayment() {
		return totalPayment;
	}


}