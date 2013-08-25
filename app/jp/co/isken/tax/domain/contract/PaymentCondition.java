package jp.co.isken.tax.domain.contract;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.avaje.ebean.validation.NotNull;

import play.db.ebean.Model;

@Entity
public class PaymentCondition extends Model {
	private static final long serialVersionUID = 1L;
	@Id
	private int id;
	@NotNull
	private RoundingRule roundingRule;
	
	public PaymentCondition(RoundingRule roundingRule) {
		this.roundingRule = roundingRule;
	}
	
	public RoundingRule getRoundingRule() {
		return roundingRule;
	}

}
