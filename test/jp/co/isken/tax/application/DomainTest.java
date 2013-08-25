package jp.co.isken.tax.application;

import static jp.co.isken.tax.application.TestUtils.date;
import static jp.co.isken.tax.application.TestUtils.range;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import jp.co.isken.tax.domain.commerce.CommercialTransaction;
import jp.co.isken.tax.domain.commerce.DealType;
import jp.co.isken.tax.domain.commerce.TaxedDealType;
import jp.co.isken.tax.domain.contract.Contract;
import jp.co.isken.tax.domain.contract.ContractType;
import jp.co.isken.tax.domain.contract.DateRange;
import jp.co.isken.tax.domain.contract.PaymentCondition;
import jp.co.isken.tax.domain.contract.RoundingRule;
import jp.co.isken.tax.domain.contract.TaxCondition;
import jp.co.isken.tax.domain.contract.TaxUnitRule;
import jp.co.isken.tax.domain.item.Goods;
import jp.co.isken.tax.domain.item.Item;
import jp.co.isken.tax.domain.item.Unit;
import jp.co.isken.tax.domain.item.UnitPrice;
import jp.co.isken.tax.domain.party.Party;
import jp.co.isken.tax.domain.party.PartyType;
import jp.co.isken.tax.domain.commerce.ComputeType;
import jp.co.isken.tax.facade.Order;
import jp.co.isken.tax.facade.Payment;
import jp.co.isken.tax.view.OrderLinks;
import jp.co.isken.taxlib.domain.TaxRate;

//import org.junit.Ignore;
import org.junit.Test;

public class DomainTest extends BaseModelTest {
	//消費税の登録
	static final TaxRate tr1 = new TaxRate(0.03, jp.co.isken.taxlib.domain.Unit.PERCENT, range("1989/04/01", "1997/04/01"));
	static final TaxRate tr2 = new TaxRate(0.05, jp.co.isken.taxlib.domain.Unit.PERCENT, range("1997/04/01", "2014/04/01"));
	static final TaxRate tr3 = new TaxRate(0.08, jp.co.isken.taxlib.domain.Unit.PERCENT, range("2014/04/01", "2015/10/01"));
	static final TaxRate tr4 = new TaxRate(0.08, jp.co.isken.taxlib.domain.Unit.PERCENT, range("2015/10/01", "2020/04/01"));
	// 消費税条項の作成
	static final TaxCondition RD_S = new TaxCondition(RoundingRule.ROUNDDOWN, TaxUnitRule.SUM);
	static final TaxCondition RD_D = new TaxCondition(RoundingRule.ROUNDDOWN, TaxUnitRule.DETAIL);
	static final TaxCondition RD_C = new TaxCondition(RoundingRule.ROUNDDOWN, TaxUnitRule.CHEAPER);
	static final TaxCondition RO_S = new TaxCondition(RoundingRule.ROUNDOFF, TaxUnitRule.SUM);
	static final TaxCondition RO_D = new TaxCondition(RoundingRule.ROUNDOFF, TaxUnitRule.DETAIL);
	static final TaxCondition RO_C = new TaxCondition(RoundingRule.ROUNDOFF, TaxUnitRule.CHEAPER);
	static final TaxCondition RU_S = new TaxCondition(RoundingRule.ROUNDUP, TaxUnitRule.SUM);
	static final TaxCondition RU_D = new TaxCondition(RoundingRule.ROUNDUP, TaxUnitRule.DETAIL);
	static final TaxCondition RU_C = new TaxCondition(RoundingRule.ROUNDUP, TaxUnitRule.CHEAPER);
	// 代金税条項の作成
	static final PaymentCondition RD = new PaymentCondition(RoundingRule.ROUNDDOWN);
	//商品
	static final Goods goods1 = new Goods("鯛のアラ", new UnitPrice(273, Unit.KG), Item.課税品目);
	static final Goods goods2 = new Goods("ヒラマサのアラ", new UnitPrice(126, Unit.KG), Item.課税品目);
	//取引先、契約
	static final Party client01 = new Party("切り捨て-合計", PartyType.法人);
	static final Contract contract01 = new Contract(ContractType.SALES, client01, new DateRange(date("2013/07/28"),date("2014/07/28")), RD_S, RD,date("2013/07/28"));
	static final Party client02 = new Party("切り捨て-明細", PartyType.法人);
	static final Contract contract02 = new Contract(ContractType.SALES, client02, new DateRange(date("2013/07/28"),date("2014/07/28")), RD_D, RD,date("2013/07/28"));
	static final Party client03 = new Party("切り捨て-安い方", PartyType.法人);
	static final Contract contract03 = new Contract(ContractType.SALES, client03, new DateRange(date("2013/07/28"),date("2014/07/28")), RD_C, RD,date("2013/07/28"));
	static final Party client04 = new Party("四捨五入-合計", PartyType.法人);
	static final Contract contract04 = new Contract(ContractType.SALES, client04, new DateRange(date("2013/07/28"),date("2014/07/28")), RO_S, RD,date("2013/07/28"));
	static final Party client05 = new Party("四捨五入-明細", PartyType.法人);
	static final Contract contract05 = new Contract(ContractType.SALES, client05, new DateRange(date("2013/07/28"),date("2014/07/28")), RO_D, RD,date("2013/07/28"));
	static final Party client06 = new Party("四捨五入-安い方", PartyType.法人);
	static final Contract contract06 = new Contract(ContractType.SALES, client06, new DateRange(date("2013/07/28"),date("2014/07/28")), RO_C, RD,date("2013/07/28"));
	static final Party client07 = new Party("切り上げ-合計", PartyType.法人);
	static final Contract contract07 = new Contract(ContractType.SALES, client07, new DateRange(date("2013/07/28"),date("2014/07/28")), RU_S, RD,date("2013/07/28"));
	static final Party client08 = new Party("切り上げ-明細", PartyType.法人);
	static final Contract contract08 = new Contract(ContractType.SALES, client08, new DateRange(date("2013/07/28"),date("2014/07/28")), RU_D, RD,date("2013/07/28"));
	static final Party client09 = new Party("切り上げ-安い方", PartyType.法人);
	static final Contract contract09 = new Contract(ContractType.SALES, client09, new DateRange(date("2013/07/28"),date("2014/07/28")), RU_C, RD,date("2013/07/28"));
	static final Party client10 = new Party("[内税]切り捨て-合計", PartyType.法人);
	static final Contract contract10 = new Contract(ContractType.SALES, client10, new DateRange(date("2013/07/28"),date("2014/07/28")), RD_S, RD,date("2013/07/28"));
	static final Party client11 = new Party("[非課税]切り捨て-合計", PartyType.法人);
	static final Contract contract11 = new Contract(ContractType.SALES, client11, new DateRange(date("2013/07/28"),date("2014/07/28")), RD_S, RD,date("2013/07/28"));
	//期待結果
	static final double PRICE01 = 1501.0;
	static final double PRICE02 = 1045.0;
	static final double TAX01 = 127.0;
	static final double TAX02 = 128.0;
	static final double TAX03 = 129.0;
	
	private void initTest() {
		//消費税の作成
		tr1.save();
		tr2.save();
		tr3.save();
		tr4.save();
		//商品の作成
		goods1.save();
		goods2.save();
	}

	@Test
	public void UC002ー01ー01() {
		System.out.println("UC002-01-01:[丸め=切り捨て,課税単位=合計]/消費税率=5%");
		initTest();
		//取引先、契約の作成
		client01.save();
		contract01.save();
		//注文
		Order o = new Order(client01, DealType.販売取引, true, TaxedDealType.社外取引, date("2013/07/30"), ComputeType.外税);
		o.addDeals(goods1, 5.5, Unit.KG);
		o.addDeals(goods2, 8.3, Unit.KG);
		//代金計算
		Payment p = new Payment(o);
		p.addPayment();
		double price1 = p.getPayment(goods1);
		double price2 = p.getPayment(goods2);
		double tax = p.getTax();
		//代金確認
		assertThat(price1, is(PRICE01));
		assertThat(price2, is(PRICE02));
		assertThat(tax, is(TAX01));
		//代金登録
		o.commit();
		p.commit();
		//レシート表示
		CommercialTransaction ct = o.getCommercialTransaction();
		OrderLinks ol = new OrderLinks(ct);
		ol.printReceipt();
	}

	@Test
	public void UC002ー01ー02() {
		System.out.println("UC002-01-02:[丸め=切り捨て,課税単位=明細]/消費税率=5%");
		client02.save();
		contract02.save();
		// 注文
		Order o = new Order(client02, DealType.販売取引, true, TaxedDealType.社外取引, date("2013/07/30"), ComputeType.外税);
		o.addDeals(goods1, 5.5, Unit.KG);
		o.addDeals(goods2, 8.3, Unit.KG);
		//　代金計算
		Payment p = new Payment(o);
		p.addPayment();
		double price1 = p.getPayment(goods1);
		double price2 = p.getPayment(goods2);
		double tax = p.getTax();
		// 代金確認
		assertThat(price1, is(PRICE01));
		assertThat(price2, is(PRICE02));
		assertThat(tax, is(TAX01));
		o.commit();
		p.commit();
		//レシート表示
		CommercialTransaction ct = o.getCommercialTransaction();
		OrderLinks ol = new OrderLinks(ct);
		ol.printReceipt();
	}

	@Test
	public void UC002ー01ー03() {
		System.out.println("UC002-01-03:[丸め=切り捨て,課税単位=安い方]/消費税率=5%");
		client03.save();
		contract03.save();
		// 注文
		Order o = new Order(client03, DealType.販売取引, true, TaxedDealType.社外取引, date("2013/07/30"), ComputeType.外税);
		o.addDeals(goods1, 5.5, Unit.KG);
		o.addDeals(goods2, 8.3, Unit.KG);
		//　代金計算
		Payment p = new Payment(o);
		p.addPayment();
		double price1 = p.getPayment(goods1);
		double price2 = p.getPayment(goods2);
		double tax = p.getTax();
		// 代金確認
		assertThat(price1, is(PRICE01));
		assertThat(price2, is(PRICE02));
		assertThat(tax, is(TAX01));
		o.commit();
		p.commit();
		//レシート表示
		CommercialTransaction ct = o.getCommercialTransaction();
		OrderLinks ol = new OrderLinks(ct);
		ol.printReceipt();
	}

	//@Ignore
	@Test
	public void UC002ー01ー04() {
		System.out.println("UC002-01-04:[丸め=四捨五入,課税単位=合計]/消費税率=5%");
		client04.save();
		contract04.save();
		// 注文
		Order o = new Order(client04, DealType.販売取引, true, TaxedDealType.社外取引, date("2013/07/30"), ComputeType.外税);
		o.addDeals(goods1, 5.5, Unit.KG);
		o.addDeals(goods2, 8.3, Unit.KG);
		//　代金計算
		Payment p = new Payment(o);
		p.addPayment();
		double price1 = p.getPayment(goods1);
		double price2 = p.getPayment(goods2);
		double tax = p.getTax();
		// 代金確認
		assertThat(price1, is(PRICE01));
		assertThat(price2, is(PRICE02));
		assertThat(tax, is(TAX01));
		o.commit();
		p.commit();
		//レシート表示
		CommercialTransaction ct = o.getCommercialTransaction();
		OrderLinks ol = new OrderLinks(ct);
		ol.printReceipt();
	}
	//@Ignore
	@Test
	public void UC002ー01ー05() {
		System.out.println("UC002-01-05:[丸め=四捨五入,課税単位=明細]/消費税率=5%");
		client05.save();
		contract05.save();
		// 注文
		Order o = new Order(client05, DealType.販売取引, true, TaxedDealType.社外取引, date("2013/07/30"), ComputeType.外税);
		o.addDeals(goods1, 5.5, Unit.KG);
		o.addDeals(goods2, 8.3, Unit.KG);
		//　代金計算
		Payment p = new Payment(o);
		p.addPayment();
		double price1 = p.getPayment(goods1);
		double price2 = p.getPayment(goods2);
		double tax = p.getTax();
		// 代金確認
		assertThat(price1, is(PRICE01));
		assertThat(price2, is(PRICE02));
		assertThat(tax, is(TAX01));
		o.commit();
		p.commit();
		//レシート表示
		CommercialTransaction ct = o.getCommercialTransaction();
		OrderLinks ol = new OrderLinks(ct);
		ol.printReceipt();
	}
	//@Ignore
	@Test
	public void UC002ー01ー06() {
		System.out.println("UC002-01-06:[丸め=四捨五入,課税単位=安い方]/消費税率=5%");
		client06.save();
		contract06.save();
		// 注文
		Order o = new Order(client06, DealType.販売取引, true, TaxedDealType.社外取引, date("2013/07/30"), ComputeType.外税);
		o.addDeals(goods1, 5.5, Unit.KG);
		o.addDeals(goods2, 8.3, Unit.KG);
		//　代金計算
		Payment p = new Payment(o);
		p.addPayment();
		double price1 = p.getPayment(goods1);
		double price2 = p.getPayment(goods2);
		double tax = p.getTax();
		// 代金確認
		assertThat(price1, is(PRICE01));
		assertThat(price2, is(PRICE02));
		assertThat(tax, is(TAX01));
		o.commit();
		p.commit();
		//レシート表示
		CommercialTransaction ct = o.getCommercialTransaction();
		OrderLinks ol = new OrderLinks(ct);
		ol.printReceipt();
	}
	//@Ignore
	@Test
	public void UC002ー01ー07() {
		System.out.println("UC002-01-07:[丸め=切り上げ,課税単位=合計]/消費税率=5%");
		client07.save();
		contract07.save();
		// 注文
		Order o = new Order(client07, DealType.販売取引, true, TaxedDealType.社外取引, date("2013/07/30"), ComputeType.外税);
		o.addDeals(goods1, 5.5, Unit.KG);
		o.addDeals(goods2, 8.3, Unit.KG);
		//　代金計算
		Payment p = new Payment(o);
		p.addPayment();
		double price1 = p.getPayment(goods1);
		double price2 = p.getPayment(goods2);
		double tax = p.getTax();
		// 代金確認
		assertThat(price1, is(PRICE01));
		assertThat(price2, is(PRICE02));
		assertThat(tax, is(TAX02));
		o.commit();
		p.commit();
		//レシート表示
		CommercialTransaction ct = o.getCommercialTransaction();
		OrderLinks ol = new OrderLinks(ct);
		ol.printReceipt();
	}
	//@Ignore
	@Test
	public void UC002ー01ー08() {
		System.out.println("UC002-01-08:[丸め=切り上げ,課税単位=明細]/消費税率=5%");
		client08.save();
		contract08.save();
		// 注文
		Order o = new Order(client08, DealType.販売取引, true, TaxedDealType.社外取引, date("2013/07/30"), ComputeType.外税);
		o.addDeals(goods1, 5.5, Unit.KG);
		o.addDeals(goods2, 8.3, Unit.KG);
		//　代金計算
		Payment p = new Payment(o);
		p.addPayment();
		double price1 = p.getPayment(goods1);
		double price2 = p.getPayment(goods2);
		double tax = p.getTax();
		// 代金確認
		assertThat(price1, is(PRICE01));
		assertThat(price2, is(PRICE02));
		assertThat(tax, is(TAX03));
		o.commit();
		p.commit();
		//レシート表示
		CommercialTransaction ct = o.getCommercialTransaction();
		OrderLinks ol = new OrderLinks(ct);
		ol.printReceipt();
	}

	//@Ignore
	@Test
	public void UC002ー01ー09() {
		System.out.println("UC002-01-09:[丸め=切り上げ,課税単位=安い方]/消費税率=5%");
		client09.save();
		contract09.save();
		// 注文
		Order o = new Order(client09, DealType.販売取引, true, TaxedDealType.社外取引, date("2013/07/30"), ComputeType.外税);
		o.addDeals(goods1, 5.5, Unit.KG);
		o.addDeals(goods2, 8.3, Unit.KG);
		//　代金計算
		Payment p = new Payment(o);
		p.addPayment();
		double price1 = p.getPayment(goods1);
		double price2 = p.getPayment(goods2);
		double tax = p.getTax();
		// 代金確認
		assertThat(price1, is(PRICE01));
		assertThat(price2, is(PRICE02));
		assertThat(tax, is(TAX02));
		o.commit();
		p.commit();
		//レシート表示
		CommercialTransaction ct = o.getCommercialTransaction();
		OrderLinks ol = new OrderLinks(ct);
		ol.printReceipt();
	}

	//@Ignore
	@Test
	public void UC002ー01ー10() {
		System.out.println("UC002-01-10:[内税計算][丸め=切り捨て,課税単位=合計]/消費税率=5%");
		client10.save();
		contract10.save();
		// 注文
		Order o = new Order(client10, DealType.販売取引, true, TaxedDealType.社外取引, date("2013/07/30"), ComputeType.内税);
		o.addDeals(goods1, 5.5, Unit.KG);
		o.addDeals(goods2, 8.3, Unit.KG);
		//　代金計算
		Payment p = new Payment(o);
		p.addPayment();
		double price1 = p.getPayment(goods1);
		double price2 = p.getPayment(goods2);
		double tax = p.getTax();
		// 代金確認
		assertThat(price1, is(PRICE01));
		assertThat(price2, is(PRICE02));
		assertThat(tax, is(121.0));
		o.commit();
		p.commit();
		//レシート表示
		CommercialTransaction ct = o.getCommercialTransaction();
		OrderLinks ol = new OrderLinks(ct);
		ol.printReceipt();
	}

	//@Ignore
	@Test
	public void UC002ー01ー11() {
		System.out.println("UC002-01-11:[非課税計算][丸め=切り捨て,課税単位=合計]/消費税率=5%");
		client11.save();
		contract11.save();
		// 注文
		Order o = new Order(client11, DealType.販売取引, true, TaxedDealType.社外取引, date("2013/07/30"), ComputeType.非課税);
		o.addDeals(goods1, 5.5, Unit.KG);
		o.addDeals(goods2, 8.3, Unit.KG);
		//　代金計算
		Payment p = new Payment(o);
		p.addPayment();
		double price1 = p.getPayment(goods1);
		double price2 = p.getPayment(goods2);
		double tax = p.getTax();
		// 代金確認
		assertThat(price1, is(PRICE01));
		assertThat(price2, is(PRICE02));
		assertThat(tax, is(0.0));
		o.commit();
		p.commit();
		//レシート表示
		CommercialTransaction ct = o.getCommercialTransaction();
		OrderLinks ol = new OrderLinks(ct);
		ol.printReceipt();
	}

}
