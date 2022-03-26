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

import org.lsmr.selfcheckout.devices.SelfCheckoutStation;

/**
 * In this stage there's no way to interact with the user,
 * so for this use case we simply enable all related devices.
 */

// Control software for 'customer wishes to checkout' use case 
// use case for partial payment as well
public class CustomerCheckout{
	
	private SelfCheckoutStation station;
	
	public CustomerCheckout(SelfCheckoutStation station) {
		this.station = station;
	}
	
	
	//Checkout station state before customer starts using the station
	public void beforePurchase() {
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
	public void payWithBankNoteAndCoin() {
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
	
	
	//Customer choose this as final option they are done with all payment
	public boolean confirmPurchase(BigDecimal amountOwed) {
		 int res;
	     res = amountOwed.compareTo(new BigDecimal(0));

	     //Return false if customer has not paid for everything
	     if( res == 0 || res == -1 ) {
	         return false;
	     }
		
		station.mainScanner.disable();
		station.coinSlot.disable();
		station.banknoteInput.disable();
		station.cardReader.disable();
		
		//Returns true if everything is paid for
		return true;
		
	}
	
	//Customer wishes to add more items even after partial payment
	public void addMoreItems() {
		station.mainScanner.enable();
		
		station.coinSlot.disable();
		station.banknoteInput.disable();
		station.cardReader.disable();
	}
	
}