package jp.co.isken.tax.domain.commerce;

import jp.co.isken.tax.domain.contract.ContractType;

public enum DealType {
	販売取引 {
		@Override
		public jp.co.isken.tax.domain.commerce.DealType getDealType() {
			return jp.co.isken.tax.domain.commerce.DealType.販売取引;
		}
		@Override
		public jp.co.isken.tax.domain.contract.ContractType getContractType() {
			return jp.co.isken.tax.domain.contract.ContractType.SALES;
		}
	}, 
	仕入取引 {
		@Override
		public jp.co.isken.tax.domain.commerce.DealType getDealType() {
			return jp.co.isken.tax.domain.commerce.DealType.仕入取引;
		}
		@Override
		public jp.co.isken.tax.domain.contract.ContractType getContractType() {
			return jp.co.isken.tax.domain.contract.ContractType.PURCHASE;
		}
	};
	
	public jp.co.isken.tax.domain.commerce.DealType getDealType() {
		return null;
	}

	public ContractType getContractType() {
		return null;
	}
}
