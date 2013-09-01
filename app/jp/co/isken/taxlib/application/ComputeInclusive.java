package jp.co.isken.taxlib.application;

import java.util.Date;
import java.math.BigDecimal;

import jp.co.isken.taxlib.domain.TaxItem;

public class ComputeInclusive extends ComputeConsumptionTax {
	
	public ComputeInclusive(ComputeRounding round, Date date) {
		super(round, date);
	}

	public ComputeInclusive(ComputeRounding round, Date date, TaxItem item) {
		super(round, date, item);
	}

	@Override
	public double calcTax(double price) {
		BigDecimal bdprice = new BigDecimal(price);
		BigDecimal bdrate0 = new BigDecimal(rate);
		BigDecimal bdrate1 = new BigDecimal("1.0");

		BigDecimal p = bdprice.multiply(bdrate0);
		BigDecimal r = bdrate1.add(bdrate0);
	
		return round.divide(p.doubleValue(), r.doubleValue());
	}

}
