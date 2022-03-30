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

package controller;

import java.math.BigDecimal;

import org.lsmr.selfcheckout.devices.*;
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
	ReceiptPrinter printer;
	
	public CustomerCheckout(SelfCheckoutStation station) {
		this.station = station;
		cashLogic = new PayCash(station, new BigDecimal(0));
		cardLogic = new CardFromCardReader(station);
		printer = station.printer;
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
	public void payWithDebitOrCredit(BigDecimal payment) {
		getCardLogic().paymentAmount = payment;
		station.cardReader.enable();
		
		station.mainScanner.disable();
		station.coinSlot.disable();
		station.banknoteInput.disable();
	}
	
	
	//Customer choose this as final option they are done with all payment, prints reciept
	public boolean confirmPurchase() {
		 int res;
		 BigDecimal amountPayed = cashLogic.getTotalPayment().add(getCardLogic().paymentTotal);
	     res = getAmountOwed().compareTo(amountPayed);

	     //Return false if customer has not paid for everything
	     if( res == 1 ) {
	         return false;
	     }
	 	
	 	/*
	 	 * Code from Iteration 1 - Group 48
	 	
	 	for(Product product : products) {
	 		String toPrint = "$" + product.getPrice().setScale(2, RoundingMode.HALF_EVEN) + "\n";
	 		for(char c : toPrint.toCharArray()) {
	 			printer.print(c);
	 		}
	 	}
	 	
	 	String endString = "Total:\n" + "$" + new BigDecimal(totalCost).setScale(2, RoundingMode.HALF_EVEN);
	 	for(char c : endString.toCharArray()) {
	 		printer.print(c);
	 	}
	 	*/
	    if(cardLogic.memberNumber != null) {
		 	for(char c :cardLogic.memberNumber.toCharArray()) {
		 		printer.print(c);
		 	}
	    }
	 	printer.cutPaper();
	
		station.mainScanner.disable();
		station.coinSlot.disable();
		station.banknoteInput.disable();
		station.cardReader.disable();
		
		//Returns true if everything is paid for
		return true;
		
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


	public CardFromCardReader getCardLogic() {
		return cardLogic;
	}


	public void setCardLogic(CardFromCardReader cardLogic) {
		this.cardLogic = cardLogic;
	}
	
}