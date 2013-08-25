package jp.co.isken.tax.domain.item;

public enum Item {
	課税品目 {
		@Override
		public jp.co.isken.taxlib.domain.TaxItem getTaxItem() {
			return null;
		}
	},
	非課税品目 {
		@Override
		public jp.co.isken.taxlib.domain.TaxItem getTaxItem() {
			return null;
		}
	};

	public jp.co.isken.taxlib.domain.TaxItem getTaxItem() {
		return null;
	}
}

