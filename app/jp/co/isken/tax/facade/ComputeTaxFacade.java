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
	
	private ComputeRounding round;
	private Date date;
	private ComputeType computeType;
	private ComputeConsumptionTax cal;
	
	public ComputeTaxFacade(RoundingRule round, Date date, ComputeType computeType) {
		//まるめ計算変換
		this.round = round.getComputeRounding();
		this.date = date;
		this.computeType = computeType;
	}
	
	public double calcConsumptionTax(Item item, double price) {
		//税品目変換
		setCalculator(item.getTaxItem());
		return cal.calcTax(price);
	}
	
	/*
	 * 消費税計算機の設定
	 */
	private void setCalculator(TaxItem item) {
		if(item == null) {
			if(computeType.equals(ComputeType.外税)) {
				this.cal = new ComputeExclusive(round, date);
			}
			else if(computeType.equals(ComputeType.内税)) {
				this.cal = new ComputeInclusive(round, date);
			}
			else if(computeType.equals(ComputeType.非課税)) {
				this.cal = new ComputeFree(round, date);
			}
		}
		else {
			if(computeType.equals(ComputeType.外税)) {
				this.cal = new ComputeExclusive(round, date, item);
			}
			else if(computeType.equals(ComputeType.内税)) {
				this.cal = new ComputeInclusive(round, date, item);
			}
			else if(computeType.equals(ComputeType.非課税)) {
				this.cal = new ComputeFree(round, date, item);
			}
		}
	}
}
