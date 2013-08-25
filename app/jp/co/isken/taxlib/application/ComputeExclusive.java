package jp.co.isken.taxlib.application;

import java.util.Date;
import java.math.BigDecimal;

import jp.co.isken.taxlib.domain.TaxItem;

public class ComputeExclusive extends ComputeConsumptionTax {
	
	public ComputeExclusive(double price, ComputeRounding round, Date date) {
		super(price, round, date);
		// TODO Auto-generated constructor stub
	}

	public ComputeExclusive(double price, ComputeRounding round, Date date, TaxItem item) {
		super(price, round, date, item);
		// TODO Auto-generated constructor stub
	}

	@Override
	public double calcTax() {
		BigDecimal bdprice = new BigDecimal(this.price);
		BigDecimal bdrate = new BigDecimal(this.rate);
		return round.calc(bdprice.multiply(bdrate).doubleValue());
	}

}
