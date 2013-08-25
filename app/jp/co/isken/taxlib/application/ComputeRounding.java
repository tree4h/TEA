package jp.co.isken.taxlib.application;

import java.math.BigDecimal;

public enum ComputeRounding {
	// 切り捨て
	ROUNDDOWN {
		public double calc(double d) {
			BigDecimal bd = new BigDecimal(d);
			return bd.setScale(0, BigDecimal.ROUND_DOWN).doubleValue();
		}
		public double divide(double price, double rate) {
			BigDecimal p = new BigDecimal(price);
			BigDecimal r = new BigDecimal(rate);
			return p.divide(r,0,BigDecimal.ROUND_DOWN).doubleValue();
		}
	},
	// 切り上げ
	ROUNDUP {
		public double calc(double d) {
			BigDecimal bd = new BigDecimal(d);
			return bd.setScale(0, BigDecimal.ROUND_UP).doubleValue();
		}
		public double divide(double price, double rate) {
			BigDecimal p = new BigDecimal(price);
			BigDecimal r = new BigDecimal(rate);
			return p.divide(r,0,BigDecimal.ROUND_UP).doubleValue();
		}
	},
	// 四捨五入
	ROUNDOFF {
		public double calc(double d) {
			BigDecimal bd = new BigDecimal(d);
			return bd.setScale(0, BigDecimal.ROUND_HALF_UP).doubleValue();
		}
		public double divide(double price, double rate) {
			BigDecimal p = new BigDecimal(price);
			BigDecimal r = new BigDecimal(rate);
			return p.divide(r,0,BigDecimal.ROUND_HALF_UP).doubleValue();
		}
	};	
	
	public double calc(double d) {
		return d;
	}
	
	public double divide(double price, double rate) {
		return price;
	}
}
