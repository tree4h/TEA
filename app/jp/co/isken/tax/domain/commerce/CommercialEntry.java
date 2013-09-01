package jp.co.isken.tax.domain.commerce;

import java.math.BigDecimal;

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
	 * 商流移動修正処理
	 * 赤CEを作成(自身は削除しない)
	 * TODO 赤、黒の関連の必要性
	 */
	public CommercialEntry createRedCE() {
		CommercialEntry redCE = CommercialEntry.create(-amount, unit, transaction, account);
		return redCE;
	}
	/*
	 * 商流移動修正処理
	 * 新CEを作成
	 */
	public CommercialEntry createNewCE(double newAmount) {
		CommercialEntry newCE = CommercialEntry.create(newAmount, unit, transaction, account);
		return newCE;
	}
	/*
	 * 商流移動修正処理
	 * 関連PT、PEの修正（削除）
	 */
	public void revicePayment() {
		PaymentTransaction pt = RDBOperator.$findPT(this);
		if(pt != null) {
			pt.revice();
		}
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

	public Item getItem() {
		return account.getGoods().getItem();
	}
}
