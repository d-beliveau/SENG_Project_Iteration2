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

import java.util.ArrayList;
import java.util.List;

import org.lsmr.selfcheckout.BarcodedItem;
import org.lsmr.selfcheckout.Item;
import org.lsmr.selfcheckout.devices.AbstractDevice;
import org.lsmr.selfcheckout.devices.ElectronicScale;
import org.lsmr.selfcheckout.devices.OverloadException;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;
import org.lsmr.selfcheckout.devices.SimulationException;
import org.lsmr.selfcheckout.devices.observers.AbstractDeviceObserver;
import org.lsmr.selfcheckout.devices.observers.ElectronicScaleObserver;

//Control software for 'customer bags an item' use case 
public class BaggingArea implements ElectronicScaleObserver{
   final private SelfCheckoutStation station;
   final private ElectronicScale baggingArea;
   private double previousWeight;
   private double currentWeight;
   private boolean overloaded;
   private boolean itemTooLight;
   
   // removed Item has a global variable (change) 
   
   // change
   private ScanItem theScanner;

   @Override
   public void enabled(AbstractDevice<? extends AbstractDeviceObserver> device) {

   }

   @Override
   public void disabled(AbstractDevice<? extends AbstractDeviceObserver> device) {

   }

   @Override
   // Will be called when the scale detects changes in weight
   public void weightChanged(ElectronicScale scale, double weightInGrams) {
//     checkIfItemPlaced(weightInGrams);
       currentWeight = weightInGrams;
   }

   @Override
   public void overload(ElectronicScale baggingArea) {
       overloaded = true;
       station.mainScanner.disable();
   }

   @Override
   public void outOfOverload(ElectronicScale baggingArea) {
       overloaded = false;
       station.mainScanner.enable();
   }

   // Getter for scale, used in testing
   public ElectronicScale getBaggingAreaScale() {
       return baggingArea;
   }

   // Getter for overloaded, used in testing
   public boolean isOverloaded() {
       return overloaded;
   }

   // Set up the scale and attach the observer
   public BaggingArea(SelfCheckoutStation station) {
       this.station = station;
       baggingArea = this.station.baggingArea; // baggingArea is the scale in baggingArea
       getBaggingAreaScale().attach(this);
       getBaggingAreaScale().enable();
       
       // change
       theScanner = new ScanItem(station); // only exists once the baggibgArea is linked to the station
   }

//   // Get scanned item from scanner
//   public void scanBaggingItem(BarcodedItem barcodedItem) {
////       this.item = barcodedItem;
//       station.mainScanner.disable();
//   }

   // Simulate placing an item into bagging area
   public void placeItem(Item item) {
       if (item == null)
           throw new SimulationException("Trying to place an empty item into bagging area.");
       else
       {
    	   previousWeight = currentWeight;
           getBaggingAreaScale().add(item);
    	   station.mainScanner.disable();
       }
       
   }
   
   // Check if the weight added on scale equals to the weight of item
   public void checkIfItemPlaced(Item anItem) throws OverloadException {
	   if(currentWeight - previousWeight == anItem.getWeight())
    	   // as the getCurrentWeight() method returns a slightly randomized value
           station.mainScanner.enable();
   }
   
   
   
   
 //------------------------------------------------------------------------------------------------------------------------------------------
   // customer adding bag method
   public void addCustomerBag() throws OverloadException{
	   double bagWeight = baggingArea.getCurrentWeight() - currentWeight;
	   currentWeight = currentWeight + bagWeight;
	   // even though this weight might be a bit messed up as due to the randomness in getCurrentWeight()
	   // but its set to the exact weight when adding/removing an item due to weightChanged()
   }
   
   // customer removing bag method
   public void removeCustomerBag() throws OverloadException{
	   currentWeight = baggingArea.getCurrentWeight();
	   // even though this weight might be a bit messed up as due to the randomness in getCurrentWeight()
	   // but its set to the exact weight when adding/removing an item due to weightChanged()
   }
   
   // i am considering an item has to be bagged right after being scanned
   public void bagItemAfterScanning() throws OverloadException
   {
	// copying scannedLists for modification
	   int lengthList = theScanner.getScanneditems().size();
	   List <BarcodedItem> itemsToBeBagged = new ArrayList<BarcodedItem>();
	   for(int j = 0; j < lengthList; j++)
	   {
		   itemsToBeBagged.add(theScanner.getScanneditems().get(j));
	   }
	   
	   if(itemsToBeBagged.isEmpty());
		   // no items to be bagged 
	   else
	   {
		   try
		   {
			   itemTooLight = false;
			   placeItem(itemsToBeBagged.get(-1));
			   if((currentWeight == previousWeight) && (itemsToBeBagged.get(0).getWeight() != 0))
				   // itemWeight < sensitivity
			   {
				   itemTooLight = true;
			   }
			   // checks if this item is actually placed else throw exception
			   checkIfItemPlaced(itemsToBeBagged.get(-1));
			   // checkIfItemPlaced(Item anItem) also enables the scanner ,
			   
			   
			   // if it causes error remove "station.mainScanner.enable();" line from checkIfItemPlaced
			   // and add it here
			   
			   
			   itemsToBeBagged.remove(-1); // removing item after bagging
			   
			   // throw exception if item weight < sensitivity or item == null
		   }
		   catch(NullPointerException npe)
		   {
			   System.out.println("No item to bag");
		   }
		  
	   }
   }

   public ElectronicScale getScale() {
	   return baggingArea;
   }
   
   public void removeItem(Item item)
   {
	   if (item == null)
           throw new SimulationException("Trying to remove an empty item from bagging area.");
       else
           getBaggingAreaScale().remove(item);
	   
   }

public boolean isItemTooLight() {
	return itemTooLight;
}
}