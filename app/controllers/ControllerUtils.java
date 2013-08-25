package controllers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ControllerUtils {

	/*
	 * Enum型の定数をList<String>に変換する
	 */
	public static List<String> makeListEnumValues(Class<? extends Enum<?>> enumCls) {
    	List<String> result = new ArrayList<String>();
    	for(Enum<?> e : enumCls.getEnumConstants()) {
    		result.add(e.name());
    	}
    	return result;
	}

	/*
	 * Formの日付けinputデータ(yyyy-MM-dd)をDate型に変換する
	 * HTML5のデフォルト(yyyy-MM-dd)
	 * 時分秒は00:00:00となる
	 */
	public static Date date(String date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			return sdf.parse(date);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	/*
	 * Formのinputデータを標準出力する
	 */
	public static void printInput(Map<String,String> input) {
		for (String key : input.keySet()) {
			System.out.println("key=" + key + ", value=" + input.get(key));
		}
	}

}
