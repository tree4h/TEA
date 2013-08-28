package jp.co.isken.tax.view;

import jp.co.isken.tax.domain.item.Unit;

/*
 * 商品注文について、指定された商品の以下のデータを管理する
 * 商品id
 * 注文量
 * 注文単位
 */
public class GoodsOrder {
	
	//TODO private?情報連携のハコでしかない
	public long id;
	public double amount;
	public Unit unit;
	
	public GoodsOrder(long id, double amount, Unit unit) {
		this.id = id;
		this.amount = amount;
		this.unit = unit;
	}
	
}
