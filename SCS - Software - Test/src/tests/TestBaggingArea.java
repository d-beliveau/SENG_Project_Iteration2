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


// some tests make the program stop for a few seconds
package controller;

import org.lsmr.selfcheckout.*;
import org.lsmr.selfcheckout.devices.*;

import controller.BaggingArea;

import org.junit.Before;
import org.junit.Test;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Dictionary;
import java.util.Hashtable;

import static org.junit.Assert.*;

// Tests suite for BaggingArea
public class TestBaggingArea {
	private BaggingArea area;
	private SelfCheckoutStation station;

	@Before
	public void setup() {
		Currency currency = Currency.getInstance("USD");
		int[] ints = {5, 10, 20, 50};
		BigDecimal[] decs = {new BigDecimal(.05), new BigDecimal(.1), new BigDecimal(.25)};
		station = new SelfCheckoutStation(currency, ints, decs, 500, 1);
		area = new BaggingArea(station);
	}

	@Test
	public void testConstructor() {
		assertEquals(area.getScale(), station.baggingArea);
	}
	
	@Test(expected = SimulationException.class)
	public void testPlaceNullItem() {
		BarcodedItem item = null;
		area.placeItem(item);
	}

	@Test
	public void testScannerStatus() {
		Numeral[] nums = {Numeral.valueOf((byte) 1), Numeral.valueOf((byte) 2), Numeral.valueOf((byte) 3), Numeral.valueOf((byte) 4)};
		Barcode barcode = new Barcode(nums);
		BarcodedItem item = new BarcodedItem(barcode, 3.0);
		try {
			area.placeItem(item);
			area.checkIfItemPlaced(item);
			assertFalse(station.mainScanner.isDisabled());
		}
		catch (Exception e) {
			fail("there should not have been an exception");
		}
	}

	@Test
	public void testPlaceItem() {
		Numeral[] nums = {Numeral.valueOf((byte) 1), Numeral.valueOf((byte) 2), Numeral.valueOf((byte) 3), Numeral.valueOf((byte) 4)};
		Barcode barcode = new Barcode(nums);
		BarcodedItem item = new BarcodedItem(barcode, 3.0);
		try {
			area.placeItem(item);
			area.checkIfItemPlaced(item);
			assertEquals(area.getScale().getCurrentWeight(), 3.0, 0.1); 
			// 0.1 handles the randomness when getting current weight of scale 
		} catch(Exception e) {
			fail("there should not have been an exception");
		}
	}

	@Test
	public void testWeightNotEqual() throws OverloadException {
		Numeral[] nums = {Numeral.valueOf((byte) 1), Numeral.valueOf((byte) 2), Numeral.valueOf((byte) 3), Numeral.valueOf((byte) 4)};
		Barcode barcode = new Barcode(nums);
		BarcodedItem item = new BarcodedItem(barcode, 3.0);
		Numeral[] wrongNums = {Numeral.valueOf((byte) 1), Numeral.valueOf((byte) 2), Numeral.valueOf((byte) 3), Numeral.valueOf((byte) 5)};
		Barcode wrongBarcode = new Barcode(wrongNums);
		BarcodedItem wrongItem = new BarcodedItem(wrongBarcode, 4.0);
		try {
			area.placeItem(wrongItem);
			area.checkIfItemPlaced(item);
			assertTrue(station.mainScanner.isDisabled());
		} catch(SimulationException e) {
			fail("there should not have been an exception");
		}
	}

	@Test
	public void testOverload() throws OverloadException {
		Numeral[] nums1 = {Numeral.valueOf((byte) 1), Numeral.valueOf((byte) 2), Numeral.valueOf((byte) 3), Numeral.valueOf((byte) 4)};
		Barcode barcode1 = new Barcode(nums1);
		BarcodedItem item1 = new BarcodedItem(barcode1, 499.0);

		Numeral[] nums2 = {Numeral.valueOf((byte) 2), Numeral.valueOf((byte) 4), Numeral.valueOf((byte) 6), Numeral.valueOf((byte) 8)};
		Barcode barcode2 = new Barcode(nums2);
		BarcodedItem item2 = new BarcodedItem(barcode2, 2.0);
		try {
			area.placeItem(item1);
			area.checkIfItemPlaced(item1);
			area.placeItem(item2);
			area.checkIfItemPlaced(item2);
			assertTrue(area.isOverloaded());
		} catch(SimulationException e) {
			fail("there should not have been an exception");
		}
	}
	
	
	
//	Added extra test cases, please verify before merging
	@Test
	public void testOutOfOverload() throws OverloadException {
		Numeral[] nums1 = {Numeral.valueOf((byte) 1), Numeral.valueOf((byte) 2), Numeral.valueOf((byte) 3), Numeral.valueOf((byte) 4)};
		Barcode barcode1 = new Barcode(nums1);
		BarcodedItem item1 = new BarcodedItem(barcode1, 499.0);

		Numeral[] nums2 = {Numeral.valueOf((byte) 2), Numeral.valueOf((byte) 4), Numeral.valueOf((byte) 6), Numeral.valueOf((byte) 8)};
		Barcode barcode2 = new Barcode(nums2);
		BarcodedItem item2 = new BarcodedItem(barcode2, 2.0);
		try {
			area.placeItem(item1);
			area.checkIfItemPlaced(item1);
			area.placeItem(item2);
			area.checkIfItemPlaced(item2);
			assertTrue(area.isOverloaded());
			area.getScale().remove(item2);
			assertFalse(area.isOverloaded());
		} catch(SimulationException e) {
			fail("there should not have been an exception");
		}
	}
	
	@Test
	public void testRemoveItem() {
		Numeral[] nums = {Numeral.valueOf((byte) 1), Numeral.valueOf((byte) 2), Numeral.valueOf((byte) 3), Numeral.valueOf((byte) 4)};
		Barcode barcode = new Barcode(nums);
		BarcodedItem item = new BarcodedItem(barcode, 3.0);
		try {
			area.placeItem(item);
			area.checkIfItemPlaced(item);
			area.removeItem(item);
			assertEquals(area.getScale().getCurrentWeight(), 0.0, 0.1);
		} catch(Exception e) {
			fail("there should not have been an exception");
		}
	}
	
	@Test
	public void testItemNotAddedAfterScanning()
	{
		Numeral[] nums = {Numeral.valueOf((byte) 1), Numeral.valueOf((byte) 2), Numeral.valueOf((byte) 3), Numeral.valueOf((byte) 4)};
		Barcode barcode = new Barcode(nums);
		BarcodedItem item = new BarcodedItem(barcode, 3.0);
		ScanItem scanItem = new ScanItem(station);
		scanItem.scanAnItem(item);
		assertTrue(station.mainScanner.isDisabled());
	}
	
	@Test
	public void testItemAddedAfterScanning() throws InterruptedException, OverloadException
	{
		Numeral[] nums = {Numeral.valueOf((byte) 1), Numeral.valueOf((byte) 2), Numeral.valueOf((byte) 3), Numeral.valueOf((byte) 4)};
		Barcode barcode = new Barcode(nums);
		BarcodedItem item = new BarcodedItem(barcode, 3.0);
		ScanItem scanItem = new ScanItem(station);
		area.setScanItem(scanItem);
		scanItem.scanAnItem(item);
		area.bagItemAfterScanning();
		assertFalse(station.mainScanner.isDisabled());
	}
	
	@Test (expected = SimulationException.class)
	public void testNotScanningBeforeBagging() throws OverloadException
	{
		Numeral[] nums = {Numeral.valueOf((byte) 1), Numeral.valueOf((byte) 2), Numeral.valueOf((byte) 3), Numeral.valueOf((byte) 4)};
		Barcode barcode = new Barcode(nums);
		BarcodedItem item = new BarcodedItem(barcode, 3.0);
		// this item has not been scanned 

		area.bagItemAfterScanning();

	}
	
	@Test 
	public void testItemTooLight() throws OverloadException
	{
		Numeral[] nums = {Numeral.valueOf((byte) 1), Numeral.valueOf((byte) 2), Numeral.valueOf((byte) 3), Numeral.valueOf((byte) 4)};
		Barcode barcode = new Barcode(nums);
		BarcodedItem item = new BarcodedItem(barcode, 0.00005);
		ScanItem scanItem = new ScanItem(station);
		area.setScanItem(scanItem);
		scanItem.scanAnItem(item);
		area.bagItemAfterScanning();
		assertTrue(area.isItemTooLight());

	}
	
	@Test
	public void addingCustomerBag()
	{
		Numeral[] nums = {Numeral.valueOf((byte) 1), Numeral.valueOf((byte) 2), Numeral.valueOf((byte) 3), Numeral.valueOf((byte) 4)};
		Barcode barcode = new Barcode(nums);
		BarcodedItem item = new BarcodedItem(barcode, 3.0);
		try {
			area.addCustomerBag();
			area.placeItem(item);
			area.checkIfItemPlaced(item);
			assertEquals(area.getScale().getCurrentWeight(), 3.0, 0.1); 
			// 0.1 handles the randomness when getting current weight of scale 
		} catch(Exception e) {
			fail("there should not have been an exception");
		}
	}
	
	@Test
	public void removingCustomerBag()
	{
		Numeral[] nums = {Numeral.valueOf((byte) 1), Numeral.valueOf((byte) 2), Numeral.valueOf((byte) 3), Numeral.valueOf((byte) 4)};
		Barcode barcode = new Barcode(nums);
		BarcodedItem item = new BarcodedItem(barcode, 3.0);
		try {
			area.addCustomerBag();
			area.placeItem(item);
			area.checkIfItemPlaced(item);
			area.removeCustomerBag();
			assertEquals(area.getScale().getCurrentWeight(), 3.0, 0.1); 
			// 0.1 handles the randomness when getting current weight of scale 
		} catch(Exception e) {
			fail("there should not have been an exception");
		}
	}
	
}