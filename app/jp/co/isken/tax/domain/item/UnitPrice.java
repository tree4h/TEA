package jp.co.isken.tax.domain.item;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.avaje.ebean.validation.NotNull;

import play.db.ebean.Model;

@Entity
public class UnitPrice extends Model {
	private static final long serialVersionUID = 1L;
	@Id
	private int id;
    @NotNull
	private Unit unit;
    @NotNull
	private double price;
	
	public UnitPrice(int price, Unit unit) {
		this.price = price;
		this.unit = unit;
	}
	
	public double getPrice() {
		return price;
	}

	public Unit getUnit() {
		return unit;
	}

	public int getId() {
		return id;
	}
	
}
