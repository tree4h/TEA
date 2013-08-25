package controllers;

import static controllers.ControllerUtils.makeListEnumValues;

import java.util.List;

import jp.co.isken.tax.domain.party.Party;
import jp.co.isken.tax.domain.party.PartyType;
import jp.co.isken.tax.rdb.RDBOperator;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

import views.html.party;
import views.html.partyentry;
import views.html.partylist;

public class PartyController extends Controller {
    /*
     * 取引先一覧画面表示
     */
    public static Result showPartyList() {
		List<Party> parties = RDBOperator.$findParty();
		return ok(partylist.render(parties));
    }

    /*
     * 取引先登録画面表示
     */
    public static Result initParty() {
    	List<String> types = makeListEnumValues(PartyType.class);
		return ok(partyentry.render(types));
	}
    
    /*
     * 取引先画面表示
     */
    public static Result showParty(long id) {
		Party p = RDBOperator.$findParty(id);
		return ok(party.render(p));
    }

    /*
     * 取引先登録
     */
    public static Result entryParty() {
		DynamicForm input = Form.form().bindFromRequest();
		String name = input.data().get("party_name");
		String type = input.data().get("party_type");
		Party p = new Party(name, PartyType.valueOf(type));
		p.save();
		return redirect(controllers.routes.PartyController.showPartyList());
	}

    /*
     * 取引先削除
     */
    public static Result deleteParty() {
		DynamicForm input = Form.form().bindFromRequest();
		String party_id = input.data().get("party_id");
		Party p = RDBOperator.$findParty(Long.parseLong(party_id));
		p.delete();
		return redirect(controllers.routes.PartyController.showPartyList());
	}

}
