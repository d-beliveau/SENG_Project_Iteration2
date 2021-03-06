package controller;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.lsmr.selfcheckout.Barcode;
import org.lsmr.selfcheckout.BarcodedItem;
import org.lsmr.selfcheckout.devices.*;
import org.lsmr.selfcheckout.products.*;

/**
 * Simulates the case when the Customer chooses to checkout
 */
public class CustomerCheckout{
	
	private SelfCheckoutStation station;
	private CardFromCardReader cardLogic;
	private PayCash cashLogic;
	private BigDecimal amountOwed = new BigDecimal(0);
	private ScanItem scan;
	ReceiptPrinter printer;
	
	/**
    * Constructs an object CustomerCheckout that manages the functionalities
    * of checking out
    * 
    * @param SelfCheckoutStation
    * 
    * @param BankStub
    */
	public CustomerCheckout(SelfCheckoutStation station, BankStub bank) {
		this.station = station;
		cashLogic = new PayCash(station, new BigDecimal(0));
		cardLogic = new CardFromCardReader(station, bank);
		printer = station.printer;
	}
	
	
	/**
    * Checkout station state before customer starts using the station
    */
	public void beforeStartPurchase() {
		station.mainScanner.disable();
		station.coinSlot.disable();
		station.banknoteInput.disable();
		station.cardReader.disable();
	}
	
	/**
	 * Checkout station state after customer press start purchase button
	 */
	public void startPurchase() {
		station.mainScanner.enable();
		station.coinSlot.disable();
		station.banknoteInput.disable();
		station.cardReader.disable();
	}
	
	/**
	 * Customer choose to pay with bank note and coin
	 * @param payment
	 */
	public void payWithBankNoteAndCoin(BigDecimal payment) {
		cashLogic.setAmountOwed(payment);
		
		station.mainScanner.disable();
		station.cardReader.disable();
		
		station.coinSlot.enable();
		station.banknoteInput.enable();
	}
	
	/**
	 * Customer chooses to use membership card
	 */
	public void useMembershipCard() {
		station.cardReader.enable();
	}

	/**
	 * Customer choose to use debit or credit card for payment
	 * @param payment
	 */
	public void payWithDebitOrCredit(BigDecimal payment) {
		getCardLogic().paymentAmount = payment;
		station.cardReader.enable();
		
		station.mainScanner.disable();
		station.coinSlot.disable();
		station.banknoteInput.disable();
	}
	
	
	/**
	 * Customer choose this as final option they are done with all payment, prints receipt
	 * @return Returns true if everything is paid for
	 */
	public boolean confirmPurchase() {
		 int res;
		 BigDecimal amountPayed = cashLogic.getTotalPayment().add(getCardLogic().paymentTotal);
	     res = getAmountOwed().compareTo(amountPayed);

	     //Return false if customer has not paid for everything
	     if( res == 1 ) {
	         return false;
	     }  
	     
	     try {
	    	 if (scan.Scanneditems != null) {
		    	 //printReceiptItems();
		     }
	     }
	     catch(NullPointerException e) {}
	     
//	    //Prints member number
//		if(cardLogic.memberNumber != null) {
//			String memberPrint = "Member number: " + cardLogic.memberNumber;
//			for(char c : memberPrint.toCharArray()) {
//			 		printer.print(c);
//			}
//		}
		//printer.cutPaper();
	     
		station.mainScanner.disable();
		station.coinSlot.disable();
		station.banknoteInput.disable();
		station.cardReader.disable();
		

		return true;
		
	}
	/**
	 * Print the receipt items.
	 */
	public void printReceiptItems() {
		
		 // Going through list of items the customer has scanned
	 	for(BarcodedItem item : this.scan.Scanneditems) {
	 		// Get the barcode of the n-th item
	 		Barcode tempBarcode = item.getBarcode();
	 		
	 		// For that barcode, get the product associated with it in Dictionary (Products)
	 		BarcodedProduct product = this.scan.Products.get(tempBarcode);
	 		
	 		// Prints the price
	 		String toPrintPrice = "$" + product.getPrice().setScale(2, RoundingMode.HALF_EVEN) + "\n";
	 		for(char c : toPrintPrice.toCharArray()) {
	 			printer.print(c);
	 		}
	 		
	 		// Prints the product
	 		String toPrintProduct = product.getDescription();
	 		for(char c : toPrintProduct.toCharArray()) {
	 			printer.print(c);
	 		}
	 		printer.print('\n');
	 	}
	 	
	 	// Prints total
	 	String endString = "Total: " + "$" + this.scan.GetBillPrice(new BigDecimal(0)).setScale(2, RoundingMode.HALF_EVEN);
	 	for(char c : endString.toCharArray()) {
	 		printer.print(c);
	 	}
	 	printer.print('\n');
	}
	
	/**
	 * Customer wishes to add more items even after partial payment
	 */
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


	public CardFromCardReader getCardLogic() {
		return cardLogic;
	}


	public void setCardLogic(CardFromCardReader cardLogic) {
		this.cardLogic = cardLogic;
	}
	
}