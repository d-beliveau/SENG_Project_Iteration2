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

import org.lsmr.selfcheckout.BarcodedItem;
import org.lsmr.selfcheckout.Item;
import org.lsmr.selfcheckout.devices.AbstractDevice;
import org.lsmr.selfcheckout.devices.ElectronicScale;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;
import org.lsmr.selfcheckout.devices.SimulationException;
import org.lsmr.selfcheckout.devices.observers.AbstractDeviceObserver;
import org.lsmr.selfcheckout.devices.observers.ElectronicScaleObserver;

//Control software for 'customer bags an item' use case 
public class BaggingArea implements ElectronicScaleObserver{
   final private SelfCheckoutStation station;
   final private ElectronicScale scale;
   private Item item;
   private double currentWeight;
   private boolean overloaded;

   @Override
   public void enabled(AbstractDevice<? extends AbstractDeviceObserver> device) {

   }

   @Override
   public void disabled(AbstractDevice<? extends AbstractDeviceObserver> device) {

   }

   @Override
   // Will be called when the scale detects changes in weight
   public void weightChanged(ElectronicScale scale, double weightInGrams) {
       checkIfItemPlaced(weightInGrams);
       currentWeight = weightInGrams;
   }

   @Override
   public void overload(ElectronicScale scale) {
       overloaded = true;
   }

   @Override
   public void outOfOverload(ElectronicScale scale) {
       overloaded = false;
   }

   // Getter for scale, used in testing
   public ElectronicScale getScale() {
       return scale;
   }

   // Getter for item, used in testing
   public Item getItem() {
       return item;
   }

   // Getter for overloaded, used in testing
   public boolean isOverloaded() {
       return overloaded;
   }

   // Set up the scale and attach the observer
   public BaggingArea(SelfCheckoutStation station) {
       this.station = station;
       scale = this.station.scale;
       getScale().attach(this);
       getScale().enable();
   }

   // Get scanned item from scanner
   public void scanBaggingItem(BarcodedItem barcodedItem) {
       this.item = barcodedItem;
       station.scanner.disable();
   }

   // Simulate placing an item into bagging area
   public void placeItem(Item item) {
       if (item == null)
           throw new SimulationException("Trying to place an empty item into bagging area.");
       else
           getScale().add(item);
   }

   // Check if the weight added on scale equals to the weight of item
   private void checkIfItemPlaced(double weightInGrams) {
       if (weightInGrams - currentWeight == item.getWeight()) {
           station.scanner.enable();
       }
   }
}