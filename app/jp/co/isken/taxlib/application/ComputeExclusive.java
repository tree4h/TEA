package jp.co.isken.taxlib.application;

import java.util.Date;
import java.math.BigDecimal;

import jp.co.isken.taxlib.domain.TaxItem;

public class ComputeExclusive extends ComputeConsumptionTax {
	
	public ComputeExclusive(ComputeRounding round, Date date) {
		super(round, date);
	}

	public ComputeExclusive(ComputeRounding round, Date date, TaxItem item) {
		super(round, date, item);
	}

	@Override
	public double calcTax(double price) {
		BigDecimal bdprice = new BigDecimal(price);
		BigDecimal bdrate = new BigDecimal(rate);
		return round.calc(bdprice.multiply(bdrate).doubleValue());
	}

}
