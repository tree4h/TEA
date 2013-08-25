package jp.co.isken.tax.domain.payment;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.avaje.ebean.validation.NotNull;

import play.db.ebean.Model;

import jp.co.isken.tax.domain.party.Party;

@Entity
public class PaymentEntry extends Model {
	private static final long serialVersionUID = 1L;
	@NotNull
	private double price;
	@ManyToOne
	@JoinColumn(name = "transaction_id")
	private PaymentTransaction transaction;
	@ManyToOne
	@JoinColumn(name = "account_id")
	private PaymentAccount account;
	//永続化
	@Id
	private int id;

	private PaymentEntry(double price, PaymentTransaction transaction, PaymentAccount account) {
		this.price = price;
		this.transaction = transaction;
		this.account = account;
	}
	
	//TODO 課税単位別の振り分けはどのクラスの責務？
	public static PaymentEntry create(double price, PaymentTransaction transaction, PaymentAccount account) {
		if(!(isValidParty(transaction, account))) {
			System.out.println("PE.create():契約先不一致エラー");
			return null;
		}
		return new PaymentEntry(price, transaction, account);
	}
	
	//OCL1(契約先の一致)の実装
	private static boolean isValidParty(PaymentTransaction transaction, PaymentAccount account) {
		Party p1 = transaction.getClient();
		Party p2 = account.getClient();
		return (p1.getId() == p2.getId());
		//TODO 一致しなかった時のエラー処理
	}
	public double getPrice() {
		return price;
	}
	public PaymentTransaction getTransaction() {
		return transaction;
	}
	public PaymentAccount getAccount() {
		return account;
	}
	public int getId() {
		return id;
	}
}
