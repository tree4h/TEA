package jp.co.isken.tax.domain.commerce;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.avaje.ebean.validation.NotNull;

import play.db.ebean.Model;

import jp.co.isken.tax.domain.contract.RoundingRule;
import jp.co.isken.tax.domain.contract.TaxUnitRule;
import jp.co.isken.tax.domain.item.Goods;
import jp.co.isken.tax.domain.item.Item;
import jp.co.isken.tax.domain.item.Unit;
import jp.co.isken.tax.domain.item.UnitPrice;
import jp.co.isken.tax.domain.party.Party;
import jp.co.isken.tax.domain.payment.PaymentTransaction;
import jp.co.isken.tax.domain.commerce.ComputeType;
import jp.co.isken.tax.rdb.RDBOperator;

@Entity
public class CommercialEntry extends Model {
	private static final long serialVersionUID = 1L;
	@NotNull
	private double amount;
	@NotNull
	private Unit unit;
	@ManyToOne
	@JoinColumn(name = "transaction_id")
	private CommercialTransaction transaction;
	@ManyToOne
	@JoinColumn(name = "account_id")
	private CommercialAccount account;
	//永続化
	@Id
	private int id;
	
	private CommercialEntry(double amount, Unit unit, CommercialTransaction transaction, CommercialAccount account) {
		this.amount = amount;
		this.unit = unit;
		this.transaction = transaction;
		this.account = account;
	}
	
	public static CommercialEntry create(double amount, Unit unit, CommercialTransaction transaction, CommercialAccount account) {
		if(!(isValidParty(transaction, account))) {
			System.out.println("CE.create():契約先不一致エラー");
			return null;
		}
		//TODO 課税単位が合計または安い方
		TaxUnitRule rule = transaction.getContract().getTaxCondition().getTaxUnitRule();
		if(rule.equals(TaxUnitRule.SUM) || rule.equals(TaxUnitRule.CHEAPER)) {
			if(!(isValidItem(transaction, account))) {
				System.out.println("CE.create():品目数が複数エラー");
				return null;
			}
		}
		return new CommercialEntry(amount, unit, transaction, account);
	}

	//OCL1(契約先の一致)の実装
	private static boolean isValidParty(CommercialTransaction transaction, CommercialAccount account) {
		Party p1 = transaction.getContract().getFirstParty();
		Party p2 = account.getClient();
		return (p1.getId() == p2.getId());
		//return p1.equals(p2);
		//TODO 一致しなかった時のエラー処理
	}
	//OCL2(課税単位が合計のときに品目が１つに特定できること)の実装
	private static boolean isValidItem(CommercialTransaction transaction, CommercialAccount account) {
		Item current = transaction.getItem();
		Item target = account.getGoods().getItem();
		if(current == null) {
			transaction.setItem(target);
			return true;
		}
		else {
			if(target.equals(current)) {
				return true;
			}
			return false;
			//TODO 一致しなかった時のエラー処理
		}
	}
	
	public CommercialTransaction getTransaction() {
		return transaction;
	}

	public CommercialAccount getAccount() {
		return account;
	}
	
	public double getAmount() {
		return amount;
	}
	
	public Unit getUnit() {
		return unit;
	}

	public UnitPrice getUnitPrice() {
		return account.getGoods().getUnitPrice();
	}

	public int getId() {
		return id;
	}
	/*
	 * 商流移動キャンセル処理
	 * 1.赤CEを作成(自身は削除しない)
	 * 2.関連PE,PTの削除を行う
	 * 3.新CEを作成
	 * TODO 赤、黒の関連の必要性
	 */
	public CommercialEntry cancel(double newAmount) {
		//1.赤CEの作成
		CommercialEntry redCE = CommercialEntry.create(-amount, unit, transaction, account);
		redCE.save();
		//2.関連PE,PTの削除
		PaymentTransaction pt = RDBOperator.$findPT(this);
		pt.deleteByCancel();
		//3.新CEを作成
		CommercialEntry newCE = CommercialEntry.create(newAmount, unit, transaction, account);
		newCE.save();
		return newCE;
	}
	/*
	 * 商流移動キャンセル処理
	 * 4.新CEを根拠とするPTの作成
	 * TODO 赤、黒の関連の必要性
	 */
	public PaymentTransaction createPT() {
		//4.新CEを根拠とするPTの作成
		PaymentTransaction newPT = new PaymentTransaction(transaction.getWhenCharged(), this);
		newPT.save();
		return newPT;
	}
	public void createPEPrice(PaymentTransaction pt) {
		Party client = transaction.getContract().getFirstParty();
		double price = this.calcPayment();
		pt.createPEPrice(client, price);
	}
	public void createPETax(PaymentTransaction pt) {
		ComputeType computeType = transaction.getComputeType();
		Party client = transaction.getContract().getFirstParty();
		Item item = account.getGoods().getItem();
		RoundingRule roundingRule = transaction.getContract().getRoundingRule();
		Date dealDate = transaction.getWhenCharged();
		double price = this.calcPayment();
		pt.createPETax(client, item, roundingRule, price, dealDate, computeType);
	}
	/*
	 * 自身の代金を計算する
	 */
	public double calcPayment() {
		Goods goods = this.account.getGoods();
		BigDecimal unitPrice = new BigDecimal(goods.getUnitPrice().getPrice());
		//単位変換
		//TODO 次元の異なる単位の変換処理
		Unit u1 = goods.getUnitPrice().getUnit();
		double source;
		if(u1 == unit ) {
			source = amount;
		}
		else {
			source = u1.convert(amount, unit);
		}
		BigDecimal amount = new BigDecimal(source);
		//代金計算（数量×商品単価）
		double price = amount.multiply(unitPrice).doubleValue();
		RoundingRule rule = this.transaction.getContract().getPayCondition().getRoundingRule();
		return rule.calc(price);
	}
}
