package controller;

import org.lsmr.selfcheckout.Card.CardData;
import org.lsmr.selfcheckout.devices.AbstractDevice;
import org.lsmr.selfcheckout.devices.CardReader;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;
import org.lsmr.selfcheckout.devices.observers.AbstractDeviceObserver;
import org.lsmr.selfcheckout.devices.observers.CardReaderObserver;

public class MembershipCard implements CardReaderObserver{

	
	private SelfCheckoutStation station;
	
	
	public MembershipCard(SelfCheckoutStation station) {
		this.station = station;
		station.cardReader.attach(this);
	}
	
	@Override
	public void enabled(AbstractDevice<? extends AbstractDeviceObserver> device) {
		// Ignore this
		
	}

	@Override
	public void disabled(AbstractDevice<? extends AbstractDeviceObserver> device) {
		// Ignore this
		
	}

	@Override
	public void cardInserted(CardReader reader) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void cardRemoved(CardReader reader) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void cardTapped(CardReader reader) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void cardSwiped(CardReader reader) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void cardDataRead(CardReader reader, CardData data) {
		// TODO Auto-generated method stub
		
	}

}
