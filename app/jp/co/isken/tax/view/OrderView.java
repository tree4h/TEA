package jp.co.isken.tax.view;

import static views.ViewUtils.printDate;

import jp.co.isken.tax.domain.commerce.CommercialTransaction;
import jp.co.isken.tax.facade.Order;
import jp.co.isken.tax.view.OrderLinks;

public class OrderView {
	//注文一覧
	public int id;	//注文番号
	public String dealType;	//取引種別
	public String whenCharged;	//取引日
	public String client;	//取引先
	public double price;	//消費税額含む合計
	public double tax;
	//注文確認
	public String dealTaxType;	//取引対象区分
	public boolean taxType;		//取引課税区分
	public String computeType;	//消費税計算
	//商流取引全関連
	public OrderLinks link;
	
	/*
	 * 新規登録確認画面用
	 */
	public OrderView(Order order) {
		CommercialTransaction ct = order.getCT();
		this.id = ct.getId();
		this.dealType = ct.getDealType().name();
		this.whenCharged = printDate(ct.getWhenCharged());
		this.client = ct.getContract().getFirstParty().getName();
		this.dealTaxType = ct.getTaxType().name();
		this.taxType = ct.getIsTaxedDeal();
		this.computeType = ct.getComputeType().name();
		
		this.tax = order.getTax();
		this.price = tax + order.getSumPrice();
	}
	
	/*
	 * 修正画面用
	 */
	public OrderView(CommercialTransaction ct) {
		this.id = ct.getId();
		this.dealType = ct.getDealType().name();
		this.whenCharged = printDate(ct.getWhenCharged());
		this.client = ct.getContract().getFirstParty().getName();
		this.link = new OrderLinks(ct);
		this.tax = link.getTax();
		//TODO bag 課税単位合計時の代金がおかしい
		this.price = link.getPrice()+this.tax;
		
		this.dealTaxType = ct.getTaxType().name();
		this.taxType = ct.getIsTaxedDeal();
		this.computeType = ct.getComputeType().name();
	}

}
