package jp.co.isken.tax.facade;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jp.co.isken.tax.domain.commerce.CommercialAccount;
import jp.co.isken.tax.domain.commerce.CommercialEntry;
import jp.co.isken.tax.domain.commerce.CommercialTransaction;
import jp.co.isken.tax.domain.commerce.ComputeType;
import jp.co.isken.tax.domain.commerce.DealType;
import jp.co.isken.tax.domain.commerce.TaxedDealType;
import jp.co.isken.tax.domain.contract.Contract;
import jp.co.isken.tax.domain.contract.RoundingRule;
import jp.co.isken.tax.domain.contract.TaxUnitRule;
import jp.co.isken.tax.domain.item.Goods;
import jp.co.isken.tax.domain.item.Item;
import jp.co.isken.tax.domain.item.Unit;
import jp.co.isken.tax.domain.party.Party;
import jp.co.isken.tax.domain.payment.PaymentAccount;
import jp.co.isken.tax.domain.payment.PaymentAccountType;
import jp.co.isken.tax.domain.payment.PaymentEntry;
import jp.co.isken.tax.domain.payment.PaymentTransaction;
import jp.co.isken.tax.rdb.RDBOperator;
import jp.co.isken.tax.view.Receipt;

public class Order {
	//取引先
	private Party client;
	//基本契約
	private Contract contract;
	//計算属性-商流取引の取引日
	private Date dealDate;
	//計算属性-消費税丸め計算ルール
	private RoundingRule taxRoudingRule;
	//計算属性-課税単位
	private TaxUnitRule taxUnitRule;
	//計算属性-消費税計算方法
	private ComputeType computeType;
	
	//商流取引
	private CommercialTransaction ct;
	//商流勘定(ここで作成することもあるからいる)
	private List<CommercialAccount> cas = new ArrayList<CommercialAccount>();
	//新規作成対象CE
	private List<CommercialEntry> newCEs = new ArrayList<CommercialEntry>();
	//修正対象CE
	private List<CommercialEntry> oldCEs = new ArrayList<CommercialEntry>();
	//修正CE（赤）
	private List<CommercialEntry> redCEs = new ArrayList<CommercialEntry>();
	//商流移動（レシート用）
	private List<CommercialEntry> ces = new ArrayList<CommercialEntry>();
	
	//金流取引
	private List<PaymentTransaction> pts = new ArrayList<PaymentTransaction>();
	//金流勘定（商品代金）(ここで作成することもあるからいる)
	private PaymentAccount goodsPA;
	//金流勘定（消費税）(ここで作成することもあるからいる)
	private PaymentAccount taxPA;
	//金流移動
	private List<PaymentEntry> pes = new ArrayList<PaymentEntry>();
	
	//レシート
	private List<Receipt> receipt = new ArrayList<Receipt>();
	//消費税
	private double tax = 0.0;
	//商品代金合計
	private double sumPrice = 0.0;
	/*
	 * 新規オーダー
	 */
	public Order(long party_id, DealType dealType, boolean taxedDeal, TaxedDealType taxedDealType, 
			Date dealDate, ComputeType computeType) {
		Party client = RDBOperator.$findParty(party_id);
		this.client = client;
		this.dealDate = dealDate;
		this.computeType = computeType;
		this.contract = RDBOperator.$findContract(client, dealType.getContractType(), dealDate);
		this.taxRoudingRule = contract.getRoundingRule();
		this.taxUnitRule = contract.getTaxUnitRule();
		//商流取引の作成
		this.ct = CommercialTransaction.create(dealType, taxedDeal, taxedDealType, 
				dealDate, contract, computeType);
	}
	/*
	 * 修正オーダー
	 */
	public Order(long ct_id) {
		//商流取引の取得
		this.ct = RDBOperator.$findCT(ct_id);
		this.dealDate = ct.getWhenCharged();
		this.computeType = ct.getComputeType();
		this.contract = ct.getContract();
		this.client = contract.getFirstParty();
		this.taxRoudingRule = contract.getRoundingRule();
		this.taxUnitRule = contract.getTaxUnitRule();
		ces = RDBOperator.$findCEs(ct);
	}
	
	/*
	 * 新規オーダー
	 */
	public void createCE(long goods_id, double amount, Unit unit) {
		Goods goods = RDBOperator.$findGoods(goods_id);
		//課税単位が合計の際のCTへの品目設定
		//課税単位=安い方についても、取引単位に品目が一意となる必要がある
		if(taxUnitRule == TaxUnitRule.SUM || taxUnitRule == TaxUnitRule.CHEAPER) {
			ct.setItem(goods.getItem());
		}
		//商流勘定の検索
		CommercialAccount ca = RDBOperator.$findCA(client, goods);
		if(!(cas.contains(ca))) {
			cas.add(ca);
		}
		//商流移動の作成
		CommercialEntry ce = CommercialEntry.create(amount, unit, ct, ca);
		newCEs.add(ce);
		//商流移動(レシート用)の作成
		ces.add(ce);
	}
	/*
	 * 修正オーダー
	 */
	public void reviceCE(long ce_id, double newAmount) {
		CommercialEntry oldCE = RDBOperator.$findCE(ce_id);
		oldCEs.add(oldCE);
		CommercialEntry redCE = oldCE.createRedCE();
		CommercialEntry newCE = oldCE.createNewCE(newAmount);
		redCEs.add(redCE);
		newCEs.add(newCE);
		//商流移動(レシート用)の作成
		ces.remove(oldCE);
		ces.add(newCE);
	}
	
	/*
	 * 金流取引（PT）、移動（PE）の作成
	 * 課税単位による振り分けを行う
	 */
	public void createPayment() {
		//金流勘定の検索
		goodsPA = RDBOperator.$findPA(client, PaymentAccountType.商品代金);
		taxPA = RDBOperator.$findPA(client, PaymentAccountType.消費税);
		
		if(taxUnitRule == TaxUnitRule.DETAIL) {
			//消費税の計算
			tax = this.calTaxByDETAIL();
			this.createPaymentByDETAIL();
		}
		else if(taxUnitRule == TaxUnitRule.SUM) {
			//消費税の計算
			tax = this.calTaxBySUM();
			this.createPaymentBySUM();
		}
		else {
			double tax_sum = this.calTaxBySUM();
			double tax_detail = this.calTaxByDETAIL();
			if(tax_sum < tax_detail) {
				this.taxUnitRule = TaxUnitRule.SUM;
				tax = tax_sum;
				this.createPaymentBySUM();
			}
			else {
				this.taxUnitRule = TaxUnitRule.DETAIL;
				tax = tax_detail;
				this.createPaymentByDETAIL();
			}
		}
		//レシートの作成
		this.setReceipt();
	}
	/*
	 * 修正対象外のCEをレシート登録する
	 */
	private void setReceipt() {
		for(CommercialEntry ce : ces) {
			//レシートの作成
			PaymentEntry pe1 = RDBOperator.$findPE(ce, goodsPA);
			if(pe1 != null) {
				Goods g = ce.getAccount().getGoods();
				receipt.add(new Receipt(ce.getId(), g.getId(), g.getName(), ce.getAmount(), 
						ce.getUnit().name(), pe1.getPrice()));
			}
		}
	}
	/*
	 * 修正オーダー
	 * 金流取引（PT）、移動（PE）の削除
	 * 課税単位による振り分けを行う
	 * オーダーの課税単位は、明細、合計に事前に決定されている
	 */
	public void deletePayment() {
		if(taxUnitRule == TaxUnitRule.DETAIL) {
			this.deletePaymentByDETAIL();
		}
		else if(taxUnitRule == TaxUnitRule.SUM) {
			this.deletePaymentBySUM();
		}
		else {
			System.out.println("deletePayment:異常発生");
		}
	}
	/*
	 * [課税単位＝明細]
	 * 金流取引（PT）、移動（PE）の作成
	 */
	private void createPaymentByDETAIL() {
		for(CommercialEntry ce : newCEs) {
			PaymentTransaction pt = ce.createPT();
			PaymentEntry pe1 = ce.createGoodsPE(pt);
			PaymentEntry pe2 = ce.createTaxPE(pt);
			pts.add(pt);
			pes.add(pe1);
			pes.add(pe2);
			//レシート作成
			Goods g = ce.getAccount().getGoods();
			receipt.add(new Receipt(ce.getId(), g.getId(), g.getName(), ce.getAmount(), 
					ce.getUnit().name(), pe1.getPrice()));
		}
	}
	/*
	 * [課税単位＝合計]
	 * 金流取引（PT）、移動（PE）の作成
	 */
	private void createPaymentBySUM() {
		for(CommercialEntry ce : newCEs) {
			PaymentTransaction pt = ce.createPT();
			PaymentEntry pe1 = ce.createGoodsPE(pt);
			pts.add(pt);
			pes.add(pe1);
			//レシート作成
			Goods g = ce.getAccount().getGoods();
			receipt.add(new Receipt(ce.getId(), g.getId(), g.getName(), ce.getAmount(), 
					ce.getUnit().name(), pe1.getPrice()));
		}
		//消費税移動の作成
		PaymentTransaction pt = ct.createPT();
		PaymentEntry pe = PaymentEntry.create(tax, pt, taxPA);
		pts.add(pt);
		pes.add(pe);
	}
	/*
	 * [課税単位＝明細]
	 * 金流取引（PT）、移動（PE）の修正
	 */
	private void deletePaymentByDETAIL() {
		//CE関連の削除
		for(CommercialEntry ce : oldCEs) {
			ce.revicePayment();
		}
	}
	/*
	 * [課税単位＝合計]
	 * 金流取引（PT）、移動（PE）の修正
	 */
	private void deletePaymentBySUM() {
		//CE関連の削除
		for(CommercialEntry ce : oldCEs) {
			ce.revicePayment();
		}
		//CT関連の削除
		ct.revicePT();
	}
	/*
	 * [課税単位＝合計]
	 * 消費税の計算
	 */
	private double calTaxBySUM() {
		//合計代金の計算
		sumPrice = 0.0;
		for (CommercialEntry ce : ces) {
			sumPrice += ce.calcPayment();
		}
		//消費税計算
		Item item = ct.getItem();
		ComputeTaxFacade taxCal = new ComputeTaxFacade(item, taxRoudingRule, sumPrice, dealDate, computeType);
		return taxCal.calcConsumptionTax();
	}
	/*
	 * [課税単位＝明細]
	 * 消費税の計算
	 */
	private double calTaxByDETAIL() {
		//合計代金の計算
		sumPrice = 0.0;
		double tax_detail = 0.0;
		for (CommercialEntry ce : ces) {
			Goods goods = ce.getAccount().getGoods();
			Item item = goods.getItem();
			//代金計算
			double price = ce.calcPayment();
			//消費税計算
			ComputeTaxFacade taxCal = new ComputeTaxFacade(item, taxRoudingRule, price, dealDate, computeType);
			tax_detail += taxCal.calcConsumptionTax();
			//商品合計代金の計算
			sumPrice += price;
		}
		return tax_detail;
	}
	/*
	 * オーダー番号（商流取引番号）を返す
	 */
	public int getId() {
		return ct.getId();
	}
	/*
	 * 商流取引を返す
	 */
	public CommercialTransaction getCT() {
		return ct;
	}

	/*
	 * 永続化
	 * 永続化の順番（参照されているものからSAVE）
	 */
	public void commit() {
		ct.save();
		for(CommercialAccount ca : cas) {
			ca.save();
		}
		for(CommercialEntry ce : ces) {
			ce.save();
		}
		for(CommercialEntry ce : redCEs) {
			ce.save();
		}
		//oldCEsはUPDATEしないためsaveなし
		for(PaymentTransaction pt : pts) {
			pt.save();
		}
		goodsPA.save();
		taxPA.save();
		for(PaymentEntry pe : pes) {
			pe.save();
		}
	}
	/*
	 * 商品毎の代金を返す
	 */
	public double getPayment(Goods goods) {
		int id = goods.getId();
		for(Receipt r : receipt) {
			if(id == r.gid) {
				return r.price;
			}
		}
		//TODO 例外処理
		return (Double) null;
	}
	/*
	 * レシートを返す
	 */
	public List<Receipt> getReceipt() {
		return receipt;
	}
	/*
	 * 消費税を返す
	 */
	public double getTax() {
		return tax;
	}
	/*
	 * 商品代金合計を返す
	 */
	public double getSumPrice() {
		if(computeType.name() == ComputeType.内税.name()) {
			return sumPrice-tax;
		}
		return sumPrice;
	}
}
