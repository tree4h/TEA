package jp.co.isken.tax.facade;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import jp.co.isken.tax.domain.commerce.CommercialAccount;
import jp.co.isken.tax.domain.commerce.CommercialEntry;
import jp.co.isken.tax.domain.commerce.CommercialTransaction;
import jp.co.isken.tax.domain.commerce.ComputeType;
import jp.co.isken.tax.domain.commerce.DealType;
import jp.co.isken.tax.domain.commerce.TaxedDealType;
import jp.co.isken.tax.domain.contract.Contract;
import jp.co.isken.tax.domain.contract.RoundingRule;
import jp.co.isken.tax.domain.contract.TaxUnitRule;
import jp.co.isken.tax.domain.item.Goods;
import jp.co.isken.tax.domain.item.Unit;
import jp.co.isken.tax.domain.party.Party;
import jp.co.isken.tax.rdb.RDBOperator;
import jp.co.isken.tax.view.GoodsOrder;

public class Order {
	//取引先
	private Party client;
	//基本契約
	private Contract contract;
	//課税単位
	private TaxUnitRule taxUnitRule;
	
	//商流取引
	private CommercialTransaction ct;
	//商流勘定(いらない？ここで作成することもあるからいる)
	private List<CommercialAccount> cas = new ArrayList<CommercialAccount>();
	//新規作成対象CE
	private List<CommercialEntry> newCE = new ArrayList<CommercialEntry>();
	//修正対象CE
	private List<CommercialEntry> oldCE = new ArrayList<CommercialEntry>();
	//修正CE（赤）
	private List<CommercialEntry> redCE = new ArrayList<CommercialEntry>();
	
	/*
	 * 新規注文
	 */
	public Order(long party_id, DealType dealType, boolean taxedDeal, TaxedDealType taxedDealType, 
			Date dealDate, ComputeType computeType) {
		Party client = RDBOperator.$findParty(party_id);
		
		this.client = client;
		this.contract = RDBOperator.$findContract(client, dealType.getContractType(), dealDate);
		this.taxUnitRule = contract.getTaxCondition().getTaxUnitRule();
		//商流取引の作成
		this.ct = CommercialTransaction.create(dealType, taxedDeal, taxedDealType, 
				dealDate, contract, computeType);
	}
	/*
	 * 修正注文
	 */
	public Order(long ct_id) {
		//商流取引の取得
		this.ct = RDBOperator.$findCT(ct_id);
		this.contract = ct.getContract();
		this.client = contract.getFirstParty();
		this.taxUnitRule = contract.getTaxCondition().getTaxUnitRule();
	}
	
	/*
	 * 新規OrderかどうかはControllerで振り分ける
	 */
	public void createCE(List<GoodsOrder> gos) {
		for(GoodsOrder go : gos) {
			Goods goods = RDBOperator.$findGoods(go.id);
			//課税単位が合計の際のCTへの品目設定
			if(taxUnitRule == TaxUnitRule.SUM || taxUnitRule == TaxUnitRule.CHEAPER) {
				ct.setItem(goods.getItem());
			}
			//商流勘定の検索
			CommercialAccount ca = RDBOperator.$findCA(client, goods);
			//商流移動の作成
			CommercialEntry ce = CommercialEntry.create(go.amount, go.unit, ct, ca);
			if(!(cas.contains(ca))) {
				cas.add(ca);
			}
			newCE.add(ce);
		}
	}
	/*
	 * 修正オーダー
	 */
	public void reviceCE(List<GoodsOrder> gos) {
		for(Map.Entry<CommercialEntry, Double> ce : oldCEs.entrySet()) {
			CommercialEntry oldCE = ce.getKey();
			double newAmount = ce.getValue();
			newCEs.add(oldCE.cancel(newAmount));
		}
		for(GoodsOrder go : gos) {
			Goods goods = RDBOperator.$findGoods(go.id);
			//商流勘定の検索
			CommercialAccount ca = RDBOperator.$findCA(client, goods);
			//商流移動の作成
			CommercialEntry ce = CommercialEntry.create(go.amount, go.unit, ct, ca);
			if(!(cas.contains(ca))) {
				cas.add(ca);
			}
			newCE.add(ce);
		}
	}
	
	/*
	 * 永続化
	 * 永続化の順番が重要
	 * CEの前に、CT,CAの永続化が必要
	 */
	public void commit() {
		ct.save();
		for(CommercialAccount ca : cas) {
			ca.save();
		}
		for(CommercialEntry ce : newCE) {
			ce.save();
		}
	}
	
	public TaxUnitRule getTaxUnitRule() {
		return this.taxUnitRule;
	}

	public Party getClient() {
		return this.client;
	}

	public CommercialTransaction getCommercialTransaction() {
		return this.ct;
	}

	public List<CommercialEntry> getCommercialEtnries() {
		return this.newCE;
	}

	public List<CommercialAccount> getCommercialAccounties() {
		return this.cas;
	}

	public RoundingRule getTaxRoundingRule() {
		return this.contract.getTaxCondition().getRoundingRule();
	}

	public RoundingRule getPayRoundingRule() {
		return this.contract.getPayCondition().getRoundingRule();
	}
	
}
