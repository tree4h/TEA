package jp.co.isken.tax.domain.contract;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.db.ebean.Model;

import com.avaje.ebean.validation.NotNull;

@Entity
public class TaxCondition extends Model {
	private static final long serialVersionUID = 1L;
	@Id
	private int id;
	@NotNull
	private RoundingRule roundingRule;
	@NotNull
	private TaxUnitRule taxUnitRule;

	public TaxCondition(RoundingRule roundingRule, TaxUnitRule taxUnitRule) {
		this.roundingRule = roundingRule;
		this.taxUnitRule = taxUnitRule;
	}
	
	public RoundingRule getRoundingRule() {
		return roundingRule;
	}
	
	public TaxUnitRule getTaxUnitRule() {
		return taxUnitRule;
	}

}
