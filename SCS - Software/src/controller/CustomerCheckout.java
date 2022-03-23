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

import org.lsmr.selfcheckout.devices.SelfCheckoutStation;

/**
 * In this stage there's no way to interact with the user,
 * so for this use case we simply enable all related devices.
 */

// Control software for 'customer wishes to checkout' use case 
public class CustomerCheckout{
	
	private SelfCheckoutStation station;
	
	public CustomerCheckout(SelfCheckoutStation station) {
		this.station = station;
	}
	
	
	public void beforeCheckout() {
		station.mainScanner.enable();
		station.coinSlot.disable();
		station.banknoteInput.disable();
		station.cardReader.disable();
	}
	
	
	public void customerFinishAddingItem() {
		station.mainScanner.disable();
		station.coinSlot.enable();
		station.banknoteInput.enable();
		station.cardReader.enable();
	}
	
	public void additionItemAfterPartialPayment() {
		station.mainScanner.enable();
		station.coinSlot.disable();
		station.banknoteInput.disable();
		station.cardReader.disable();
	}
}