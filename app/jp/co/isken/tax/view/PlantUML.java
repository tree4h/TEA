package jp.co.isken.tax.view;

import static views.ViewUtils.printDate;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import jp.co.isken.tax.domain.commerce.CommercialAccount;
import jp.co.isken.tax.domain.commerce.CommercialEntry;
import jp.co.isken.tax.domain.item.Goods;
import jp.co.isken.tax.domain.item.Item;
import jp.co.isken.tax.domain.party.Party;
import jp.co.isken.tax.domain.payment.PaymentAccount;
import jp.co.isken.tax.domain.payment.PaymentAccountType;
import jp.co.isken.tax.domain.payment.PaymentEntry;
import jp.co.isken.tax.domain.payment.PaymentTransaction;

public class PlantUML {
	private OrderLinks ol;
	private String uml;	//PlantUMLの文字列
	private int number; //取引番号
	private String start = "@startuml\n";
	private String title = "title 商流取引";
	private String objects = "";
	private String links = "";
	private String end = "@enduml";
	public String puml_name;//PlantUML-PUMLファイル
	public String png_name;	//PlantUML-PNGファイル

	private static final String U = " -u-> ";
	//private static final String D = " -d-> ";
	private static final String R = " -r-> ";
	private static final String L = " -l-> ";
	
	//出力先
	//private static final String WORK_DIR = "C:\\play-2.1.1\\TEA\\public\\plantuml\\";
	private static final String WORK_DIR = ".\\public\\plantuml\\";

	public PlantUML(OrderLinks ol) {
		this.ol = ol;
		this.number = ol.ct.getId();
		this.title = title+number+"\n";
		this.puml_name = WORK_DIR+"TEA_"+number+".puml";
		this.png_name = "TEA_"+number+".png";
	}
	
	public void printPlantUML() {
		makePlantUML();
		uml = start+title+objects+links+end;
		//System.out.println(uml);
		
		File file = new File(puml_name);
		try {
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			pw.println(uml);
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.execPlantUML();
	}
	
	//pumlファイルからPNGファイル生成
	private void execPlantUML() {
		Runtime r = Runtime.getRuntime();
		try {
			//String cmd = "java -jar "+WORK_DIR+"plantuml.jar -charset UTF-8 -o "+WORK_DIR+" "+puml_name;
			String cmd = "java -jar "+WORK_DIR+"plantuml.jar -charset UTF-8 "+puml_name;
			r.exec(cmd);
			//外部実行コマンドの終了を待つ（puml.pngファイル作成中にアクセスしてエラーとなるため）
			Process p = Runtime.getRuntime().exec(cmd);
			int ret = p.waitFor();
			System.out.print("result of planuml.exe-->"+ret);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void makePlantUML() {
		getPartyString();
		getContractString();
		getCTString();
		for(CommercialEntry ce : ol.ces) {
			getCEString(ce);
		}
		for(CommercialAccount ca : ol.cas) {
			getCAString(ca);
		}
		for(Goods g : ol.goods) {
			getGoodsString(g);
		}
		for(Item item : ol.items) {
			getItemString(item);
		}
		for(PaymentTransaction pt : ol.pts) {
			getPTString(pt);
		}
		for(PaymentEntry pe : ol.pes) {
			getPEString(pe);
		}
		for(PaymentAccount pa : ol.pas) {
			getPAString(pa);
		}
		for(PaymentAccountType p : ol.pat) {
			getPATString(p);
		}
	}

	private void getPartyString() {
		Party client = ol.contract_client;
		long id = client.getId();
		String name = client.getName();
		String obj = "P"+id;
		String str1 = "object \"Party"+id+"\" as "+obj+"\n";
		String str2 = obj+":name = "+name+"\n";
		objects = objects+str1+str2;
	}

	private void getContractString() {
		int id = ol.contract.getId();
		String obj = "C"+id;
		String str1 = "object \"Contract"+id+"\" as "+obj+"\n";
		String str2 = obj+":代金丸め = "+ol.contract.getPayCondition().getRoundingRule()+"\n";
		String str3 = obj+":消費税丸め = "+ol.contract.getTaxCondition().getRoundingRule()+"\n";
		String str4 = obj+":課税単位 = "+ol.contract.getTaxCondition().getTaxUnitRule()+"\n";
		objects = objects+str1+str2+str3+str4;
		//リンク作成
		String obj2 = "P"+ol.contract.getFirstParty().getId();
		String link = obj+U+obj2+"\n";
		links = links+link;
	}

	private void getCTString() {
		int id = ol.ct.getId();
		String obj = "CT"+id;
		String name = printDate(ol.ct.getWhenCharged());
		String str1 = "object \""+obj+"\" as "+obj+"\n";
		String str2 = obj+":取引日 = "+name+"\n";
		objects = objects+str1+str2;
		//リンク作成
		String obj2 = "C"+ol.ct.getContract().getId();
		String link = obj+U+obj2+"\n";
		links = links+link;
	}

	private void getCEString(CommercialEntry ce) {
		int id = ce.getId();
		String obj = "CE"+id;
		String amount = ce.getAmount()+ce.getUnit().toString();
		String str1 = "object \""+obj+"\" as "+obj+"\n";
		String str2 = obj+":量 = "+amount+"\n";
		objects = objects+str1+str2;
		//リンク作成
		String obj2 = "CT"+ce.getTransaction().getId();
		String link1 = obj+L+obj2+"\n";
		String obj3 = "CA"+ce.getAccount().getId();
		String link2 = obj+R+obj3+"\n";
		links = links+link1+link2;
	}

	private void getCAString(CommercialAccount ca) {
		int id = ca.getId();
		String obj = "CA"+id;
		String client = ca.getClient().getName();
		String goods = ca.getGoods().getName();
		String str1 = "object \""+obj+"\" as "+obj+"\n";
		String str2 = obj+":client = "+client+"\n";
		String str3 = obj+":goods = "+goods+"\n";
		objects = objects+str1+str2+str3;
	}

	private void getGoodsString(Goods goods) {
		int id = goods.getId();
		String obj = "G"+id;
		String name = goods.getName();
		String price = goods.getUnitPrice().getPrice()+"円/"+goods.getUnitPrice().getUnit().toString();
		String str1 = "object \"Goods"+id+"\" as "+obj+"\n";
		String str2 = obj+":name = "+name+"\n";
		String str3 = obj+":price = "+price+"\n";
		objects = objects+str1+str2+str3;
		//リンク作成
		String obj2 = "I"+goods.getItem().hashCode();
		String link = obj+U+obj2+"\n";
		links = links+link;
	}

	private void getItemString(Item item) {
		String name = item.toString();
		String obj = "I"+item.hashCode();
		String str1 = "object \""+name+"\" as "+obj+"\n";
		objects = objects+str1;
	}

	private void getPTString(PaymentTransaction pt) {
		int id = pt.getId();
		String obj = "PT"+id;
		String str1 = "object \""+obj+"\" as "+obj+"\n";
		objects = objects+str1;
		//リンク作成
		if(pt.getBasisTransaction() != null) {
			String obj2 = "CT"+pt.getBasisTransaction().getId();
			String link = obj+U+obj2+"\n";
			links = links+link;		
		}
		if(pt.getBasisEntry() != null) {
			String obj2 = "CE"+pt.getBasisEntry().getId();
			String link = obj+U+obj2+"\n";
			links = links+link;		
		}
	}

	private void getPEString(PaymentEntry pe) {
		int id = pe.getId();
		String obj = "PE"+id;
		String price = pe.getPrice()+"円";
		String str1 = "object \""+obj+"\" as "+obj+"\n";
		String str2 = obj+":price = "+price+"\n";
		objects = objects+str1+str2;
		//リンク作成
		String obj2 = "PT"+pe.getTransaction().getId();
		String link1 = obj+L+obj2+"\n";
		String obj3 = "PA"+pe.getAccount().getId();
		String link2 = obj+R+obj3+"\n";
		links = links+link1+link2;
	}

	private void getPAString(PaymentAccount pa) {
		int id = pa.getId();
		String obj = "PA"+id;
		String client = pa.getClient().getName();
		String pat = pa.getAccountType().toString();
		String str1 = "object \""+obj+"\" as "+obj+"\n";
		String str2 = obj+":client = "+client+"\n";
		String str3 = obj+":科目 = "+pat+"\n";
		objects = objects+str1+str2+str3;
	}

	private void getPATString(PaymentAccountType pat) {
		String name = pat.toString();
		String obj = "PAT"+pat.hashCode();
		String str1 = "object \""+name+"\" as "+obj+"\n";
		objects = objects+str1;
	}

}
