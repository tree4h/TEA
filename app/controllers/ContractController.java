package controllers;

import static controllers.ControllerUtils.makeListEnumValues;
import static controllers.ControllerUtils.date;
import static controllers.ControllerUtils.printInput;

import java.util.Date;
import java.util.List;

import jp.co.isken.tax.domain.contract.Contract;
import jp.co.isken.tax.domain.contract.ContractType;
import jp.co.isken.tax.domain.contract.DateRange;
import jp.co.isken.tax.domain.contract.PaymentCondition;
import jp.co.isken.tax.domain.contract.RoundingRule;
import jp.co.isken.tax.domain.contract.TaxCondition;
import jp.co.isken.tax.domain.contract.TaxUnitRule;
import jp.co.isken.tax.domain.party.Party;
import jp.co.isken.tax.rdb.RDBOperator;

import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

import views.html.contract;
import views.html.contractentry;
import views.html.contractlist;

public class ContractController extends Controller {
    /*
     * 契約一覧画面表示
     */
    public static Result showContractList() {
		List<Contract> contracts = RDBOperator.$findContract();
		return ok(contractlist.render(contracts));
    }
    /*
     * 契約登録画面表示
     */
    public static Result initContract() {
		List<Party> parties = RDBOperator.$findParty();
		List<String> types = makeListEnumValues(ContractType.class);
		List<String> rounds = makeListEnumValues(RoundingRule.class);
		List<String> units = makeListEnumValues(TaxUnitRule.class);
		return ok(contractentry.render(parties, types, rounds, units));
	}
    /*
     * 契約登録
     */
    public static Result entryContract() {
    	DynamicForm input = Form.form().bindFromRequest();
    	printInput(input.data());
    	String party_id = input.data().get("party_id");
		String contract_type = input.data().get("contract_type");
		String start = input.data().get("start");
		String end = input.data().get("end");
		String payment_round = input.data().get("payment_round");
		String tax_round = input.data().get("tax_round");
		String tax_unit = input.data().get("tax_unit");
		String agreement = input.data().get("agreement");
		
		//取引先の特定
		Party party = RDBOperator.$findParty(Long.parseLong(party_id));
		//契約種別
		ContractType ct = ContractType.valueOf(contract_type);
		//契約期間
		DateRange range = new DateRange(date(start), date(end));
		//消費税条項
		TaxCondition taxCondition = new TaxCondition(RoundingRule.valueOf(tax_round),TaxUnitRule.valueOf(tax_unit));
		//代金条項
		PaymentCondition payCondition = new PaymentCondition(RoundingRule.valueOf(payment_round));
		//締結日
		Date whenAgreement = date(agreement);
		//契約の作成
		Contract contract = new Contract(ct, party, range, taxCondition, payCondition, whenAgreement);
		//契約の保存
		contract.save();

		return redirect(controllers.routes.ContractController.showContractList());
	}
    
    /*
     * 契約画面表示
     */
    public static Result showContract(long id) {
		Contract c = RDBOperator.$findContract(id);
		return ok(contract.render(c));
    }

    /*
     * 契約削除
     */
    public static Result deleteContract() {
		DynamicForm input = Form.form().bindFromRequest();
		String contract_id = input.data().get("contract_id");
		Contract c = RDBOperator.$findContract(Long.parseLong(contract_id));
		c.delete();
		return redirect(controllers.routes.ContractController.showContractList());
	}

}
