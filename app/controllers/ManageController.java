package controllers;

import java.util.List;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.TxRunnable;

import jp.co.isken.tax.domain.commerce.CommercialAccount;
import jp.co.isken.tax.domain.commerce.CommercialEntry;
import jp.co.isken.tax.domain.commerce.CommercialTransaction;
import jp.co.isken.tax.domain.contract.Contract;
import jp.co.isken.tax.domain.item.Goods;
import jp.co.isken.tax.domain.party.Party;
import jp.co.isken.tax.domain.payment.PaymentAccount;
import jp.co.isken.tax.domain.payment.PaymentEntry;
import jp.co.isken.tax.domain.payment.PaymentTransaction;
import jp.co.isken.taxlib.domain.TaxRate;
import play.db.ebean.Model.Finder;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.manage;

public class ManageController extends Controller {
	/*
     * 管理メニュー画面表示
     */
    public static Result showManageMenu() {
		return ok(manage.render());
    }
	/*
     * 全データ削除
     */
    public static Result deleteData() {
    	//トランザクションの実行
    	Ebean.execute(new TxRunnable() {
        	@Override
        	public void run() {
        		deleteTax();
            	deletePE();
            	deletePA();
            	deletePT();
            	deleteCE();
            	deleteCA();
            	deleteCT();
            	deleteGoods();
            	deleteContract();
            	deleteParty();
        	}
    	});
		return ok(manage.render());
    }
    
    private static void deleteTax() {
    	Finder<Long, TaxRate> finder = new Finder<Long, TaxRate>(Long.class, TaxRate.class);
		List<TaxRate> targets = finder.all();
		for(TaxRate target : targets) {
			target.delete();
		}
    }
    private static void deletePE() {
    	Finder<Long, PaymentEntry> finder = new Finder<Long, PaymentEntry>(Long.class, PaymentEntry.class);
		List<PaymentEntry> targets = finder.all();
		for(PaymentEntry target : targets) {
			target.delete();
		}
    }
    private static void deletePA() {
    	Finder<Long, PaymentAccount> finder = new Finder<Long, PaymentAccount>(Long.class, PaymentAccount.class);
		List<PaymentAccount> targets = finder.all();
		for(PaymentAccount target : targets) {
			target.delete();
		}
    }
    private static void deletePT() {
    	Finder<Long, PaymentTransaction> finder = new Finder<Long, PaymentTransaction>(Long.class, PaymentTransaction.class);
		List<PaymentTransaction> targets = finder.all();
		for(PaymentTransaction target : targets) {
			target.delete();
		}
    }
    private static void deleteCE() {
    	Finder<Long, CommercialEntry> finder = new Finder<Long, CommercialEntry>(Long.class, CommercialEntry.class);
		List<CommercialEntry> targets = finder.all();
		for(CommercialEntry target : targets) {
			target.delete();
		}
    }
    private static void deleteCA() {
    	Finder<Long, CommercialAccount> finder = new Finder<Long, CommercialAccount>(Long.class, CommercialAccount.class);
		List<CommercialAccount> targets = finder.all();
		for(CommercialAccount target : targets) {
			target.delete();
		}
    }
    private static void deleteCT() {
    	Finder<Long, CommercialTransaction> finder = new Finder<Long, CommercialTransaction>(Long.class, CommercialTransaction.class);
		List<CommercialTransaction> targets = finder.all();
		for(CommercialTransaction target : targets) {
			target.delete();
		}
    }
    private static void deleteGoods() {
    	Finder<Long, Goods> finder = new Finder<Long, Goods>(Long.class, Goods.class);
		List<Goods> targets = finder.all();
		for(Goods target : targets) {
			target.delete();
		}
    }
    private static void deleteContract() {
    	Finder<Long, Contract> finder = new Finder<Long, Contract>(Long.class, Contract.class);
		List<Contract> targets = finder.all();
		for(Contract target : targets) {
			System.out.println("deleteContract="+target.getId());
			target.delete();
		}
    }
    private static void deleteParty() {
    	Finder<Long, Party> finder = new Finder<Long, Party>(Long.class, Party.class);
		List<Party> targets = finder.all();
		for(Party target : targets) {
			target.delete();
		}
    }

}
