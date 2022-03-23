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

package tests;

import org.lsmr.selfcheckout.Barcode;
import org.lsmr.selfcheckout.BarcodedItem;
import org.lsmr.selfcheckout.Numeral;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;
import org.lsmr.selfcheckout.devices.SimulationException;
import org.lsmr.selfcheckout.products.BarcodedProduct;

import controller.ScanItem;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Dictionary;
import java.util.Hashtable;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

// Test suite for ScanItem
public class TestScanItem {

	//Station Set-up
    private Currency c = Currency.getInstance("CAD");
    private int[] banknoteDenominations = {5, 10, 20, 50, 100};
    private BigDecimal[] coinDenominations = {new BigDecimal(.05), new BigDecimal(.10), new BigDecimal(.25)};
    private SelfCheckoutStation station;
    private ScanItem software;
    
    private Dictionary <Barcode, BarcodedProduct> Empty;
    private BigDecimal Price;
    private boolean Status;
    
    //Numerals
    private Numeral[] soupCode = new Numeral[] {Numeral.valueOf((byte)0b000)};
    private Numeral[] doritoCode = new Numeral[] {Numeral.valueOf((byte)0b111)};

    //Barcodes
    private Barcode soupBar = new Barcode(soupCode);
    private Barcode doritoBar = new Barcode(doritoCode);

    //Price
    private BigDecimal soupPrice = new BigDecimal(3.00);
    private BigDecimal doritoPrice = new BigDecimal(2.00);
    
    //Items
    private BarcodedItem soupItem;
    private BarcodedItem doritoItem;

    //Products
    private BarcodedProduct soupProd;
    private BarcodedProduct doritoProd;
    
    @Before
    public void setup()
    {
    	//Set-up station
        this.station = new SelfCheckoutStation(c, banknoteDenominations, coinDenominations, 10000, 1);
        this.software = new ScanItem(station);
        
        //Set-up items
        this.soupItem = new BarcodedItem(soupBar, 50);
        this.doritoItem = new BarcodedItem(doritoBar, 50);

        //Set-up products
        this.soupProd = new BarcodedProduct(soupBar,"Soup",soupPrice);
        this.doritoProd = new BarcodedProduct(doritoBar,"Soup",doritoPrice);
    }

    //BASIC FUNCTIONALITY TESTING
    
    //Test for when scanning item
    @Test
    public void scanAnItem()
    {
    	//Scan item until success
    	do 
    	{
    		Status = this.software.scanAnItem(doritoItem);
    	}
    	while(this.Status == false);
    	//Comparing if the item just scanned is the right item
    	assertEquals(doritoItem, this.software.getScanneditems().get(0));
    }    
    
    //Test for when scanning an item fails
    @Test
    public void scanAnItemFail()
    {
    	//Scan dorito item until fail
    	do
    	{
        	this.software = new ScanItem(station);
    		Status = this.software.scanAnItem(doritoItem);
    		this.software.removeItem(doritoItem);
    	} 
    	while(this.Status == true);
    	
    	//Check if fail status is failed
    	assertEquals(this.software.scanResult(), this.Status);
    }
    
    //Scanning multiple of same items
    @Test
    public void scanMultiItemSame()
    {
    	//Add dorito item
    	do 
    	{
    		Status = this.software.scanAnItem(doritoItem);
    	}
    	while(this.Status == false);
    	
    	//Add dorito item
    	do 
    	{
    		Status = this.software.scanAnItem(doritoItem);
    	}
    	while(this.Status == false);
    	
    	//Comparing if the item just scanned is the right item
    	assertEquals(2, this.software.getScanneditems().size());
    }    
    
    //Scanning multiple of different items
    @Test 
    public void scanMultiItemDiff()
    {
    	
    	//Add soup item
    	do 
    	{
    		Status = this.software.scanAnItem(soupItem);
    	}
    	while(this.Status == false);
    	
    	//Add dorito item
    	do 
    	{
    		Status = this.software.scanAnItem(doritoItem);
    	}
    	while(this.Status == false);
    	
    	//Comparing if the item just scanned is the right item
    	assertEquals(2, this.software.getScanneditems().size());
    }   
    
    //Test when adding product to catalog succeeds
    @Test
    public void testAddProduct()
    {
    	//Add product
    	this.software.addProduct(doritoProd);
    	//Check if product added to catalog
    	assertEquals(doritoProd,this.software.getProducts().get(doritoBar));
    }
    
    //Test when removing product from catalog succeeds
    @Test
    public void testRemoveProduct()
    {
    	//Make product list
    	Empty = new Hashtable<Barcode, BarcodedProduct>();
    	this.software.addProduct(doritoProd);
    	//Remove product from product list
    	this.software.removeProduct(doritoProd);
    	//Check if removed item success
    	assertEquals(Empty,this.software.getProducts());
    }
    
    //Test removing nonexistent product
    @Test
    public void testRemoveNonExistProduct()
    {
    	//remove nonexistent product
    	this.software.removeProduct(doritoProd);
    }
    
    //MISC FUNCTIONALITY TESTING

    //Test converting scanned item into product 
    @Test
    public void testItemAsProduct()
    {
    	//Scan dorito item
    	do 
    	{
            this.software.scanAnItem(doritoItem);
    	} 
    	while(this.software.scanResult() == false);
    	
    	//Check if prod converted
    	this.software.addProduct(doritoProd);
    	assertEquals(doritoProd,this.software.ItemAsProduct(doritoBar));
    }
    
    //Test if item not in catalog can be found as product
    @Test (expected = SimulationException.class)
    public void testNonExistItemAsProduct()
    {
    	this.software.ItemAsProduct(doritoBar);
    }
    
    //Test converting scanned item and finding product price
    @Test
    public void testItemAsProductPrice()
    {
    	//Scan dorito item
    	do 
    	{
            this.software.scanAnItem(doritoItem);
    	} 
    	while(this.software.scanResult() == false);
        
    	//Compare to price of doritoProd
    	this.software.addProduct(doritoProd);
    	assertEquals(doritoPrice,this.software.ItemAsProductPrice(doritoBar));
    }
    
    //Preliminary test to tally cost a product
    @Test
    public void testGetBillPrice()
    {
    	//set-up item
    	this.software.addProduct(doritoProd);
    	
    	//Scan dorito item
    	do 
    	{
            this.software.scanAnItem(doritoItem);
    	} 
    	while(this.software.scanResult() == false);
    	
    	//Compare price of single dorito item
    	assertEquals(doritoPrice,this.software.GetBillPrice());
    }
    
    //Test to tally cost of multiple products
    @Test    
    public void testMultiGetBillPrice()
    {
    	//Set-up items + static price
    	this.software.addProduct(doritoProd);
    	this.software.addProduct(soupProd);
    	this.Price = new BigDecimal(5.00);
    	
    	//Scan dorito item
    	do 
    	{
            this.software.scanAnItem(doritoItem);
    	} 
    	while(this.software.scanResult() == false);
    	
    	//Scan soup item
    	do 
    	{
            this.software.scanAnItem(soupItem);
    	} 
    	while(this.software.scanResult() == false);
    	
    	//Compare bill price
    	assertEquals(Price,this.software.GetBillPrice());
    }

    //Test observer is disabled
    @Test
    public void testDisable()
    {
    	this.software.disabled(station.scanner);
    }
    
    //Test observer is enabled
    @Test
    public void testEnable()
    {
    	this.software.enabled(station.scanner);
    }
}