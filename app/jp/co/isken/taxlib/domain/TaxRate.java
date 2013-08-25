package jp.co.isken.taxlib.domain;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import com.avaje.ebean.validation.NotNull;
import play.db.ebean.Model;

@Entity
public class TaxRate extends Model {
	private static final long serialVersionUID = 1L;
	@NotNull
	private double rate;		//数値
	@SuppressWarnings("unused")
	private Unit unit;			//単位
	@OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="range_id")
	private DateRange range;	//税の有効期間
	private TaxItem item;		//税品目
	@Id
	private int id;

	public TaxRate(double rate, Unit unit, DateRange range) {
		this.rate = rate;
		this.unit = unit;
		this.range = range;
	}

	public TaxRate(double rate, Unit unit, DateRange range, TaxItem item) {
		this.rate = rate;
		this.unit = unit;
		this.range = range;
		this.item = item;
	}
	
	public boolean isEffective(Date target) {
		return range.isEffective(target);
	}
	public int getId() {
		return id;
	}
	public TaxItem getTaxItem() {
		return item;
	}
	public double getRate() {
		return rate;
	}
	public Date getEffectiveDate() {
		return range.getBegin();
	}
	public Date getInEffectiveDate() {
		return range.getEnd();
	}
}
