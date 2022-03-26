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

import org.lsmr.selfcheckout.Barcode;
import org.lsmr.selfcheckout.BarcodedItem;
import org.lsmr.selfcheckout.Card.CardData;
import org.lsmr.selfcheckout.devices.AbstractDevice;
import org.lsmr.selfcheckout.devices.BarcodeScanner;
import org.lsmr.selfcheckout.devices.CardReader;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;
import org.lsmr.selfcheckout.devices.SimulationException;
import org.lsmr.selfcheckout.products.BarcodedProduct;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import org.lsmr.selfcheckout.devices.observers.AbstractDeviceObserver;
import org.lsmr.selfcheckout.devices.observers.BarcodeScannerObserver;
import org.lsmr.selfcheckout.devices.observers.CardReaderObserver;

// Controller class for 'customer scans item' use case
public class ScanItem implements BarcodeScannerObserver{

	private Dictionary<Barcode, BarcodedProduct> Products = new Hashtable<Barcode, BarcodedProduct>();
	private	List <BarcodedItem> Scanneditems = new ArrayList<BarcodedItem>();

	//Key variables
	private BigDecimal billprice;
	private BigDecimal productprice;
	private BarcodedProduct p;
	
	private SelfCheckoutStation station;

	private boolean scanned = false;	
	
	public ScanItem(SelfCheckoutStation station) {
		this.station = station;
		station.mainScanner.attach(this);
	}
	
	//Construct scanner observer from checkout station
	public boolean scanAnItem(BarcodedItem item) {
		
		station.mainScanner.scan(item);
		if (this.scanResult() == true) {
			Scanneditems.add(item);
			
			//!!! COMMENTED THIS LINE OUT FOR TESTING !!!
			//scanBaggingItem(item);
			
			return true;
			
		} else {
			return false;
		}
	}
	
	//Add product to product list
	public void addProduct(BarcodedProduct Product) {
		this.Products.put(Product.getBarcode(), Product);

	}
	
	public void removeItem(BarcodedItem item) {
		this.Scanneditems.remove(item);
	}
	
	//Remove product from product list
	public void removeProduct(BarcodedProduct Product) {
		this.Products.remove(Product.getBarcode());
	}
	
	//Scanneditem getter
	public List<BarcodedItem> getScanneditems() {
		return Scanneditems;
	}
	
	//Product Dictionary Getter
	public Dictionary<Barcode, BarcodedProduct> getProducts() {
		return Products;
	}
	
	//BARCODE OBSERVERS IMPLEMENTATION
	@Override
	public void enabled(AbstractDevice<? extends AbstractDeviceObserver> device) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void disabled(AbstractDevice<? extends AbstractDeviceObserver> device) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void barcodeScanned(BarcodeScanner barcodeScanner, Barcode barcode) {
		// TODO Auto-generated method stub
		this.scanned = true;
	}
	
	//Gives whether item was successfully scanned
	public boolean scanResult() {
		return this.scanned;
	}

	//Take item barcode to find corresponding product
	public BarcodedProduct ItemAsProduct(Barcode currentcode) {
		p = Products.get(currentcode);
		if (p == null) {
			throw new SimulationException("Product not in catalog");
		}
		return p;
	}
	
	//Take item barcode to find corresponding product price
	public BigDecimal ItemAsProductPrice(Barcode currentcode) {
		//Don't need to check if price is bugged/negative because BigDecimal doesn't permit
		return Products.get(currentcode).getPrice();
	}
	
	//Loop to add product prices to bill price
	private void TallyBillPrice() {
		billprice = new BigDecimal(0.00);
		for (int i = 0; i < Scanneditems.size(); i++) {
			productprice = ItemAsProductPrice(Scanneditems.get(i).getBarcode());
			billprice = billprice.add(productprice);
		}
		
	}
	
	
	
	//BillPrice Getter
	public BigDecimal GetBillPrice(BigDecimal partialPayment) {
		TallyBillPrice();
		billprice = billprice.subtract(partialPayment);
		return billprice;
	}
	
	//BillPrice Setter
	public void SetBillPrice(BigDecimal price) {
		billprice = price;
	}

	
}