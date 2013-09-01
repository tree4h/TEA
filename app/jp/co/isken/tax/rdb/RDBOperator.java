package jp.co.isken.tax.rdb;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jp.co.isken.tax.domain.commerce.CommercialAccount;
import jp.co.isken.tax.domain.commerce.CommercialEntry;
import jp.co.isken.tax.domain.commerce.CommercialTransaction;
import jp.co.isken.tax.domain.contract.Contract;
import jp.co.isken.tax.domain.contract.ContractType;
import jp.co.isken.tax.domain.item.Goods;
import jp.co.isken.tax.domain.party.Party;
import jp.co.isken.tax.domain.payment.PaymentAccount;
import jp.co.isken.tax.domain.payment.PaymentAccountType;
import jp.co.isken.tax.domain.payment.PaymentEntry;
import jp.co.isken.tax.domain.payment.PaymentTransaction;
import jp.co.isken.taxlib.domain.DateRange;
import jp.co.isken.taxlib.domain.TaxItem;
import jp.co.isken.taxlib.domain.TaxRate;
import play.db.ebean.Model.Finder;

public class RDBOperator {
	//商流
	private static Finder<Long, CommercialTransaction> $ct = new Finder<Long, CommercialTransaction>(Long.class, CommercialTransaction.class);
	private static Finder<Long, CommercialAccount> $ca = new Finder<Long, CommercialAccount>(Long.class, CommercialAccount.class);
	private static Finder<Long, CommercialEntry> $ce = new Finder<Long, CommercialEntry>(Long.class, CommercialEntry.class);
	//金流
	private static Finder<Long, PaymentTransaction> $pt = new Finder<Long, PaymentTransaction>(Long.class, PaymentTransaction.class);
	private static Finder<Long, PaymentAccount> $pa = new Finder<Long, PaymentAccount>(Long.class, PaymentAccount.class);
	private static Finder<Long, PaymentEntry> $pe = new Finder<Long, PaymentEntry>(Long.class, PaymentEntry.class);
	//契約
	private static Finder<Long, Contract> $contract = new Finder<Long, Contract>(Long.class, Contract.class);
	//商品
	private static Finder<Long, Goods> $goods = new Finder<Long, Goods>(Long.class, Goods.class);
	//Party
	private static Finder<Long, Party> $party = new Finder<Long, Party>(Long.class, Party.class);
	//消費税
	private static Finder<Long, TaxRate> $tax = new Finder<Long, TaxRate>(Long.class, TaxRate.class);

	/*
	 * 指定されたidのCEを返す
	 */
	public static CommercialEntry $findCE(long id) {
		return $ce.byId(id);
	}
	/*
	 * 指定されたCEを根拠とするPTを返す
	 */
	public static PaymentTransaction $findPT(CommercialEntry ce) {
		int id = ce.getId();
		List<PaymentTransaction> pts = $pt.all();
		for(PaymentTransaction pt : pts) {
			CommercialEntry target = pt.getBasisEntry();
			if(target != null && id == target.getId()) {
				return pt;
			}
		}
		return null;
	}
	/*
	 * 指定されたCTを根拠とするPTを返す
	 */
	public static PaymentTransaction $findPT(CommercialTransaction ct) {
		int id = ct.getId();
		List<PaymentTransaction> pts = $pt.all();
		for(PaymentTransaction pt : pts) {
			CommercialTransaction target = pt.getBasisTransaction();
			if(target != null && id == target.getId()) {
				return pt;
			}
		}
		return null;
	}
	/*
	 * 指定されたPTを参照するPEのリストを返す
	 */
	public static List<PaymentEntry> $findPEs(PaymentTransaction pt) {
		List<PaymentEntry> result = new ArrayList<PaymentEntry>();
		int id = pt.getId();
		List<PaymentEntry> pes = $pe.all();
		for(PaymentEntry pe : pes) {
			if(id == pe.getTransaction().getId()) {
				result.add(pe);
			}
		}
		return result;
	}
	/*
	 * 指定されたCTに関連するPEのリストを返す
	 */
	public static List<PaymentEntry> $findPEs(CommercialTransaction ct) {
		//CTを参照するCEを検索
		List<CommercialEntry> ces = $findCEs(ct);
		//CEを根拠とするPTを検索
		List<PaymentTransaction> pts = new ArrayList<PaymentTransaction>();
		for(CommercialEntry ce : ces) {
			PaymentTransaction pt = $findPT(ce);
			if(pt != null) {
				pts.add(pt);
			}
		}
		//PTを参照するPEを検索
		List<PaymentEntry> result = new ArrayList<PaymentEntry>();
		for(PaymentTransaction pt : pts) {
			List<PaymentEntry> pes = $findPEs(pt);
			for(PaymentEntry pe : pes) {
				result.add(pe);
			}
		}
		return result;
	}
	/*
	 * 指定されたCTを参照するCEのリストを返す
	 */
	public static List<CommercialEntry> $findCEs(CommercialTransaction ct) {
		int id = ct.getId();
		List<CommercialEntry> result = new ArrayList<CommercialEntry>();
		List<CommercialEntry> ces = $ce.all();
		for(CommercialEntry ce : ces) {
			if(id == ce.getTransaction().getId()) {
				result.add(ce);
			}
		}
		return result;
	}
	/*
	 * 指定された（取引先、商品）のCAを返す
	 * なければ作成して返す
	 */
	public static CommercialAccount $findCA(Party client, Goods goods) {
		int client_id = client.getId();
		int goods_id = goods.getId();
		List<CommercialAccount> cas = $ca.all();
		for(CommercialAccount ca : cas) {
			if(client_id == ca.getClient().getId() && goods_id == ca.getGoods().getId()) {
				return ca;
			}
		}
		CommercialAccount ca = new CommercialAccount(client, goods);
		ca.save();
		return ca;
	}
	/*
	 * 指定された（取引先、代金勘定のアカウントType）のPAを返す
	 * なければ作成して返す
	 */
	public static PaymentAccount $findPA(Party client, PaymentAccountType pat) {
		int id = client.getId();
		String name = pat.name();
		List<PaymentAccount> pas = $pa.all();
		for(PaymentAccount pa : pas) {
			if(id == pa.getClient().getId() && name == pa.getAccountType().name()) {
				return pa;
			}
		}
		PaymentAccount pa = new PaymentAccount(client, pat);
		pa.save();
		return pa;
	}
	
	public static CommercialTransaction $findCT(long id) {
		return $ct.byId(id);
	}
	public static Goods $findGoods(long id) {
		return $goods.byId(id);
	}
	public static Party $findParty(long id) {
		return $party.byId(id);
	}
	public static Contract $findContract(long id) {
		return $contract.byId(id);
	}

	public static List<TaxRate> $findTaxRate() {
		return $tax.all();
	}
	public static List<Goods> $findGoods() {
		return $goods.all();
	}
	public static List<CommercialTransaction> $findCTs() {
		return $ct.all();
	}
	public static List<Contract> $findContract() {
		return $contract.all();
	}
	public static List<Party> $findParty() {
		return $party.all();
	}
	/*
	 * 取引日において、有効な契約を返す
	 */
	public static Contract $findContract(Party client, ContractType type, Date dealDate) {
		List<Contract> contracts = $contract.all();
		for (Contract c : contracts) {
			if (c.getFirstParty().getId() == client.getId() && c.getType() == type && c.getRange().isEffective(dealDate)) {
				return c;
			}
		}
		//TODO 例外処理
		System.out.println("The Contract does not exist!!");
		return null;
	}
	/*
	 * 指定のPT,PAを参照するPEを返す
	 */
	public static PaymentEntry $findPE(PaymentTransaction pt, PaymentAccount pa) {
		int pt_id = pt.getId();
		int pa_id = pa.getId();
		List<PaymentEntry> pes = $pe.all();
		for (PaymentEntry pe : pes) {
			if (pt_id == pe.getTransaction().getId() && pa_id == pe.getAccount().getId()) {
				return pe;
			}
		}
		return null;
	}
	/*
	 * 指定のCEを参照するPEを返す
	 */
	public static PaymentEntry $findPE(CommercialEntry ce, PaymentAccount pa) {
		PaymentTransaction pt = $findPT(ce);
		if(pt == null) {
			return null;
		}
		return $findPE(pt, pa);
	}
	
	/*
	 * 取引日において有効な品目別税率を返す
	 */
	public static double $findTaxRate(Date target, TaxItem item) {
		List<TaxRate> trs = $tax.all();
		for (TaxRate tr : trs) {
			if (tr.getTaxItem().name() == item.name() && tr.isEffective(target)) {
				return tr.getRate();
			}
		}
		//TODO 例外処理
		System.out.println("The Tax-Rate does not exist!!");
		return 0.0;
	}
	/*
	 * 取引日において有効な税率を返す
	 */
	public static double $findTaxRate(Date target) {
		List<TaxRate> trs = $tax.all();
		for (TaxRate tr : trs) {
			if (tr.isEffective(target)) {
				return tr.getRate();
			}
		}
		//TODO 例外処理
		System.out.println("The Tax-Rate does not exist!!");
		return 0.0;
	}
	/*
	 * 指定日において有効な税率が登録されているかを返す
	 * 登録されている：true
	 * 登録されていない：false
	 * アプリケーションによる一意性の担保（PKとは別に担保）
	 */
	public static boolean $isTaxRate(DateRange target) {
		List<TaxRate> trs = $tax.all();
		for (TaxRate tr : trs) {
			if (tr.isEffective(target.getBegin())) {
				return true;
			}
			if (tr.isEffective(target.getEnd())) {
				return true;
			}
		}
		return false;
	}

}
