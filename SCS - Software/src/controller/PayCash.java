package controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import org.lsmr.selfcheckout.Banknote;
import org.lsmr.selfcheckout.Coin;
import org.lsmr.selfcheckout.devices.*;
import org.lsmr.selfcheckout.devices.observers.AbstractDeviceObserver;
import org.lsmr.selfcheckout.devices.observers.BanknoteDispenserObserver;
import org.lsmr.selfcheckout.devices.observers.BanknoteSlotObserver;
import org.lsmr.selfcheckout.devices.observers.BanknoteValidatorObserver;
import org.lsmr.selfcheckout.devices.observers.CoinDispenserObserver;
import org.lsmr.selfcheckout.devices.observers.CoinSlotObserver;
import org.lsmr.selfcheckout.devices.observers.CoinTrayObserver;
import org.lsmr.selfcheckout.devices.observers.CoinValidatorObserver;

// Control software for 'customer pays with coin,' 'customer pays with banknote,' and 'give customer change' use cases
public class PayCash {
	
	// PayCash principle fields
	private SelfCheckoutStation scs;
	private BigDecimal totalPayment = new BigDecimal(0);
	private BigDecimal amountOwed = new BigDecimal(0);
	private BigDecimal insertedCoinValue = new BigDecimal(0);
	private int insertedNoteValue;
	private PCC pcc;
	private PCB pcb;
	
	// Not sure if it's a good idea to set these as class fields. It makes it easier since a method can only return one type.
	//Change to getters and setters
	public ArrayList<Integer> billsDue;
	public ArrayList<BigDecimal> coinsDue;
	
	// Constructor, sets 'scs' and 'amountOwed' based off parameters
	public PayCash(SelfCheckoutStation station, BigDecimal amount){
		
		scs = station;
		Arrays.sort(scs.banknoteDenominations);
		Collections.sort(scs.coinDenominations);
		amountOwed = amount;
		
		//Initializing observers
		pcc = new PCC();
		pcb = new PCB();
		
		//Register observers in the coin related devices
		scs.coinSlot.attach(pcc);
		scs.coinValidator.attach(pcc);
		scs.coinTray.attach(pcc);
		
		//Registers observers in the bank note related devices
		scs.banknoteInput.attach(pcb);
		scs.banknoteValidator.attach(pcb);
		scs.banknoteInput.attach(pcb);

	}
	
	
	// Compares totalPayment (money entered) to amountOwed (cost of goods)
	public Boolean checkEnough() {
		BigDecimal paid = this.totalPayment;
		BigDecimal total = this.amountOwed;
		Boolean enough;
		
		// Paid more than cost
		if(paid.compareTo(total) == 1) {
			
			scs.coinSlot.disable();
			scs.banknoteInput.disable();
			
			// Initiate change calculation and delivery
			determineChange(paid, total);
			deliverChange();
			enough = true;
		}
		// Exact change
		else if (paid.compareTo(total) == 0) {
			
			scs.coinSlot.disable();
			scs.banknoteInput.disable();
			enough = true;
		}
		// Insufficient funds
		else {
			
			enough = false;
		}
		
		return enough;
	}	
	
	
	// Delivers change to customer
	// void return type is tentative
	public void deliverChange() {
		// Call banknote dispenser
		// Call coin dispenser
	}
	
	
	// Principle method for determining change, calls other methods for calculation
	public void determineChange(BigDecimal paid, BigDecimal total) {
		
		// Using other methods to calculate change due in array lists of integers (banknotes) and BigDecimal (coins)
		BigDecimal changeDue = paid.subtract(total);
		ArrayList<BigDecimal> changeList = separateCoinsFromBills(changeDue);
		ArrayList<Integer> billsDue = calcBillsChange(changeList.get(0));
		Collections.sort(billsDue);
		ArrayList<BigDecimal> coinsDue = calcCoinsChange(changeList.get(1));
		Collections.sort(coinsDue);
		
		scs.banknoteInput.enable();
		scs.coinSlot.enable();
		
		// Setting class fields
		this.billsDue = billsDue;
		this.coinsDue = coinsDue;
	}
	
	
	// Separates change due in coins from change due in banknotes
	public ArrayList<BigDecimal> separateCoinsFromBills(BigDecimal change) {
	
		double changeDouble = change.doubleValue();
		int changeInt = change.intValue();
		changeDouble -= changeInt;
		
		ArrayList<BigDecimal> changeReturn = new ArrayList<>();
		changeReturn.add(new BigDecimal(changeInt));
		changeReturn.add(new BigDecimal(changeDouble));
		
		return changeReturn;
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
	
	
	//COIN PAYMENT - Implementation of Coin observers
		private class PCC implements CoinSlotObserver, CoinValidatorObserver, CoinTrayObserver, CoinDispenserObserver{
			@Override
			public void enabled(AbstractDevice<? extends AbstractDeviceObserver> device) {
				// Ignore	
			}

			@Override
			public void disabled(AbstractDevice<? extends AbstractDeviceObserver> device) {
				// Ignore
			}

			
			@Override
			public void coinInserted(CoinSlot slot) {
				getInsertedCoinValue();
			}

			
			@Override
			public void validCoinDetected(CoinValidator validator, BigDecimal value) {
				totalPayment = totalPayment.add(value);
				checkEnough();
			}

			@Override
			public void invalidCoinDetected(CoinValidator validator) {
				//Ignore 
			}
			
			@Override
			public void coinAdded(CoinTray tray) {
				
				/*Simulates removal of coin from the coin tray
				for(Coin theCoin :tray.collectCoins() ) {
					coinTrayList.add(theCoin);
				}*/
				
			}

			@Override
			public void coinsFull(CoinDispenser dispenser) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void coinsEmpty(CoinDispenser dispenser) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void coinAdded(CoinDispenser dispenser, Coin coin) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void coinRemoved(CoinDispenser dispenser, Coin coin) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void coinsLoaded(CoinDispenser dispenser, Coin... coins) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void coinsUnloaded(CoinDispenser dispenser, Coin... coins) {
				// TODO Auto-generated method stub
				
			}
		}
		
		
		//BANKNOTE PAYMENT - Implementation of Bank note observers
		private class PCB implements BanknoteSlotObserver, BanknoteValidatorObserver, BanknoteDispenserObserver{
			
			@Override
			public void enabled(AbstractDevice<? extends AbstractDeviceObserver> device) {			
				//Ignore
			}

			@Override
			public void disabled(AbstractDevice<? extends AbstractDeviceObserver> device) {
				//Ignore
			}
			
			@Override
			public void banknoteInserted(BanknoteSlot slot) {
				getInsertedNoteValue();
			}

			@Override
			public void banknoteEjected(BanknoteSlot slot) {
				//Ignore
			}

			@Override
			public void banknoteRemoved(BanknoteSlot slot) {
				//Ignore
			}

			@Override
			public void validBanknoteDetected(BanknoteValidator validator, Currency currency, int value) {
				//Subtract the value of cart from the customer bank note value
				BigDecimal bigDecimalVal = new BigDecimal(value);
				totalPayment = totalPayment.add(bigDecimalVal);
				checkEnough();
			}

			@Override
			public void invalidBanknoteDetected(BanknoteValidator validator) {
				// Ignore
			}

			@Override
			public void moneyFull(BanknoteDispenser dispenser) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void banknotesEmpty(BanknoteDispenser dispenser) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void billAdded(BanknoteDispenser dispenser, Banknote banknote) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void banknoteRemoved(BanknoteDispenser dispenser, Banknote banknote) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void banknotesLoaded(BanknoteDispenser dispenser, Banknote... banknotes) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void banknotesUnloaded(BanknoteDispenser dispenser, Banknote... banknotes) {
				// TODO Auto-generated method stub
				
			}
		}
	
}