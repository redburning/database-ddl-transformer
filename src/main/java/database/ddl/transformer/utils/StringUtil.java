package database.ddl.transformer.utils;

import java.util.List;

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
	
	public static boolean isBlank(List<String> list) {
		return list == null || list.isEmpty();
	}
	
	/**
     * 检查字符串是否包含正则字符
     * 
     * @param str
     * @return
     */
    public static boolean containsRegexCharacter(String str) {
        // 定义正则表达式的特殊字符集合
        String specialCharacters = ".*+?^${}()|[\\]\\\\";
        for (char ch : str.toCharArray()) {
            if (specialCharacters.indexOf(ch) != -1) {
                return true;
            }
        }
        return false;
    }
}
