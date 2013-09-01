package jp.co.isken.taxlib.application;

import java.util.Date;

import jp.co.isken.taxlib.domain.TaxItem;

public class ComputeFree extends ComputeConsumptionTax {
	
	public ComputeFree(ComputeRounding round, Date date) {
		super(round, date);
	}

	public ComputeFree(ComputeRounding round, Date date, TaxItem item) {
		super(round, date, item);
	}

	@Override
	public double calcTax(double price) {
		return 0.0;
	}

}
