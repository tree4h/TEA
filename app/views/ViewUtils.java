package views;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ViewUtils {

	public static String printDate(Date date) {
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    	return sdf.format(date);
	}
}
