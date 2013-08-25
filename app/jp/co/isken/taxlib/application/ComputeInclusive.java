package jp.co.isken.taxlib.application;

import java.util.Date;
import java.math.BigDecimal;

import jp.co.isken.taxlib.domain.TaxItem;

public class ComputeInclusive extends ComputeConsumptionTax {
	
	public ComputeInclusive(double price, ComputeRounding round, Date date) {
		super(price, round, date);
		// TODO Auto-generated constructor stub
	}

	public ComputeInclusive(double price, ComputeRounding round, Date date, TaxItem item) {
		super(price, round, date, item);
		// TODO Auto-generated constructor stub
	}

	@Override
	public double calcTax() {
		BigDecimal bdprice = new BigDecimal(this.price);
		BigDecimal bdrate0 = new BigDecimal(this.rate);
		BigDecimal bdrate1 = new BigDecimal("1.0");

		BigDecimal p = bdprice.multiply(bdrate0);
		BigDecimal r = bdrate1.add(bdrate0);
	
		return round.divide(p.doubleValue(), r.doubleValue());
	}

}
