package jp.co.isken.taxlib.application;

import java.util.Date;

import jp.co.isken.taxlib.domain.TaxItem;

public class ComputeFree extends ComputeConsumptionTax {
	
	public ComputeFree(double price, ComputeRounding round, Date date) {
		super(price, round, date);
		// TODO Auto-generated constructor stub
	}

	public ComputeFree(double price, ComputeRounding round, Date date, TaxItem item) {
		super(price, round, date, item);
		// TODO Auto-generated constructor stub
	}

	@Override
	public double calcTax() {
		return 0.0;
	}

}
