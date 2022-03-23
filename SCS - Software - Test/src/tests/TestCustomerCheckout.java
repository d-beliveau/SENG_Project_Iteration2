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

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;
import org.lsmr.selfcheckout.devices.SimulationException;

import controller.CustomerCheckout;

import java.math.BigDecimal;
import java.util.Currency;

// Test suite for CustomerCheckout
public class TestCustomerCheckout {
    private CustomerCheckout checkoutTest;
    private SelfCheckoutStation station;

    @Before
    public void setup() {
        Currency currency = Currency.getInstance("USD");
        int[] ints = {5, 10, 20, 50};
        BigDecimal[] decs = {new BigDecimal(".05"), new BigDecimal(".1"), new BigDecimal(".25")};
        station = new SelfCheckoutStation(currency, ints, decs, 500, 1);
    }
    
    @Test
    public void testCheckout() {
        try {
            checkoutTest = new CustomerCheckout(station);
            assertTrue(station.scanner.isDisabled());
            assertTrue(station.scale.isDisabled());
            assertFalse(station.coinSlot.isDisabled());
            assertFalse(station.coinValidator.isDisabled());
            assertFalse(station.coinStorage.isDisabled());
            assertFalse(station.coinTray.isDisabled());
            assertFalse(station.banknoteInput.isDisabled());
            assertFalse(station.banknoteValidator.isDisabled());
            assertFalse(station.banknoteStorage.isDisabled());
            assertFalse(station.banknoteOutput.isDisabled());
        }
        catch (Exception e) {
            fail("there should not have been an exception");
        }
    }

    @Test(expected = SimulationException.class)
    public void testFailCheckout() {
        station.coinSlot.forceErrorPhase();
        checkoutTest = new CustomerCheckout(station);
    }
}