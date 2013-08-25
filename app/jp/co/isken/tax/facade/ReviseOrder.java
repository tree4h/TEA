package jp.co.isken.tax.facade;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.co.isken.tax.domain.commerce.CommercialTransaction;
import jp.co.isken.tax.domain.commerce.CommercialEntry;
import jp.co.isken.tax.domain.contract.RoundingRule;
import jp.co.isken.tax.domain.contract.TaxUnitRule;
import jp.co.isken.tax.domain.item.Goods;
import jp.co.isken.tax.domain.item.Item;
import jp.co.isken.tax.domain.payment.PaymentTransaction;
import jp.co.isken.tax.domain.commerce.ComputeType;

import jp.co.isken.tax.rdb.RDBOperator;

public class ReviseOrder {
	//キャンセル対象CT
	private CommercialTransaction ct;
	//キャンセル対象CE
	private Map<CommercialEntry, Double> oldCEs = new HashMap<CommercialEntry, Double>();
	//新規作成CE
	private List<CommercialEntry> newCEs = new ArrayList<CommercialEntry>();
	//キャンセル対象取引の課税単位
	private TaxUnitRule rule;
	//キャンセル対象取引の消費税計算方法
	private ComputeType computeType;
	//商流取引の取引日
	private Date dealDate;
	//消費税丸め計算ルール
	private RoundingRule taxRoudingRule;
	
	public ReviseOrder(long reviceCT, Map<Long, Double> reviceCE) {
		this.ct = RDBOperator.$findCT(reviceCT);
		System.out.println("init():"+reviceCE.size());
		for(Map.Entry<Long, Double> e : reviceCE.entrySet()) {
			CommercialEntry ce = RDBOperator.$findCE(e.getKey());
			oldCEs.put(ce, e.getValue());
		}
		this.computeType = ct.getComputeType();
		this.rule = ct.getContract().getTaxCondition().getTaxUnitRule();
		this.dealDate = ct.getWhenCharged();
		this.taxRoudingRule = ct.getRoundingRule();
	}
	/*
	 * 
	 */
	public void addPayment() {
		this.reviceCE();
		
		if(rule == TaxUnitRule.DETAIL) {
			this.addPaymentByDETAIL();
		}
		else if(rule == TaxUnitRule.SUM) {
			this.addPaymentBySUM();
		}
		else {
			List<CommercialEntry> ces = RDBOperator.$findCEs(ct);
			double tax_sum = this.calTaxBySUM(ces);
			double tax_detail = this.calTaxByDETAIL(ces);
			if(tax_sum < tax_detail) {
				this.addPaymentBySUM();
			}
			else {
				this.addPaymentByDETAIL();
			}
		}
	}
	
	/*
	 * 商流移動の修正
	 * 1.赤CEの作成
	 * 2.元CEを参照しているPT,PEの削除
	 * 3.修正後CEの作成
	 */
	private void reviceCE() {
		for(Map.Entry<CommercialEntry, Double> ce : oldCEs.entrySet()) {
			CommercialEntry oldCE = ce.getKey();
			double newAmount = ce.getValue();
			newCEs.add(oldCE.cancel(newAmount));
		}
	}
	
	private void addPaymentByDETAIL() {
		for(CommercialEntry ce : newCEs) {
			PaymentTransaction pt = ce.createPT();
			ce.createPEPrice(pt);
			ce.createPETax(pt);
		}
	}
	private void addPaymentBySUM() {
		for(CommercialEntry ce : newCEs) {
			PaymentTransaction pt = ce.createPT();
			ce.createPEPrice(pt);
		}
		//消費税移動の作成
		ct.cancel();
		PaymentTransaction pt = ct.createPT();
		ct.createPETax(pt);
	}

	/*
	 * 課税単位＝合計
	 */
	private double calTaxBySUM(List<CommercialEntry> ces) {
		//合計代金の計算
		double sum = 0.0;
		for (CommercialEntry ce : ces) {
			sum += ce.calcPayment();
		}
		//消費税計算
		Item item = ct.getItem();
		ComputeTaxFacade taxCal = new ComputeTaxFacade(item, taxRoudingRule, sum, dealDate, computeType);
		return taxCal.calcConsumptionTax();
	}

	/*
	 * 課税単位＝明細
	 */
	private double calTaxByDETAIL(List<CommercialEntry> ces) {
		double tax_detail = 0.0;
		for (CommercialEntry ce : ces) {
			Goods goods = ce.getAccount().getGoods();
			Item item = goods.getItem();
			//代金計算
			double price = ce.calcPayment();
			//消費税計算
			ComputeTaxFacade taxCal = new ComputeTaxFacade(item, taxRoudingRule, price, dealDate, computeType);
			double tmp = taxCal.calcConsumptionTax();
			tax_detail += tmp;
		}
		return tax_detail;
	}

}
