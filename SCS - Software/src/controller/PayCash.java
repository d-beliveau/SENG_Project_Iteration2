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
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import org.lsmr.selfcheckout.devices.*;
import org.lsmr.selfcheckout.devices.observers.AbstractDeviceObserver;
import org.lsmr.selfcheckout.devices.observers.BanknoteStorageUnitObserver;
import org.lsmr.selfcheckout.devices.observers.BanknoteValidatorObserver;
import org.lsmr.selfcheckout.devices.observers.CoinStorageUnitObserver;
import org.lsmr.selfcheckout.devices.observers.CoinValidatorObserver;

//Control software for 'customer pays with coin' and 'customer pays with banknote' use cases
public class PayCash implements CoinValidatorObserver, BanknoteValidatorObserver, /*CoinDispenserObserver, BanknoteDispenserObserver,
	*/CoinStorageUnitObserver, BanknoteStorageUnitObserver{
	
	// Changing PayCash fields to a single SelfCheckoutStation feild
	private SelfCheckoutStation scs;

	private BigDecimal totalPayment = new BigDecimal(0);
	private BigDecimal amountOwed;
	private BigDecimal insertedCoinValue;
	private int insertedNoteValue;
	
	// Constructor, sets 'scs' and 'amountOwed' based off parameters
	public PayCash(SelfCheckoutStation station, BigDecimal amount){
		
		scs = station;
		Arrays.sort(scs.banknoteDenominations);
		Collections.sort(scs.coinDenominations);
		amountOwed = amount;
		
		scs.banknoteValidator.attach(this);
		scs.coinValidator.attach(this);
		scs.banknoteStorage.attach(this);
		scs.coinStorage.attach(this);
		
//*** THIS BLOCK WILL ADD APPROPRIATE DISPENSER OBSERVERS WHEN THEY'RE EVENTUALLY NEEDED ***//
		
//		for(int denomination : scs.banknoteDispensers.keySet()) {
//			
//			scs.banknoteDispensers.get(denomination).attach(this);
//		}
//		for(BigDecimal denomination : scs.coinDispensers.keySet()) {
//			
//			scs.coinDispensers.get(denomination).attach(this);
//		}
//
	}
	

	// Compares totalPayment (money entered) to amountOwed (cost of goods)
	public Boolean checkEnough(BigDecimal paid, BigDecimal total) {
		
		Boolean enough;
		
		if(paid.compareTo(total) == 1) {
			
			scs.coinSlot.disable();
			scs.banknoteInput.disable();
//			deliverChange(total, paid);
			enough = true;
		}
		else if(paid.compareTo(total) == 0) {
			
			scs.coinSlot.disable();
			scs.banknoteInput.disable();
			enough = true;
		}
		else {
			
			enough = false;
		}
		
		return enough;
	}	
	
	
	// Calculates change due in banknotes by looping through a banknotes assignment
	public ArrayList<Integer> calcBillsChange(BigDecimal change) {
		
		// Convert change to a double
		double changeDouble = change.doubleValue();
		
		// Converting scs.banknoteDenominations to an ArrayList<Integer>
		ArrayList<Integer> banknoteDenoms = new ArrayList<>();
		for (int bill : scs.banknoteDenominations) {
			banknoteDenoms.add(bill);
		}
		Collections.sort(banknoteDenoms);
		
		// Assigning banknotes due as an ArrayList<Double>, starting with largest banknotes
		ArrayList<Integer> banknotesDue = new ArrayList<>();
		for (int i = banknoteDenoms.size() - 1; i >= 0 ; i--) {
			int currentBanknote = banknoteDenoms.get(i);

			// Giving as many of the current banknote as possible
			while(changeDouble >= currentBanknote) {
				banknotesDue.add(currentBanknote);
				changeDouble -= currentBanknote;
			}
			
		}

		return banknotesDue;
	}
	
	
	// Calculates change due in coins by looping through a coin assignment
	// Converts BigDecimal to double for calculation and the converts back
	public ArrayList<BigDecimal> calcCoinsChange(BigDecimal change) {
		
		// Convert change to a double
		double changeDouble = change.doubleValue();
		
		// Converting scs.coinDenominations to an ArrayList<Double>
		ArrayList<Double> coinDenomsDouble = new ArrayList<>();
		Collections.sort(scs.coinDenominations);
		for (BigDecimal bd : scs.coinDenominations) {
			coinDenomsDouble.add(bd.doubleValue());
		}
		
		// Assigning coins due as an ArrayList<Double>, starts with largest coins
		ArrayList<Double> coinsDueDouble = new ArrayList<>();
		for (int i = coinDenomsDouble.size() - 1; i >= 0 ; i--) {
			double currentCoin = coinDenomsDouble.get(i);

			while(changeDouble >= (currentCoin)) {
				coinsDueDouble.add(currentCoin);
				changeDouble = changeDouble - currentCoin;
			}
			
		}
		
		// Converting coins due to an ArrayList<BigDecimal>
		ArrayList<BigDecimal> coinsDue = new ArrayList<>();
		for (double i : coinsDueDouble) {
			coinsDue.add((new BigDecimal(i)).setScale(2, RoundingMode.FLOOR));
		}

		return coinsDue;
	}
	
	@Override
	public void validCoinDetected(CoinValidator validator, BigDecimal value) {
		
		insertedCoinValue = value;
	}
	
	@Override
	public void validBanknoteDetected(BanknoteValidator validator, Currency currency, int value) {
		
		insertedNoteValue = value;
	}
	
	@Override
	public void coinAdded(CoinStorageUnit unit) {
		
		totalPayment = totalPayment.add(insertedCoinValue);
		checkEnough(totalPayment, amountOwed);
	}
	
	@Override
	public void banknoteAdded(BanknoteStorageUnit unit) {		
		
		totalPayment = totalPayment.add(BigDecimal.valueOf(insertedNoteValue));
		checkEnough(totalPayment, amountOwed);
	}
	
	@Override
	public void banknotesFull(BanknoteStorageUnit unit) {

//		System.err.println("This terminal is unable accept more bank notes. Bank note storage full.");
	}
	
	@Override
	public void coinsFull(CoinStorageUnit unit) {
		
//		System.err.println("This terminal is unable accept any more change. Coin storage full.");
	}
	
	@Override
	public void enabled(AbstractDevice<? extends AbstractDeviceObserver> device) {}

	@Override
	public void disabled(AbstractDevice<? extends AbstractDeviceObserver> device) {}

	@Override
	public void invalidCoinDetected(CoinValidator validator) {}

	@Override
	public void invalidBanknoteDetected(BanknoteValidator validator) {}

	@Override
	public void banknotesLoaded(BanknoteStorageUnit unit) {}

	@Override
	public void banknotesUnloaded(BanknoteStorageUnit unit) {}
	
	@Override
	public void coinsLoaded(CoinStorageUnit unit) {}

	@Override
	public void coinsUnloaded(CoinStorageUnit unit) {}
	
///***HELPER METHODS***///
	
	public void setTotalPayment(BigDecimal value) {
		
		totalPayment = value;
	}
	
	public void setAmountOwed(BigDecimal value) {
		
		amountOwed = value;
	}
	
	public BigDecimal getTotalPayment() {
		
		return totalPayment;
	}
	
	public BigDecimal getAmountOwed() {
		
		return amountOwed;
	}
	
	public BigDecimal getInsertedCoinValue() {
		
		return insertedCoinValue;
	}
	
	public int getInsertedNoteValue() {
		
		return insertedNoteValue;
	}
	
	
//*** KEEP THESE METHODS FOR LATER IMPLENTATIONS***//
//*** PLEASE DON'T DELETE YET ***//
	
//	public ArrayList<BigDecimal> separateCoinsFromBills(BigDecimal change) {
//	
//	double changeDouble = change.doubleValue();
//	int changeInt = change.intValue();
//	changeDouble -= changeInt;
//	
//	ArrayList<BigDecimal> changeReturn = new ArrayList<>();
//	changeReturn.add(new BigDecimal(changeInt));
//	changeReturn.add(new BigDecimal(changeDouble));
//	
//	return changeReturn;
//}
	
//	// Changed to for-each loop
//	private int sumBills(ArrayList<Integer> billsArray) {
//		
//		int sum = 0;
//		
//		for(int bill : billsArray) {
//			
//			sum += bill;
//		}
//		
//		return sum;
//	}
	
//	public BigDecimal deliverChange(BigDecimal cost, BigDecimal entered) {
//		
//		BigDecimal changeDue = entered.subtract(cost);
//		ArrayList<Integer> billsDue = calcBillsChange(changeDue);
//		Collections.sort(billsDue);
//		BigDecimal changeStillDue = changeDue.subtract(BigDecimal.valueOf(sumBills(billsDue)));
//		ArrayList<BigDecimal> coinsDue = calcCoinsChange(changeStillDue);
//		Collections.sort(coinsDue);
//		int i = 0;
//		BigDecimal zero = new BigDecimal(0);
//		
//		try {
//			
//			for(BigDecimal denomination : scs.coinDispensers.keySet()) {
//				
//				while(coinsDue.contains(denomination)) {
//					
//					scs.coinDispensers.get(denomination).emit();
//					coinsDue.set(i, zero);
//					i++;
//					changeDue = changeDue.subtract(denomination);
//				}
//			}
//			
//			for(int denomination : scs.banknoteDispensers.keySet()) {
//				
//				while(billsDue.contains(denomination)) {
//					
//					scs.banknoteDispensers.get(denomination).emit();
//					billsDue.set(i, 0);
//					i++;
//					changeDue =  changeDue.subtract(BigDecimal.valueOf(denomination));
//				}
//			}
//		
//			i = 0;
//
//		}
//		catch(OverloadException | DisabledException | EmptyException e) {
//			
//		}
//		
//		scs.banknoteInput.enable();
//		scs.coinSlot.enable();
//		
//		return changeDue;
//	}
	
//	private int getNoteKey(BanknoteDispenser dispenser) {
//		
//		int billDenom = 0;
//		
//		for(int denomination : scs.banknoteDispensers.keySet()) {
//			// This if conditional never passes
//			if(Objects.equals(scs.banknoteDispensers.get(denomination), dispenser)) {
//				
//				billDenom = denomination;
//			}
//		}
//		
//		return billDenom;
//	}
//	
//	private BigDecimal getCoinKey(CoinDispenser dispenser) {
//		
//		BigDecimal coinDenom = new BigDecimal(0);
//		
//		for(BigDecimal denomination : scs.coinDispensers.keySet()) {
//			// This if conditional never passes
//			if(Objects.equals(scs.coinDispensers.get(denomination), dispenser)) {
//				
//				coinDenom = denomination;
//			}
//		}
//		
//		return coinDenom;
//	}
	
//	@Override
//	public void banknotesEmpty(BanknoteDispenser dispenser) {
//
//		System.err.println("This checkout is out of $" + getNoteKey(dispenser) + " bills.");
//	}
//	
//	@Override
//	public void coinsEmpty(CoinDispenser dispenser) {
//		
//		System.err.println("This checkout is out of " + getCoinKey(dispenser).setScale(2, RoundingMode.HALF_EVEN) + " cent coins.");
//	}
}