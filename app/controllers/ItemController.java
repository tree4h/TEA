package controllers;

import static controllers.ControllerUtils.makeListEnumValues;

import java.util.List;

import javax.persistence.PersistenceException;

import jp.co.isken.tax.domain.item.Item;
import jp.co.isken.tax.domain.item.Goods;
import jp.co.isken.tax.domain.item.Unit;
import jp.co.isken.tax.domain.item.UnitPrice;
import jp.co.isken.tax.rdb.RDBOperator;

import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.item;
import views.html.itementry;
import views.html.itemlist;

public class ItemController extends Controller {
	//画面表示用　処理結果メッセージ
	private static ViewMessage message = ViewMessage.NO_MESSAGE;
	/*
     * 商品一覧画面表示
     */
    public static Result showItemList() {
		List<Goods> goods = RDBOperator.$findGoods();
		return ok(itemlist.render(goods));
    }
    /*
     * 商品登録画面表示
     */
    public static Result initItem() {
    	List<String> types = makeListEnumValues(Item.class);
    	List<String> units = makeListEnumValues(Unit.class);
		return ok(itementry.render(types, units));
	}
    /*
     * 商品画面表示
     */
    public static Result showItem(long id) {
		Goods goods = RDBOperator.$findGoods(id);
		//画面表示用メッセージの設定
		String msg = message.getMessage();
		//メッセージの初期化
		message = ViewMessage.NO_MESSAGE;
		return ok(item.render(msg, goods));
    }
    /*
     * 商品登録
     */
    public static Result entryItem() {
		DynamicForm input = Form.form().bindFromRequest();
		String name = input.data().get("item_name");
		String type = input.data().get("item");
		String price = input.data().get("price");
		String unit = input.data().get("unit");
		
		UnitPrice unitPrice = new UnitPrice(Integer.parseInt(price), Unit.valueOf(unit));
		Goods goods = new Goods(name, unitPrice, Item.valueOf(type));
		goods.save();
		
		return redirect(controllers.routes.ItemController.showItemList());
	}
    /*
     * 商品削除
     */
    public static Result deleteItem() {
		DynamicForm input = Form.form().bindFromRequest();
		long id = Long.parseLong(input.data().get("goods_id"));
		Goods goods = RDBOperator.$findGoods(id);
		try {
			goods.delete();
		} catch (PersistenceException e) {
			message = ViewMessage.ERROR_ITEM_DELETE;
			return redirect(controllers.routes.ItemController.showItem(id));
		}
		return redirect(controllers.routes.ItemController.showItemList());
	}
}
