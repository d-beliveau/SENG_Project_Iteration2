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
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.lsmr.selfcheckout.devices.observers.AbstractDeviceObserver;
import org.lsmr.selfcheckout.devices.observers.BarcodeScannerObserver;
import org.lsmr.selfcheckout.devices.observers.CardReaderObserver;

/**
 *  Controller class for 'customer scans item' use case
 */
public class ScanItem implements BarcodeScannerObserver{

	protected Dictionary<Barcode, BarcodedProduct> Products = new Hashtable<Barcode, BarcodedProduct>();
	protected List <BarcodedItem> Scanneditems = new ArrayList<BarcodedItem>();

	//Key variables
	private BigDecimal billprice;
	private BigDecimal productprice;
	private BarcodedProduct p;
	private AtomicBoolean hasItemBeenBagged = new AtomicBoolean();
	
	private SelfCheckoutStation station;

	private boolean scanned = false;	
	
	public ScanItem(SelfCheckoutStation station) {
		this.station = station;
		station.mainScanner.attach(this);
		hasItemBeenBagged.set(false);
	}
	
	/**
	 * Construct scanner observer from checkout station
	 * @param item
	 * 		Item being scanned
	 * @return
	 */
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
	
	/**
	 * Add product to product list
	 * @param Product
	 * 			Product to be added to the list
	 */
	public void addProduct(BarcodedProduct Product) {
		this.Products.put(Product.getBarcode(), Product);

	}
	
	public void removeItem(BarcodedItem item) {
		this.Scanneditems.remove(item);
	}
	
	/**
	 * Remove product from product list
	 * @param Product
	 * 			Product to be removed from the list
	 */
	public void removeProduct(BarcodedProduct Product) {
		this.Products.remove(Product.getBarcode());
	}
	
	public List<BarcodedItem> getScanneditems() {
		return Scanneditems;
	}
	
	public Dictionary<Barcode, BarcodedProduct> getProducts() {
		return Products;
	}
	
	/**
	 * BARCODE OBSERVERS IMPLEMENTATION
	 */
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
		station.mainScanner.disable();
		MultithreadingDemo waitForCustomer = new MultithreadingDemo(station, hasItemBeenBagged);
		waitForCustomer.start();
	}
	
	/**
	 * Gives whether item was successfully scanned
	 * @return true if item is scanned successfully
	 */
	public boolean scanResult() {
		return this.scanned;
	}

	/**
	 * Take item barcode to find corresponding product
	 * @param currentcode
	 * 			Barcode scanned
	 * @return
	 */
	public BarcodedProduct ItemAsProduct(Barcode currentcode) {
		p = Products.get(currentcode);
		if (p == null) {
			throw new SimulationException("Product not in catalog");
		}
		return p;
	}
	
	/**
	 * Take item barcode to find corresponding product price
	 * @param currentcode
	 * 			Barcode scanned
	 * @return
	 */
	public BigDecimal ItemAsProductPrice(Barcode currentcode) {
		//Don't need to check if price is bugged/negative because BigDecimal doesn't permit
		return Products.get(currentcode).getPrice();
	}
	
	/**
	 * Loop to add product prices to bill price
	 */
	private void TallyBillPrice() {
		billprice = new BigDecimal(0.00);
		for (int i = 0; i < Scanneditems.size(); i++) {
			productprice = ItemAsProductPrice(Scanneditems.get(i).getBarcode());
			billprice = billprice.add(productprice);
		}
		
	}
	
	public boolean getHasItemBeenBagged() {
		return hasItemBeenBagged.get();
	}
	
	public BigDecimal GetBillPrice(BigDecimal partialPayment) {
		TallyBillPrice();
		billprice = billprice.subtract(partialPayment);
		return billprice;
	}
	
	public void SetBillPrice(BigDecimal price) {
		billprice = price;
	}
	
}
/**
 * Implementing concurrency for the timer which waits for 5 seconds for 
 * the user to place the item on the scale.
 */
class MultithreadingDemo extends Thread {
	private SelfCheckoutStation station;
	private AtomicBoolean hasItemBeenBagged;
	
	public MultithreadingDemo(SelfCheckoutStation station, AtomicBoolean hasItemBeenBagged) {
		this.station = station;
		this.hasItemBeenBagged = hasItemBeenBagged;
	}
	
    public void run()
    {
    	
		double duration =0;
		LocalDateTime initialTime = LocalDateTime.now();
		hasItemBeenBagged.set(false);
		
	
		while(duration < 5) {
			duration = ChronoUnit.SECONDS.between(initialTime, LocalDateTime.now());
			//baggingArea.bagItemAfterScanning
			if(station.mainScanner.isDisabled() == false) { //if scanner is enabled
				hasItemBeenBagged.set(true);;
				break;
			}
		}
		
    }
}
