package jp.co.isken.tax.domain.contract;

import java.math.BigDecimal;

import jp.co.isken.taxlib.application.ComputeRounding;

public enum RoundingRule {
	// 切り捨て
	ROUNDDOWN {
		public double calc(double d) {
			BigDecimal bd = new BigDecimal(d);
			return bd.setScale(0, BigDecimal.ROUND_DOWN).doubleValue();
		}
		@Override
		public jp.co.isken.taxlib.application.ComputeRounding getComputeRounding() {
			return ComputeRounding.ROUNDDOWN;
		}
	},
	// 切り上げ
	ROUNDUP {
		public double calc(double d) {
			BigDecimal bd = new BigDecimal(d);
			return bd.setScale(0, BigDecimal.ROUND_UP).doubleValue();
		}
		@Override
		public jp.co.isken.taxlib.application.ComputeRounding getComputeRounding() {
			return ComputeRounding.ROUNDUP;
		}
	},
	// 四捨五入
	ROUNDOFF {
		public double calc(double d) {
			BigDecimal bd = new BigDecimal(d);
			return bd.setScale(0, BigDecimal.ROUND_HALF_UP).doubleValue();
		}
		@Override
		public jp.co.isken.taxlib.application.ComputeRounding getComputeRounding() {
			return ComputeRounding.ROUNDOFF;
		}
	};	
	
	public double calc(double d) {
		return d;
	}

	public jp.co.isken.taxlib.application.ComputeRounding getComputeRounding() {
		return null;
	}

}
