package tests;

import java.math.BigDecimal;
import java.util.Currency;

import org.junit.*;
import org.lsmr.selfcheckout.Card;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;

import controller.*;

public class TestPaymentCards {
	private CustomerCheckout checkoutTest;
    private SelfCheckoutStation station;
    private Card debitCard;
    private Card creditCard;
    private Card memberCard;
	
	 @Before
	 public void setup() {
		 Currency currency = Currency.getInstance("USD");
	     int[] ints = {5, 10, 20, 50};
	     BigDecimal[] decs = {new BigDecimal(".05"), new BigDecimal(".1"), new BigDecimal(".25")};
	     station = new SelfCheckoutStation(currency, ints, decs, 500, 1);
	 }
	 
	 @Test
	 public void memberReciept() {
		 
	 }
	 
	 @Test
	 public void payMix() {
		 
	 }
}
