package controllers;

import static controllers.ControllerUtils.date;
import static controllers.ControllerUtils.makeListEnumValues;
import static controllers.ControllerUtils.printInput;

import java.util.ArrayList;
import java.util.HashMap;
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
import jp.co.isken.tax.facade.Payment;
import jp.co.isken.tax.facade.ReviseOrder;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.orderentry;
import views.html.orderlist;
import views.html.orderconfirm;
import views.html.order;

import jp.co.isken.tax.rdb.RDBOperator;
import jp.co.isken.tax.view.GoodsOrder;
import jp.co.isken.tax.view.OrderLinks;
import jp.co.isken.tax.view.OrderView;
import jp.co.isken.tax.view.PlantUML;
import jp.co.isken.tax.view.Receipt;

public class OrderController extends Controller {
	//画面表示用　処理結果メッセージ
	private static ViewMessage message = ViewMessage.NO_MESSAGE;
	//Formデータinputのキー
	private static final String G_KEY = "goods_g";
	private static final String A_KEY = "goods_a";
	private static final String U_KEY = "goods_u";

	/*
     * 注文一覧画面表示
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
     * 注文登録画面表示
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
     * 注文確認画面表示
     */
   public static Result confirmOrder(long id) {
	   CommercialTransaction ct = RDBOperator.$findCT(id);
	   OrderView ov = new OrderView(ct);
	   List<Receipt> receipt = ov.link.getReceipt2();
		return ok(orderconfirm.render(ov, receipt));
	}
   /*
    * 注文画面表示
    * 注文確認画面と同じ処理（VIEWが異なるだけ）
    */
   public static Result showOrder(long id) {
	   CommercialTransaction ct = RDBOperator.$findCT(id);
	   OrderView ov = new OrderView(ct);
	   List<Receipt> receipt = ov.link.getReceipt2();
	   return ok(order.render(ov, receipt));
   }
   /*
    * 注文TEA画像表示
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
    * 注文修正処理
    */
   public static Result reviseOrder() {
	   DynamicForm input = Form.form().bindFromRequest();
	   String order_id = input.data().get("order_id");
	   long reviseCT = Long.parseLong(order_id);
	   //修正対象商品情報の取得
	   Map<Long, Double> reviseCE = reviceGoodsOrder(input.data());
	   //TODO debug
	   printInput(input.data());

	   ReviseOrder reviseOrder = new ReviseOrder(reviseCT, reviseCE);
	   reviseOrder.addPayment();
	   
	   return redirect(controllers.routes.OrderController.confirmOrder(Integer.parseInt(order_id)));
   }
   /*
    * 注文削除処理
    */
   public static Result deleteOrder() {
	   DynamicForm input = Form.form().bindFromRequest();
	   String order_id = input.data().get("order_id");
	   CommercialTransaction ct = RDBOperator.$findCT(Long.parseLong(order_id));
	   //注文の全関連の取得
	   OrderLinks ol = new OrderLinks(ct);
	   //注文の関連インスタンスを含め削除
	   ol.delete();
	   return redirect(controllers.routes.OrderController.showOrderList());
   }
    /*
     * 注文登録
     */
   public static Result entryOrder() {
	   	try {
	   		DynamicForm input = Form.form().bindFromRequest();
		   	//printInput(input.data());
		   	//商流inputの取得
		   	String party_id = input.data().get("party_id");
			String charged = input.data().get("charged");
			String deal_type = input.data().get("deal_type");
			String deal_taxtype = input.data().get("deal_taxtype");
			String tax_type = input.data().get("tax_type");
			String compute_type = input.data().get("compute_type");
			//商流取引input作成
			boolean taxType = true;
			if(tax_type == "0") { taxType = false; }
			DealType dealType = DealType.valueOf(deal_type);
			TaxedDealType taxedDealType = TaxedDealType.valueOf(deal_taxtype);
			ComputeType computeType = ComputeType.valueOf(compute_type);
			//商流ファサードの作成,商流取引（CT）の作成
			Order order = new Order(Long.parseLong(party_id), dealType, taxType, taxedDealType,
					date(charged), computeType);
			//商品注文の取得
			List<GoodsOrder> gos = makeGoodsOrder(input.data());
			//商流勘定（CE）の作成
			order.createCE(gos);
			
			//金流inputの取得
			//金流ファサード作成
			Payment payment = new Payment(order);
			//金流勘定作成
			payment.addPayment();
			//TODO 確認の前に永続化している
			order.commit();
			payment.commit();
			return redirect(controllers.routes.OrderController.confirmOrder(order.getCommercialTransaction().getId()));
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
   
   //TODO 商品input取得　この処理は無理やり！！JSON?が利用できる？
   private static List<GoodsOrder> makeGoodsOrder(Map<String, String> input) {
	   System.out.println("makeGoodsOrder");
	   List<GoodsOrder> result = new ArrayList<GoodsOrder>();
	   boolean order_flag = false;
	   for (String key : input.keySet()) {
		   System.out.println("key=" + key + ", value=" + input.get(key));
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
				   GoodsOrder go = new GoodsOrder(Long.parseLong(id), amount, unit);
				   result.add(go);
			   }
		   }
	   }
	   if(!(order_flag)) {
		   throw new IllegalArgumentException();
	   }
	   return result;
   }
   
   /*
    * TODO 商品inputからHashMap<修正対象CE.id,修正量>取得　
    * この処理は無理やり！！JSON?が利用できる？
    */
   private static Map<Long, Double> reviceGoodsOrder(Map<String, String> input) {
	   System.out.println("reviceGoodsOrder");
	   Map<Long, Double> result = new HashMap<Long, Double>();
	   String F_KEY = "cancel_f";	//キャンセルフラグ
	   String A_KEY = "cancel_a";	//修正値
	   for (String key : input.keySet()) {
		   if(key.startsWith(F_KEY)) {
			   //checkboxは、checkedになっているものだけ送信される
			   //商流移動番号の取得
				String id_str = key.replace(F_KEY, "");
				//注文量の取得
				double newAmount = Double.parseDouble(input.get(A_KEY+id_str));
				long id = Long.parseLong(id_str);
				result.put(id, newAmount);
		   }
	   }
	   return result;
   }

}
