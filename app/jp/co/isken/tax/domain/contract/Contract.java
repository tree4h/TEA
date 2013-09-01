package jp.co.isken.tax.domain.contract;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import play.db.ebean.Model;

import com.avaje.ebean.validation.NotNull;

import jp.co.isken.tax.domain.party.Party;

@Entity
public class Contract extends Model {
	private static final long serialVersionUID = 1L;
	@NotNull
	private ContractType type;				//契約種別
	@NotNull
	private Date whenAgreement;				//締結日

	@ManyToOne
	@JoinColumn(name = "firstParty_id")
	private Party firstParty;				//契約先
	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name="range_id")
	private DateRange range;				//契約期間（発効日-失効日）
	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name="taxCondition_id")
	private TaxCondition taxCondition;		//消費税条項
	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name="payCondtion_id")
	private PaymentCondition payCondition;	//代金税条項
	//永続化
	@Id
	private int id;

	public Contract(ContractType type, Party party, DateRange range, TaxCondition taxCondition, PaymentCondition payCondition, Date whenAgreement) {
		this.type = type;
		this.firstParty = party;
		this.range = range;
		this.taxCondition = taxCondition;
		this.payCondition = payCondition;
		this.whenAgreement = whenAgreement;
	}
	
	//TODO 永続化層処理
	public void commit() {
	}
	
	public int getId() {
		return id;
	}
	
	public ContractType getType() {
		return type;
	}
	
	public Party getFirstParty() {
		return firstParty;
	}
	
	public Date getWhenAgreement() {
		return whenAgreement;
	}
	
	public TaxCondition getTaxCondition() {
		return taxCondition;
	}

	public PaymentCondition getPayCondition() {
		return payCondition;
	}
	
	public RoundingRule getRoundingRule() {
		return taxCondition.getRoundingRule();
	}
	public TaxUnitRule getTaxUnitRule() {
		return taxCondition.getTaxUnitRule();
	}

	public DateRange getRange() {
		return this.range;
	}

	public Date getEffectiveDate() {
		return this.range.getBegin();
	}

	public Date getInEffectiveDate() {
		return this.range.getEnd();
	}
	/*
	 * 取引日において、有効な契約であるかどうかを返す
	 */
	public boolean isEffectiveContract(Date whenCharged) {
		return range.isEffective(whenCharged);
	}
}
