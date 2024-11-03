package database.ddl.transformer.utils;

public class StringUtil {

	/**
	 * 判断字符串是否为空
	 * 
	 * @param string 字符串
	 * @return 若为null或全部为空白，则返回true
	 */
	public static boolean isBlank(String string) {
		return string == null || string.trim().length() == 0;
	}
	
}
