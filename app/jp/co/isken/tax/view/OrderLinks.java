package jp.co.isken.tax.view;

import static views.ViewUtils.printDate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.co.isken.tax.domain.commerce.CommercialAccount;
import jp.co.isken.tax.domain.commerce.CommercialEntry;
import jp.co.isken.tax.domain.commerce.CommercialTransaction;
import jp.co.isken.tax.domain.contract.Contract;
import jp.co.isken.tax.domain.item.Goods;
import jp.co.isken.tax.domain.item.Item;
import jp.co.isken.tax.domain.item.Unit;
import jp.co.isken.tax.domain.party.Party;
import jp.co.isken.tax.domain.payment.PaymentAccount;
import jp.co.isken.tax.domain.payment.PaymentAccountType;
import jp.co.isken.tax.domain.payment.PaymentEntry;
import jp.co.isken.tax.domain.payment.PaymentTransaction;
import jp.co.isken.tax.rdb.RDBOperator;

public class OrderLinks {
	protected CommercialTransaction ct;

	protected Contract contract;
	protected Party contract_client;
	protected Party commercial_client;
	protected Party payment_client;

	protected List<CommercialEntry> ces = new ArrayList<CommercialEntry>();
	protected List<CommercialAccount> cas = new ArrayList<CommercialAccount>();
	protected List<Goods> goods = new ArrayList<Goods>();
	protected List<Item> items = new ArrayList<Item>();

	protected List<PaymentTransaction> pts = new ArrayList<PaymentTransaction>();
	protected List<PaymentEntry> pes = new ArrayList<PaymentEntry>();
	protected List<PaymentAccount> pas = new ArrayList<PaymentAccount>();
	protected List<PaymentAccountType> pat = new ArrayList<PaymentAccountType>();
	
	//導出値
	protected int price = 0;
	protected int tax = 0;
	//商流取引毎のレシート
	protected Map<String, Integer> receipt = new HashMap<String, Integer>();
	protected List<Receipt> receipt2 = new ArrayList<Receipt>();
	
	public OrderLinks(CommercialTransaction ct) {
		this.ct = ct;
		contract = ct.getContract();
		contract_client = contract.getFirstParty();
		this.setCommercialLinks();
		this.setPaymentTransaction();
		this.setPaymentEntry();
		this.setPaymentAccount();
		this.setReceipt();
	}

	public List<Receipt> getReceipt2() {
		return receipt2;
	}

	public Map<String, Integer> getReceipt() {
		return receipt;
	}
	
	public int getPrice() {
		return price;
	}

	public int getTax() {
		return tax;
	}
	
	private void setReceipt() {
		for(PaymentEntry pe : pes) {
			if(pe.getAccount().getAccountType() == PaymentAccountType.商品代金) {
				CommercialEntry bce = pe.getTransaction().getBasisEntry();
				Goods g = bce.getAccount().getGoods();
				//商品名の取得
				String gname = g.getName();
				//商品代金の取得
				int gprice = (int) pe.getPrice();
				receipt.put(gname, gprice);
				double gamount = bce.getAmount();
				String gunit = bce.getUnit().name();
				receipt2.add(new Receipt(bce.getId(), gname, gamount, gunit, gprice));
				//商品代金の合計
				price += gprice;
			}
		}
		for(PaymentTransaction pt : pts) {
			PaymentAccount pa = RDBOperator.$findPA(payment_client, PaymentAccountType.消費税);
			PaymentEntry pe = RDBOperator.$findPE(pt, pa);
			if(pe != null) {
				//消費税の取得
				tax += (int) pe.getPrice();
			}
		}
		receipt.put("合計額", price);
		receipt.put("消費税", tax);
	}

	public void printReceipt() {
		this.setReceipt();
		System.out.println("===========================================================");
		System.out.println("取引番号:"+ct.getId());
		System.out.println("-----------------------------------------------------------");
		for(String key : receipt.keySet()) {
			int data = receipt.get(key);
			System.out.println(key+":"+data+"円");
		}
		System.out.println("===========================================================");
	}

	public void printOrder() {
		System.out.println("===========================================================");
		System.out.println("取引番号:"+ct.getId());
		System.out.println("-----------------------------------------------------------");
		System.out.println("取引種別:"+ct.getDealType()
				+"   取引日:" +printDate(ct.getWhenCharged())
				+"   取引先:" +commercial_client.getName());
		System.out.println("-----------------------------------------------------------");
		System.out.println("取引内容:");
		System.out.println("-----------------------------------------------------------");
		for(CommercialEntry ce : ces) {
			String name = ce.getAccount().getGoods().getName();
			double amount = ce.getAmount();
			Unit unit = ce.getUnit();
			System.out.println(name+":"+amount+unit);
		}
		System.out.println("===========================================================");
	}

	private void setCommercialLinks() {
		ces = RDBOperator.$findCEs(ct);
		for (CommercialEntry p : ces) {
			CommercialAccount ca = p.getAccount();
			if(!(cas.contains(ca))) {
				cas.add(ca);
				this.setCommercialClient(ca.getClient());
				Goods g = ca.getGoods();
				if(!(goods.contains(g))) {
					goods.add(g);
					if(!(items.contains(g.getItem()))) {
						items.add(ca.getGoods().getItem());
					}
				}
			}
		}
	}
	
	//TODO 
	private void setCommercialClient(Party client) {
		if(contract_client.getId() != client.getId()) {
			System.out.println("オーダー不正：基本契約と異なる商流取引の契約先");
			System.out.println(contract_client.getName()+"/"+client.getName());
		}
	
		if(commercial_client == null) {
			commercial_client = client;
		} else {
			if(commercial_client.getId() != client.getId()) {
				System.out.println("オーダー不正：商流取引に複数の契約先が存在する。");
				System.out.println(commercial_client.getName()+"/"+client.getName());
			}
			commercial_client = client;
		}
	}

	private void setPaymentTransaction() {
		PaymentTransaction pt1 = RDBOperator.$findPT(ct);
		if(pt1 != null) {
			pts.add(pt1);
		}
		for (CommercialEntry p : ces) {
			PaymentTransaction pt2 = RDBOperator.$findPT(p);
			if(pt2 != null) {
				pts.add(pt2);
			}
		}
	}
	
	private void setPaymentEntry() {
		for (PaymentTransaction p : pts) {
			List<PaymentEntry> tmp = RDBOperator.$findPEs(p);
			for (PaymentEntry pe : tmp) {
				pes.add(pe);
			}
		}
	}

	private void setPaymentAccount() {
		for (PaymentEntry p : pes) {
			PaymentAccount pa = p.getAccount();
			if(!(pas.contains(pa))) {
				pas.add(pa);
				this.setPaymentClient(pa.getClient());
				if(!(pat.contains(pa.getAccountType()))) {
					pat.add(pa.getAccountType());
				}
			}
		}
	}

	//TODO 
	private void setPaymentClient(Party client) {
		if(contract_client.getId() != client.getId()) {
			System.out.println("オーダー不正：基本契約と異なる商流取引の契約先");
			System.out.println(contract_client.getName()+"/"+client.getName());
		}
		if(payment_client == null) {
			payment_client = client;
		} else {
			if(payment_client.getId() != client.getId()) {
				System.out.println("オーダー不正：金流取引に複数の契約先が存在する。");
				System.out.println(payment_client.getName()+"/"+client.getName());
			}
			payment_client = client;
		}
	}
	
	/*
	 * CTに関連するレコードの削除
	 * 参照しているものから削除していく
	 * PEの削除→PTの削除→CEの削除、CTの削除
	 * PA,CAは、他にも参照されるので削除しない
	 */
	public void delete() {
		System.out.println("========== TE DELETE ==========");
		System.out.println("CT:"+ct.getId());
		for(PaymentEntry pe : pes) {
			System.out.println("PE:"+pe.getId()+"-->PT:"+pe.getTransaction().getId());
			pe.delete();
		}
		for(PaymentTransaction pt : pts) {
			System.out.println("PT:"+pt.getId()+"-->CE:"+pt.getBasisEntry()+"-->CT:"+pt.getBasisTransaction());
			pt.delete();
		}
		for(CommercialEntry ce : ces) {
			System.out.println("CE:"+ce.getId()+"-->CT:"+ce.getTransaction().getId());
			ce.delete();
		}
		ct.delete();
		System.out.println("===============================");
	}

}
