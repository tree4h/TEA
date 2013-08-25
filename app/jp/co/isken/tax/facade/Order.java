package jp.co.isken.tax.facade;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

public class Order {

	private Party client;
	private Contract contract;
	private TaxUnitRule taxUnitRule;
	
	//商流取引
	private CommercialTransaction comTransaction;
	private List<CommercialEntry> comEntry = new ArrayList<CommercialEntry>();
	private List<CommercialAccount> comAccount = new ArrayList<CommercialAccount>();
	
	public Order(Party client, DealType dealType, boolean taxedDeal, TaxedDealType taxedDealType, 
			Date dealDate, ComputeType computeType) {
		this.client = client;
		this.contract = RDBOperator.$findContract(client, dealType.getContractType(), dealDate);
		this.taxUnitRule = contract.getTaxCondition().getTaxUnitRule();
		//商流取引の作成
		this.comTransaction = CommercialTransaction.create(dealType, taxedDeal, taxedDealType, 
				dealDate, contract, computeType);
	}

	public void addDeals(Goods goods, double amount, Unit unit) {
		//課税単位が合計の際のCTへの品目設定
		if(taxUnitRule == TaxUnitRule.SUM || taxUnitRule == TaxUnitRule.CHEAPER) {
			comTransaction.setItem(goods.getItem());
		}
		//商流勘定の検索
		CommercialAccount ca = RDBOperator.$findCA(client, goods);
		//商流移動の作成
		CommercialEntry ce = CommercialEntry.create(amount, unit, comTransaction, ca);
		
		if(!(comAccount.contains(ca))) {
			comAccount.add(ca);
		}
		comEntry.add(ce);
	}
	
	/*
	 * 永続化
	 * 永続化の順番が重要
	 * CEの前に、CT,CAの永続化が必要
	 */
	public void commit() {
		comTransaction.save();
		for(CommercialAccount ca : comAccount) {
			ca.save();
		}
		for(CommercialEntry ce : comEntry) {
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
		return this.comTransaction;
	}

	public List<CommercialEntry> getCommercialEtnries() {
		return this.comEntry;
	}

	public List<CommercialAccount> getCommercialAccounties() {
		return this.comAccount;
	}

	public RoundingRule getTaxRoundingRule() {
		return this.contract.getTaxCondition().getRoundingRule();
	}

	public RoundingRule getPayRoundingRule() {
		return this.contract.getPayCondition().getRoundingRule();
	}
	
}
