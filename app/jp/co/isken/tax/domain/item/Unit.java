package jp.co.isken.tax.domain.item;

public enum Unit {
	KG {
		public double toKG(double d) { return d; }
		public double toG(double d) { return d*C0; }
		public double convert(double d, Unit u) { return u.toKG(d); }
	},
	G {
		public double toKG(double d) { return d/C0; }
		public double toG(double d) { return d; }
		public double convert(double d, Unit u) { return u.toG(d); }
	},	
	個;
	
	static final double C0 = 1000.0;
	
	public double convert(double sourceValue, Unit sourceUnit) {
		return sourceValue;
	}
	public double toG(double sourceValue) {
		return sourceValue;
	}
	public double toKG(double sourceValue) {
		return sourceValue;
	}
}
