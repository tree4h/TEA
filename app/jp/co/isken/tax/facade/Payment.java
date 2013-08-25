package jp.co.isken.tax.facade;

import jp.co.isken.tax.domain.commerce.CommercialEntry;
import jp.co.isken.tax.domain.commerce.CommercialTransaction;
import jp.co.isken.tax.domain.contract.RoundingRule;
import jp.co.isken.tax.domain.contract.TaxUnitRule;
import jp.co.isken.tax.domain.item.Goods;
import jp.co.isken.tax.domain.item.Item;
import jp.co.isken.tax.domain.item.Unit;
import jp.co.isken.tax.domain.payment.PaymentAccount;
import jp.co.isken.tax.domain.payment.PaymentAccountType;
import jp.co.isken.tax.domain.payment.PaymentEntry;
import jp.co.isken.tax.domain.payment.PaymentTransaction;
import jp.co.isken.tax.rdb.RDBOperator;

import jp.co.isken.tax.domain.commerce.ComputeType;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//代金計算を行うクラス
//数量と商品単価から合計代金を計算し，丸め処理をして返す
public class Payment {
	//計算属性
	private TaxUnitRule taxUnitRule;
	private Date dealDate;	//商流取引の取引日
	private RoundingRule taxRoudingRule;
	private RoundingRule payRoudingRule;
	private ComputeType computeType;
	private CommercialTransaction ct;
	private List<CommercialEntry> ces;
	
	//代金
	private Map<Goods, Double> payments = new HashMap<Goods, Double>();
	//消費税
	private double tax;
	
	private List<PaymentTransaction> payTransaction = new ArrayList<PaymentTransaction>();
	private List<PaymentEntry> payEntry = new ArrayList<PaymentEntry>();
	private PaymentAccount goodsPA;
	private PaymentAccount taxPA;

	public Payment(Order order) {
		this.taxUnitRule = order.getTaxUnitRule();
		this.dealDate = order.getCommercialTransaction().getWhenCharged();
		this.taxRoudingRule = order.getTaxRoundingRule();
		this.payRoudingRule = order.getPayRoundingRule();
		this.computeType = order.getCommercialTransaction().getComputeType();
		this.ct = order.getCommercialTransaction();
		this.ces = order.getCommercialEtnries();
		this.goodsPA = RDBOperator.$findPA(order.getClient(), PaymentAccountType.商品代金);
		this.taxPA = RDBOperator.$findPA(order.getClient(), PaymentAccountType.消費税);
	}

	/*
	 * 外部ＩＦ
	 * 課税単位の振り分けを行う
	 */
	public void addPayment() {
		if(taxUnitRule == TaxUnitRule.DETAIL) {
			this.addPaymentByDETAIL();
		}
		else if(taxUnitRule == TaxUnitRule.SUM) {
			this.addPaymentBySUM();
		}
		else {
			double tax_sum = this.calTaxBySUM();
			double tax_detail = this.calTaxByDETAIL();
			if(tax_sum < tax_detail) {
				this.addPaymentBySUM();
			}
			else {
				this.addPaymentByDETAIL();
			}
		}
	}
	/*
	 * 課税単位＝合計
	 */
	private void addPaymentBySUM() {
		double sumPrice = 0.0;
		//商流移動根拠となる代金取引,代金移動（商品代金）の作成
		for (CommercialEntry ce : ces) {
			double price = this.addPaymentBySUM(ce);
			sumPrice += price;
		}
		//商流取引根拠となる代金取引、代金移動（消費税）の作成
		this.addPaymentBySUM(ct, sumPrice);
	}
	/*
	 * 課税単位＝合計時
	 * CE根拠となるPT,PE（商品代金）の作成
	 * 代金を返す
	 */
	private double addPaymentBySUM(CommercialEntry ce) {
		//代金取引の作成
		PaymentTransaction pt = new PaymentTransaction(ct.getWhenCharged(), ce);
		//代金計算
		double price = this.calcPayment(ce);
		//代金移動の作成
		PaymentEntry pe = PaymentEntry.create(price, pt, goodsPA);
		
		//代金登録
		this.payments.put(ce.getAccount().getGoods(), pe.getPrice());
		//代金取引の登録
		this.payTransaction.add(pt);
		//代金移動の登録
		this.payEntry.add(pe);
		
		return price;
	}
	/*
	 * 課税単位＝合計時
	 * CT根拠となるPT,PE（消費税）の作成
	 */
	private void addPaymentBySUM(CommercialTransaction ct, double sumPrice) {
		Date dealDate = ct.getWhenCharged();
		//代金取引の作成
		PaymentTransaction pt = new PaymentTransaction(dealDate, ct);
		//消費税計算
		Item item = ct.getItem();
		ComputeTaxFacade taxCal = new ComputeTaxFacade(item, taxRoudingRule, sumPrice, dealDate, computeType);
		double tax = taxCal.calcConsumptionTax();
		//代金移動（消費税）の作成
		PaymentEntry pe = PaymentEntry.create(tax, pt, taxPA);

		//消費税登録
		this.tax = pe.getPrice();
		//代金取引の登録
		this.payTransaction.add(pt);
		//代金移動の登録
		this.payEntry.add(pe);
	}

	/*
	 * 課税単位＝明細
	 */
	private void addPaymentByDETAIL() {
		//商流移動毎に代金取引,代金移動の作成
		for (CommercialEntry ce : ces) {
			this.addPaymentByDETAIL(ce);
		}
	}

	/*
	 * 課税単位＝明細時
	 * CE根拠となるPT,PE(商品代金),PE（消費税）の作成
	 */
	private void addPaymentByDETAIL(CommercialEntry ce) {
		//代金取引の作成
		PaymentTransaction pt = new PaymentTransaction(ct.getWhenCharged(), ce);
		//代金計算
		double price = this.calcPayment(ce);
		PaymentEntry pe1 = PaymentEntry.create(price, pt, goodsPA);

		//消費税計算
		Goods goods = ce.getAccount().getGoods();
		Item item = goods.getItem();
		ComputeTaxFacade taxCal = new ComputeTaxFacade(item, taxRoudingRule, price, dealDate, computeType);
		PaymentEntry pe2 = PaymentEntry.create(taxCal.calcConsumptionTax(), pt, taxPA);
		
		//商品毎の代金登録
		this.payments.put(goods, pe1.getPrice());
		//消費税登録
		this.tax += pe2.getPrice();
		//代金取引の登録
		this.payTransaction.add(pt);
		//代金移動の登録
		this.payEntry.add(pe1);
		this.payEntry.add(pe2);
	}

	/*
	 * 課税単位＝合計
	 */
	private double calTaxBySUM() {
		//合計代金の計算
		double sum = 0.0;
		for (CommercialEntry ce : ces) {
			sum += this.calcPayment(ce);
		}
		//消費税計算
		Item item = ct.getItem();
		ComputeTaxFacade taxCal = new ComputeTaxFacade(item, taxRoudingRule, sum, dealDate, computeType);
		return taxCal.calcConsumptionTax();
	}

	/*
	 * 課税単位＝明細
	 */
	private double calTaxByDETAIL() {
		double tax_detail = 0.0;
		for (CommercialEntry ce : ces) {
			Goods goods = ce.getAccount().getGoods();
			Item item = goods.getItem();
			//代金計算
			double price = this.calcPayment(ce);
			//消費税計算
			ComputeTaxFacade taxCal = new ComputeTaxFacade(item, taxRoudingRule, price, dealDate, computeType);
			double tmp = taxCal.calcConsumptionTax();
			tax_detail += tmp;
		}
		return tax_detail;
	}

	/*
	 * 永続化
	 * 永続化の順番が重要
	 * PEの前に、PT,PAの永続化が必要
	 */
	public void commit() {
		for(PaymentTransaction pt : payTransaction) {
			pt.save();
		}
		goodsPA.save();
		taxPA.save();
		for(PaymentEntry pe : payEntry) {
			pe.save();
		}
	}

	public double getPayment(Goods goods) {
		return payments.get(goods);
	}
	
	public double getTax() {
		return tax;
	}
	
	//代金計算
	private double calcPayment(CommercialEntry ce) {
		Goods goods = ce.getAccount().getGoods();
		BigDecimal unitPrice = new BigDecimal(goods.getUnitPrice().getPrice());
		//単位変換
		//TODO 次元の異なる単位の変換処理
		Unit u1 = goods.getUnitPrice().getUnit();
		Unit u2 = ce.getUnit();
		double source;
		if(u1 == u2 ) {
			source = ce.getAmount();
		}
		else {
			source = u1.convert(ce.getAmount(), u2);
		}
		BigDecimal amount = new BigDecimal(source);
		
		//代金計算（数量×商品単価）
		double price = amount.multiply(unitPrice).doubleValue();
		return this.payRoudingRule.calc(price);
	}
	
}
