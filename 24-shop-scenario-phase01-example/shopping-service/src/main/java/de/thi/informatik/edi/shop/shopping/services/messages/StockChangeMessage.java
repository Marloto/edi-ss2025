package de.thi.informatik.edi.shop.shopping.services.messages;

public class StockChangeMessage {
	private double value;
	
	public StockChangeMessage() {
	}
	
	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}
}
