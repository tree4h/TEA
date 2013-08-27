package jp.co.isken.tax.domain.contract;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.avaje.ebean.validation.NotNull;

import play.db.ebean.Model;

@Entity
@Table(name = "TaxDateRange")
public class DateRange extends Model {
	private static final long serialVersionUID = 1L;
	@Id
	private int id;
	@NotNull
	private Date beginDate;
	@NotNull
	private Date endDate;

	public DateRange(Date start, Date end) {
		this.beginDate = start;
		this.endDate = end;
	}
	
	public boolean isEffective(Date d) {
		Long target = d.getTime();
		if(target < beginDate.getTime()) {
			return false;
		}
		if(target >= endDate.getTime()) {
			return false;
		}
		return true;
	}

	public int getId() {
		return id;
	}

	public Date getBegin() {
		return beginDate;
	}
	
	public Date getEnd() {
		return endDate;
	}

}
