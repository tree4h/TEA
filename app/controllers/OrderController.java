package controllers;

import static controllers.ControllerUtils.date;
import static controllers.ControllerUtils.makeListEnumValues;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jp.co.isken.tax.domain.commerce.CommercialTransaction;
import jp.co.isken.tax.domain.commerce.DealType;
import jp.co.isken.tax.domain.commerce.TaxedDealType;
import jp.co.isken.tax.domain.item.Goods;
import jp.co.isken.tax.domain.item.Unit;
import jp.co.isken.tax.domain.party.Party;
import jp.co.isken.tax.domain.commerce.ComputeType;
import jp.co.isken.tax.facade.Order;
import play.cache.Cache;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.orderentry;
import views.html.orderlist;
import views.html.orderconfirm;
import views.html.order;

import jp.co.isken.tax.rdb.RDBOperator;
import jp.co.isken.tax.view.OrderLinks;
import jp.co.isken.tax.view.OrderView;
import jp.co.isken.tax.view.PlantUML;
import jp.co.isken.tax.view.Receipt;

public class OrderController extends Controller {
	//画面表示用　処理結果メッセージ
	private static ViewMessage message = ViewMessage.NO_MESSAGE;

	/*
     * [注文]リンク
     * →[注文一覧]画面生成
     * →[注文一覧]
     */
    public static Result showOrderList() {
		//商流取引の取得
    	List<CommercialTransaction> cts = RDBOperator.$findCTs();
		//商流取引Viewへの変換
		List<OrderView> orders = new ArrayList<OrderView>();
		for(CommercialTransaction ct : cts) {
			orders.add(new OrderView(ct));
		}
		return ok(orderlist.render(orders));
    }
    /*
     * [注文一覧]+
     * →注文登録画面生成
     * →[注文登録]
     */
    public static Result initOrder() {
    	List<Party> parties = RDBOperator.$findParty();
		List<Goods> goods = RDBOperator.$findGoods();
		List<String> types = makeListEnumValues(DealType.class);
    	List<String> taxtypes = makeListEnumValues(TaxedDealType.class);
    	List<String> units = makeListEnumValues(Unit.class);
    	//画面表示用メッセージの設定
		String msg = message.getMessage();
		//メッセージの初期化
		message = ViewMessage.NO_MESSAGE;
    	return ok(orderentry.render(msg, parties, goods, types, taxtypes, units));
	}
    /*
     * [注文一覧]注文番号
     * →注文修正画面生成
     * →[注文修正]
     */
    public static Result showOrder(long id) {
 	   CommercialTransaction ct = RDBOperator.$findCT(id);
 	   OrderView ov = new OrderView(ct);
 	   List<Receipt> receipt = ov.link.getReceipt2();
 	   
 	   //画面表示用メッセージの設定
 	   String msg = message.getMessage();
 	   //メッセージの初期化
 	   message = ViewMessage.NO_MESSAGE;
 	   return ok(order.render(msg, ov, receipt));
    }
    /*
     * [注文確認]OK
     * →注文確定（永続化）処理
     * →[注文一覧]
     */
   public static Result saveOrder() {
		DynamicForm input = Form.form().bindFromRequest();
		//キャッシュIDの取得
		String cache_id = input.data().get("cache_id");
		Order order = (Order)Cache.get(cache_id);
		//キャンセル対象CEの金流取引（PT）、移動（PE）の削除
		order.deletePayment();
		//永続化
		order.commit();
		//キャッシュの削除
		Cache.remove(cache_id);
		return redirect(controllers.routes.OrderController.showOrderList());
	}
   /*
     * [注文修正]TEA
     * →注文TEA画像作成処理
     * →[注文TEA]
    */
   public static Result showOrderTEA(long id) {
	   CommercialTransaction ct = RDBOperator.$findCT(id);
	   //注文の全関連の取得
	   OrderLinks ol = new OrderLinks(ct);
	   PlantUML puml = new PlantUML(ol);
	   puml.printPlantUML();
	   return redirect(routes.Assets.at("plantuml/"+puml.png_name));
   }
   /*
     * [注文修正]削除
     * →注文削除処理
     * →[注文一覧]
    */
   public static Result deleteOrder(long id) {
	   CommercialTransaction ct = RDBOperator.$findCT(id);
	   //注文の全関連の取得
	   OrderLinks ol = new OrderLinks(ct);
	   //注文の関連インスタンスを含め削除
	   ol.delete();
	   return redirect(controllers.routes.OrderController.showOrderList());
   }
    /*
     * [注文登録]Entry
     * →新規注文生成、注文確認画面生成
     * →[注文確認]
     */
   public static Result entryOrder() {
	   	try {
	   		DynamicForm input = Form.form().bindFromRequest();
		   	//商流inputの取得
		   	String party_id = input.data().get("party_id");
			String charged = input.data().get("charged");
			String deal_type = input.data().get("deal_type");
			String deal_taxtype = input.data().get("deal_taxtype");
			String tax_type = input.data().get("tax_type");
			String compute_type = input.data().get("compute_type");
			//商流取引input作成
			boolean taxType = true;
			if(tax_type == "0") { 
				taxType = false;
			}
			DealType dealType = DealType.valueOf(deal_type);
			TaxedDealType taxedDealType = TaxedDealType.valueOf(deal_taxtype);
			ComputeType computeType = ComputeType.valueOf(compute_type);
			
			//商流ファサードの作成,商流取引（CT）の作成
			Order order = new Order(Long.parseLong(party_id), dealType, taxType, taxedDealType,
					date(charged), computeType);
			//商流移動（CE）の作成
			createCEs(input.data(), order);
			//金流inputの取得
			//金流取引（PT）、移動（PE）の作成
			order.createPayment();
			
			String cache_id = Double.toString(Math.random()*1000);
			Cache.set(cache_id, order, 300);
			OrderView ov = new OrderView(order);
			List<Receipt> receipt = order.getReceipt();
			return ok(orderconfirm.render(cache_id, ov, receipt));
	   	} catch (java.lang.NullPointerException e) {
			message = ViewMessage.NULLP_ERROR_ALL;
			return redirect(controllers.routes.OrderController.initOrder());
	   		
	   	} catch (IllegalArgumentException e) {
			message = ViewMessage.ERROR_NO_ORDER;
			return redirect(controllers.routes.OrderController.initOrder());
	   		
	   	} catch (Exception e) {
			message = ViewMessage.ANY_ERROR_ALL;
			return redirect(controllers.routes.OrderController.initOrder());
		}
	}
   /*
    * [注文確認]修正
    * →修正注文生成、注文確認画面生成
    * →[注文確認]
    */
   public static Result reviseOrder(long id) {
	   try {
		   DynamicForm input = Form.form().bindFromRequest();
		   //商流ファサードの作成,商流取引（CT）の作成
		   Order order = new Order(id);
		   //商流移動（CE）の作成
		   reviceCEs(input.data(), order);
		   //金流取引（PT）、移動（PE）の作成
		   order.createPayment();

			String cache_id = Double.toString(Math.random()*1000);
			Cache.set(cache_id, order, 300);
			OrderView ov = new OrderView(order);
			List<Receipt> receipt = order.getReceipt();
			return ok(orderconfirm.render(cache_id, ov, receipt));
	  	} catch (java.lang.NullPointerException e) {
			message = ViewMessage.NULLP_ERROR_ALL;
			return redirect(controllers.routes.OrderController.showOrder(id));
	  		
	  	} catch (IllegalArgumentException e) {
			message = ViewMessage.ERROR_NO_ORDER;
			return redirect(controllers.routes.OrderController.showOrder(id));
	  		
	  	} catch (Exception e) {
			message = ViewMessage.ANY_ERROR_ALL;
			return redirect(controllers.routes.OrderController.showOrder(id));
		}
   }
   
   /*
    * [新規注文]
    * 商流移動（CE）の作成
    */
   private static void createCEs(Map<String, String> input, Order order) {
	   	boolean order_flag = false;
		String G_KEY = "goods_g";
		String A_KEY = "goods_a";
		String U_KEY = "goods_u";
		for (String key : input.keySet()) {
		   if(key.startsWith(G_KEY)) {
			   //商品番号の取得
			   String id = key.replace(G_KEY, "");
			   //注文量の取得
			   double amount = Double.parseDouble(input.get(A_KEY+id));
			   if(!(amount == 0.0)) {
				   //注文の記録
				   order_flag = true;
				   //注文単位の取得
				   Unit unit = Unit.valueOf(input.get(U_KEY+id));
				   //注文の作成
				   order.createCE(Long.parseLong(id), amount, unit);
			   }
		   }
	   }
	   if(!(order_flag)) {
		   throw new IllegalArgumentException();
	   }
   }
   
   /*
    * [修正注文]
    * 商流移動（CE）の作成
    */
   private static void reviceCEs(Map<String, String> input, Order order) {
	   boolean order_flag = false;
	   String F_KEY = "cancel_f";	//キャンセルフラグ
	   String A_KEY = "cancel_a";	//修正値
	   for (String key : input.keySet()) {
		   if(key.startsWith(F_KEY)) {
			   //注文の記録
			   order_flag = true;
			   //checkboxは、checkedになっているものだけ送信される
			   //商流移動番号の取得
				String id = key.replace(F_KEY, "");
				//注文量の取得
				double newAmount = Double.parseDouble(input.get(A_KEY+id));
				order.reviceCE(Long.parseLong(id), newAmount);
		   }
	   }
	   if(!(order_flag)) {
		   throw new IllegalArgumentException();
	   }
   }

}
