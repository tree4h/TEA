package jp.co.isken.taxlib.domain;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.db.ebean.Model;

import com.avaje.ebean.validation.NotNull;

@Entity
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

	public Date getBegin() {
		return begin;
	}
	
	public Date getEnd() {
		return end;
	}

}
