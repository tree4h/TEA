package controllers;

import static controllers.ControllerUtils.date;

import java.util.List;

import jp.co.isken.tax.rdb.RDBOperator;
import jp.co.isken.taxlib.domain.DateRange;
import jp.co.isken.taxlib.domain.TaxRate;
import jp.co.isken.taxlib.domain.Unit;

import play.data.DynamicForm;
import play.data.Form;
import play.db.ebean.Model.Finder;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.taxlist;
import views.html.taxentry;

public class TaxController extends Controller {
	//画面表示用　処理結果メッセージ
	private static ViewMessage message = ViewMessage.NO_MESSAGE;
    /*
     * 消費税一覧画面表示
     */
    public static Result showTaxList() {
		List<TaxRate> trs = RDBOperator.$findTaxRate();
		return ok(taxlist.render(trs));
    }
    /*
     * 消費税登録画面表示
     */
    public static Result initTax() {
		//画面表示用メッセージの設定
		String m = message.getMessage();
		//メッセージの初期化
		message = ViewMessage.NO_MESSAGE;
		return ok(taxentry.render(m));
	}
    /*
     * 消費税登録
     */
    public static Result entryTax() {
		DynamicForm input = Form.form().bindFromRequest();
		String rate = input.data().get("tax_rate");
		String start = input.data().get("start");
		String end = input.data().get("end");
		DateRange range = new DateRange(date(start), date(end));
		
		if(RDBOperator.$isTaxRate(range)) {
			message = ViewMessage.ERROR_TAX_INSERT;
			return redirect(controllers.routes.TaxController.initTax());
		}
		
		TaxRate tr = new TaxRate(Double.parseDouble(rate), Unit.PERCENT, range);
		tr.save();
		
		return redirect(controllers.routes.TaxController.showTaxList());
	}
    /*
     * 消費税登録(TEST用)
     */
    public static Result allentryTax() {
    	String d1 = "1989-04-01";
    	String d2 = "1997-04-01";
    	String d3 = "2014-04-01";
    	String d4 = "2015-10-01";
    	String d5 = "2020-04-01";

    	DateRange range1 = new DateRange(date(d1), date(d2));
    	DateRange range2 = new DateRange(date(d2), date(d3));
    	DateRange range3 = new DateRange(date(d3), date(d4));
    	DateRange range4 = new DateRange(date(d4), date(d5));
		TaxRate r1 = new TaxRate(0.03, jp.co.isken.taxlib.domain.Unit.PERCENT, range1);
		TaxRate r2 = new TaxRate(0.05, jp.co.isken.taxlib.domain.Unit.PERCENT, range2);
		TaxRate r3 = new TaxRate(0.08, jp.co.isken.taxlib.domain.Unit.PERCENT, range3);
		TaxRate r4 = new TaxRate(0.08, jp.co.isken.taxlib.domain.Unit.PERCENT, range4);
		r1.save();
		r2.save();
		r3.save();
		r4.save();
		
		return redirect(controllers.routes.TaxController.showTaxList());
	}
    /*
     * 消費税削除(TEST用)
     */
    public static Result alldeleteTax() {
    	Finder<Long, TaxRate> finder = new Finder<Long, TaxRate>(Long.class, TaxRate.class);
		List<TaxRate> trs = finder.all();
		for(TaxRate tr : trs) {
			tr.delete();
		}
		return redirect(controllers.routes.TaxController.showTaxList());
	}
}
