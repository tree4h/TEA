package jp.co.isken.tax.domain.payment;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import com.avaje.ebean.validation.NotNull;

import play.db.ebean.Model;

import jp.co.isken.tax.domain.commerce.CommercialEntry;
import jp.co.isken.tax.domain.commerce.CommercialTransaction;
import jp.co.isken.tax.domain.contract.RoundingRule;
import jp.co.isken.tax.domain.item.Item;
import jp.co.isken.tax.domain.party.Party;
import jp.co.isken.tax.facade.ComputeTaxFacade;
import jp.co.isken.tax.domain.commerce.ComputeType;
import jp.co.isken.tax.rdb.RDBOperator;

@Entity
public class PaymentTransaction extends Model {
	private static final long serialVersionUID = 1L;
	@NotNull
	private Date whenCharged;	//取引日
	@NotNull
	private Date whenBooked;	//登録日(sysdate)
	@OneToOne
	@JoinColumn(name="basisTransaction_id")
	private CommercialTransaction basisTransaction;	//根拠（課税単位=合計、代金勘定=消費税のとき）
	@OneToOne
	@JoinColumn(name="basisEntry_id")
	private CommercialEntry basisEntry;				//根拠（課税単位=明細、代金勘定=消費税のとき）
	//TODO 概念モデルにない関連
	@ManyToOne
	@JoinColumn(name = "client_id")
	private Party client;							//根拠の契約先を保持、根拠により設定の仕方が異なる
	//永続化
	@Id
	private int id;

	public PaymentTransaction(Date whenCharged, CommercialTransaction comTransaction) {
		this.whenCharged = whenCharged;
		this.whenBooked = new Date();
		this.basisTransaction = comTransaction;
		this.client = comTransaction.getContract().getFirstParty();
	}

	public PaymentTransaction(Date whenCharged, CommercialEntry comEntry) {
		this.whenCharged = whenCharged;
		this.whenBooked = new Date();
		this.basisEntry = comEntry;
		this.client = comEntry.getTransaction().getContract().getFirstParty();
	}

	public Party getClient() {
		return client;
	}

	public CommercialTransaction getBasisTransaction() {
		return basisTransaction;
	}

	public CommercialEntry getBasisEntry() {
		return basisEntry;
	}
	
	public Date getWhenBooked() {
		return whenBooked;
	}
	
	public Date getWhenCharged() {
		return whenCharged;
	}
	public int getId() {
		return id;
	}
	/*
	 * 根拠となるCE/CTがキャンセルされた時の処理
	 * 自身を参照しているPEを削除後、自身を削除する
	 */
	public void revice() {
		List<PaymentEntry> pes = RDBOperator.$findPEs(this);
		for(PaymentEntry pe : pes) {
			pe.delete();
		}
		this.delete();
	}

	/*
	 * PE（商品代金）の作成
	 */
	public PaymentEntry createPE(Party client, double price) {
		//PA(商品代金)の取得
		PaymentAccount pa = RDBOperator.$findPA(client, PaymentAccountType.商品代金);
		//新しいPEの作成
		return PaymentEntry.create(price, this, pa);
	}
	/*
	 * PE（消費税）の作成
	 */
	public PaymentEntry createPE(Party client, Item item, RoundingRule roundingRule, double price, Date dealDate, ComputeType computeType) {
		//消費税の計算
		ComputeTaxFacade taxCal = new ComputeTaxFacade(item, roundingRule, price, dealDate, computeType);
		double tax = taxCal.calcConsumptionTax();
		//PA(消費税)の取得
		PaymentAccount pa = RDBOperator.$findPA(client, PaymentAccountType.消費税);
		//新しいPEの作成
		return PaymentEntry.create(tax, this, pa);
	}
}
