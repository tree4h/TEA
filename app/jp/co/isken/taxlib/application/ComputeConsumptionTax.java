package jp.co.isken.taxlib.application;

import java.util.Date;

import jp.co.isken.tax.rdb.RDBOperator;
import jp.co.isken.taxlib.domain.TaxItem;

public abstract class ComputeConsumptionTax {
	
	protected ComputeRounding round;	//丸め計算
	protected double rate;			//消費税率
	
	//税品目がある場合
	public ComputeConsumptionTax(ComputeRounding round, Date date, TaxItem item) {
		this.round = round;
		this.rate = RDBOperator.$findTaxRate(date, item);
	}
	
	//税品目がない場合（日本の消費税はココ）
	public ComputeConsumptionTax(ComputeRounding round, Date date) {
		this.round = round;
		this.rate = RDBOperator.$findTaxRate(date);
	}
	
	//消費税の計算
	public abstract double calcTax(double price);
}
