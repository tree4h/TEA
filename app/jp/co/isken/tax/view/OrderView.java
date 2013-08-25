package jp.co.isken.tax.view;

import static views.ViewUtils.printDate;

import jp.co.isken.tax.domain.commerce.CommercialTransaction;
import jp.co.isken.tax.view.OrderLinks;

public class OrderView {
	//注文一覧
	public int id;	//注文番号
	public String dealType;	//取引種別
	public String whenCharged;	//取引日
	public String client;	//取引先
	public int price;	//消費税額含む合計
	public int tax;
	//注文確認
	public String dealTaxType;	//取引対象区分
	public boolean taxType;		//取引課税区分
	public String computeType;	//消費税計算

	public OrderLinks link;
	
	public OrderView(CommercialTransaction ct) {
		this.id = ct.getId();
		this.dealType = ct.getDealType().name();
		this.whenCharged = printDate(ct.getWhenCharged());
		this.client = ct.getContract().getFirstParty().getName();
		this.link = new OrderLinks(ct);
		this.tax = link.getTax();
		this.price = link.getPrice()+this.tax;
		
		this.dealTaxType = ct.getTaxType().name();
		this.taxType = ct.getIsTaxedDeal();
		this.computeType = ct.getComputeType().name();
	}
	
}
