package jp.co.isken.tax.application;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import jp.co.isken.taxlib.domain.DateRange;

public class TestUtils {
	public static Date date(String dateStr) {
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
    	try {
			return sdf.parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
    	return null;
	}

	public static String printDate(Date date) {
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
    	return sdf.format(date);
	}
	
	public static DateRange range(String start, String end) {
		return new DateRange(date(start), date(end));
	}

}
