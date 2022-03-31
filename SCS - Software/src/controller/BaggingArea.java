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


public class BaggingArea implements ElectronicScaleObserver{
   final private SelfCheckoutStation station;
   final private ElectronicScale baggingArea;
   private double previousWeight;
   private double currentWeight;
   private boolean overloaded;
   private boolean itemTooLight;

   private ScanItem theScanner;
   
   /**
    * Constructs the class BaggingArea which is responsible for simulating the functionalities
    * of the bagging area section of the self checkout machine.
    * 
    * @param SelfCheckoutStation
    */
   public BaggingArea(SelfCheckoutStation station) {
       this.station = station;
       baggingArea = this.station.baggingArea;
       getBaggingAreaScale().attach(this);
       getBaggingAreaScale().enable();
       
       theScanner = new ScanItem(station); 
   }
   

   @Override
   public void enabled(AbstractDevice<? extends AbstractDeviceObserver> device) {

   }

   @Override
   public void disabled(AbstractDevice<? extends AbstractDeviceObserver> device) {

   }

   
   /**
    * Triggered when a weight change greater than the sensitivity of the scale occurs.
    * 
    * @param ElectronicScale
    *            The ElectronicScale where the weight change takes place.
    * @param double
    *            The number of grams that the current weight is supposed to be.
    */
   @Override
   public void weightChanged(ElectronicScale scale, double weightInGrams) {
       currentWeight = weightInGrams;
   }

   /**
    * Simulates placing an item on the scale.
    * 
    * @param Item
    * 			Item to be place on the scale.
    */
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
   
   
   /**
    * Check if the weight on the scale is equal to the weight of the item.
    * 
    * @param ElectronicScale
    *            The ElectronicScale where the weight change takes place.
    * @param double
    *            The number of grams that the current weight is supposed to be.
    */
   public void checkIfItemPlaced(Item anItem) throws OverloadException {
	   if(currentWeight - previousWeight == anItem.getWeight())
    	   // as the getCurrentWeight() method returns a slightly randomized value
           station.mainScanner.enable();
   }
   

   /**
    * Triggered when the scale is overloaded
    * 
    * @param ElectronicScale
    *            The overloaded scale
    */
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

   public ElectronicScale getBaggingAreaScale() {
       return baggingArea;
   }

   public boolean isOverloaded() {
       return overloaded;
   }
   
   /**
    * Following are the methods for functions involving bags.
    */
   
   /**
    * Simulates the customer adding a bag on the scale
    * 
    * @param ElectronicScale
    *            The ElectronicScale where the weight change takes place.
    * @param double
    *            The number of grams that the current weight is supposed to be.
    *            
    * @throws OverloadException
    */
   public void addCustomerBag() throws OverloadException{
	   double bagWeight = baggingArea.getCurrentWeight() - currentWeight;
	   currentWeight = currentWeight + bagWeight;
   }
   
   /**
    * Simulates the customer removing a bag from the scale
    *            
    * @throws OverloadException
    */
   public void removeCustomerBag() throws OverloadException{
	   currentWeight = baggingArea.getCurrentWeight();
   }
   
   /**
    * Bagging an item after scanning
    *            
    * @throws OverloadException
    */
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
			   placeItem(itemsToBeBagged.get(0));
			   if((currentWeight == previousWeight) && (itemsToBeBagged.get(0).getWeight() != 0))
				   // itemWeight < sensitivity
			   {
				   itemTooLight = true;
			   }
			   // checks if this item is actually placed else throw exception
			   checkIfItemPlaced(itemsToBeBagged.get(0));
			   // checkIfItemPlaced(Item anItem) also enables the scanner ,
			   
			   
			   // if it causes error remove "station.mainScanner.enable();" line from checkIfItemPlaced
			   // and add it here
			   
			   
			   itemsToBeBagged.remove(0); // removing item after bagging
			   
			   // throw exception if item weight < sensitivity or item == null
		   }
		   catch(NullPointerException npe)
		   {
			   System.out.println("No item to bag");
		   }
		  
	   }
   }
   
   /**
    * Simulates the user removing an item from the scale
    * 
    * @param Item
    * 			The item to be removed.
    */
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

    public ElectronicScale getScale() {
	    return baggingArea;
    }
}