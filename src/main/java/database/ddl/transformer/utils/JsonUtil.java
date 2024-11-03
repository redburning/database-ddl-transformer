package database.ddl.transformer.utils;

import java.io.InputStream;
import java.util.Scanner;

import com.alibaba.fastjson.JSONObject;

public class JsonUtil {

	public static JSONObject loadJson(String fileName) {
		try (InputStream inputStream = JsonUtil.class.getClassLoader().getResourceAsStream(fileName);
				Scanner scanner = new Scanner(inputStream)) {
			String content = scanner.useDelimiter("\\A").next();
			JSONObject jsonObject = JSONObject.parseObject(content);
			return jsonObject;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
