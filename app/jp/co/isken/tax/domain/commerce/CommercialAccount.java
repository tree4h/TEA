package jp.co.isken.tax.domain.commerce;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import play.db.ebean.Model;

import jp.co.isken.tax.domain.item.Goods;
import jp.co.isken.tax.domain.party.Party;

@Entity
public class CommercialAccount extends Model {
	private static final long serialVersionUID = 1L;
	@ManyToOne
	@JoinColumn(name = "client_id")
	private Party client;
	@ManyToOne
	@JoinColumn(name = "goods_id")
	private Goods goods;
	//永続化
	@Id
	private int id = 0;
	
	public CommercialAccount(Party client, Goods goods) {
		this.client = client;
		this.goods = goods;
	}
	
	public Party getClient() {
		return client;
	}
	
	public Goods getGoods() {
		return goods;
	}

	public int getId() {
		return id;
	}

}
