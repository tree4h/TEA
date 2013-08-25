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
	private Date begin;
	@NotNull
	private Date end;

	public DateRange(Date start, Date end) {
		this.begin = start;
		this.end = end;
	}
	
	public boolean isEffective(Date d) {
		Long target = d.getTime();
		if(target < begin.getTime()) {
			return false;
		}
		if(target >= end.getTime()) {
			return false;
		}
		return true;
	}

	public int getId() {
		return id;
	}

	public Date getBegin() {
		return begin;
	}
	
	public Date getEnd() {
		return end;
	}

}
