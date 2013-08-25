package jp.co.isken.tax.domain.item;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import com.avaje.ebean.validation.NotNull;

import play.db.ebean.Model;

@Entity
public class Goods extends Model {
	private static final long serialVersionUID = 1L;
    @NotNull
	private String name;
	@OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="unitPrice_id")
    private UnitPrice unitPrice;
    @NotNull
	private Item item;
	//永続化
	@Id
	private int id;

	public Goods(String name, UnitPrice unitPrice, Item item) {
		this.name = name;
		this.unitPrice = unitPrice;
		this.item = item;
	}

	public String getName() {
		return name;
	}

	public Item getItem() {
		return item;
	}
	
	public UnitPrice getUnitPrice() {
		return unitPrice;
	}

	public int getId() {
		return id;
	}
}
