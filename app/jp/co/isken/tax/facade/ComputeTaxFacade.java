package jp.co.isken.tax.facade;

import java.util.Date;

import jp.co.isken.tax.domain.contract.RoundingRule;
import jp.co.isken.tax.domain.item.Item;
import jp.co.isken.taxlib.application.ComputeConsumptionTax;
import jp.co.isken.taxlib.application.ComputeExclusive;
import jp.co.isken.taxlib.application.ComputeFree;
import jp.co.isken.taxlib.application.ComputeInclusive;
import jp.co.isken.taxlib.application.ComputeRounding;
import jp.co.isken.taxlib.domain.TaxItem;
import jp.co.isken.tax.domain.commerce.ComputeType;

public class ComputeTaxFacade {
	
	private TaxItem item;
	private ComputeRounding round;
	private double price;
	private Date date;
	//消費税計算
	private ComputeConsumptionTax cal;
	
	public ComputeTaxFacade(Item item, RoundingRule round, double price, Date date, ComputeType computeType) {
		//品目変換
		this.item = item.getTaxItem();
		//まるめ計算変換
		this.round = round.getComputeRounding();
		this.price = price;
		this.date = date;
		this.setCalculator(computeType);
	}
	
	private void setCalculator(ComputeType computeType) {
		if(item == null) {
			if(computeType.equals(ComputeType.外税)) {
				this.cal = new ComputeExclusive(price, round, date);
			}
			else if(computeType.equals(ComputeType.内税)) {
				this.cal = new ComputeInclusive(price, round, date);
			}
			else if(computeType.equals(ComputeType.非課税)) {
				this.cal = new ComputeFree(price, round, date);
			}
		}
		else {
			if(computeType.equals(ComputeType.外税)) {
				this.cal = new ComputeExclusive(price, round, date, item);
			}
			else if(computeType.equals(ComputeType.内税)) {
				this.cal = new ComputeInclusive(price, round, date, item);
			}
			else if(computeType.equals(ComputeType.非課税)) {
				this.cal = new ComputeFree(price, round, date, item);
			}
		}
	}
	
	public double calcConsumptionTax() {
		return this.cal.calcTax();
	}
}
