package jp.co.isken.tax.domain.commerce;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.avaje.ebean.validation.NotNull;

import play.db.ebean.Model;

import jp.co.isken.tax.domain.contract.Contract;
import jp.co.isken.tax.domain.contract.RoundingRule;
import jp.co.isken.tax.domain.item.Item;
import jp.co.isken.tax.domain.payment.PaymentEntry;
import jp.co.isken.tax.domain.payment.PaymentTransaction;
import jp.co.isken.tax.domain.commerce.ComputeType;
import jp.co.isken.tax.rdb.RDBOperator;

@Entity
public class CommercialTransaction extends Model {
	private static final long serialVersionUID = 1L;
	@Id
	private int id;
	@NotNull
	private Date whenCharged;		//取引日
	@NotNull
	private DealType dealType;		//取引種別(仕入/販売)
	@NotNull
	private TaxedDealType taxType;	//取引対象区分（社外取引/社内取引/国外取引）
	@NotNull
	private boolean isTaxedDeal;	//取引課税区分(課税/非課税)
	@NotNull
	private ComputeType computeType;//消費税計算(外税/内税/非課税)
	private Item item;				//品目（課税単位が合計の時に保持）
	@NotNull
	private Date whenBooked;	//登録日(sysdate)
	@ManyToOne
	@JoinColumn(name = "contract_id")
	private Contract contract;	//Partyから取引日に有効な契約を導出するクラスが必要

	private CommercialTransaction(DealType dealType, boolean isTaxedDeal, TaxedDealType taxType, 
			Date whenCharged, Contract contract, ComputeType computeType) {
		this.dealType = dealType;
		this.isTaxedDeal = isTaxedDeal;
		this.taxType = taxType;
		this.whenCharged = whenCharged;
		this.contract = contract;
		this.whenBooked = new Date();
		this.computeType = computeType;
	}
	
	public static CommercialTransaction create(DealType dealType, boolean isTaxedDeal, TaxedDealType taxType, 
			Date whenCharged, Contract contract, ComputeType computeType) {
		//取引種別と契約種別が同じとなっているかの確認
		if(dealType.getContractType() != contract.getType()) {
			System.out.println("オーダー不正：契約種別と取引種別が異なる");
			return null;
		}
		//取引日で有効な契約となっているかの確認
		if(!(contract.isEffectiveContract(whenCharged))) {
			System.out.println("オーダー不正：有効な契約が存在しない");
			return null;
		}
		return new CommercialTransaction(dealType, isTaxedDeal, taxType, whenCharged, contract, computeType);
	}
	public void setItem(Item item) {
		if(this.item == null) {
			this.item = item;
		}
		else {
			if(this.item != item) {
				//TODO throwException(不正なオーダー：消費税率が異なる商品を合計指定されたオーダーとなる)
				System.out.println("CT.setItem():品目数が複数エラー");
			}
		}
	}

	public ComputeType getComputeType() {
		return computeType;
	}

	public Item getItem() {
		return item;
	}

	public boolean getIsTaxedDeal() {
		return isTaxedDeal;
	}
	
	public TaxedDealType getTaxType() {
		return taxType;
	}

	public Contract getContract() {
		return contract;
	}
	
	public Date getWhenBooked() {
		return whenBooked;
	}
	
	public Date getWhenCharged() {
		return whenCharged;
	}
	
	public RoundingRule getRoundingRule() {
		return contract.getRoundingRule();
	}

	public DealType getDealType() {
		return dealType;
	}

	public int getId() {
		return id;
	}
	/*
	 * 商流移動キャンセル処理[課税単位＝合計]
	 * 関連PE,PTの削除を行う
	 */
	public void revicePT() {
		//関連PT,PEの削除
		PaymentTransaction pt = RDBOperator.$findPT(this);
		if(pt != null) {
			pt.revice();
		}
	}
	/*
	 * CTを根拠とするPT,PE（消費税）の作成
	 */
	public PaymentTransaction createPT() {
		return new PaymentTransaction(whenCharged, this);
	}

	/*
	 * 自身の取引の合計代金を算出して返す
	 */
	public double sumUpPrice() {
		List<PaymentEntry> pes = RDBOperator.$findPEs(this);
		double sum = 0.0;
		for(PaymentEntry pe : pes) {
			sum = sum + pe.getPrice();
		}
		return sum;
	}
}
