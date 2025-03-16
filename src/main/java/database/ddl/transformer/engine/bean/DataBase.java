package database.ddl.transformer.engine.bean;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Data;

/**
 * DataBase Definition
 */
@Data
public class DataBase {

	/**
	 * 数据库名
	 */
	private String catalog;

	/**
	 * 数据库字符集
	 */
	private String characterSetDataBase;

	/**
	 * 数据库排序规则
	 */
	private String collationDataBase;

	/**
	 * 表定义
	 */
	private Map<String, Table> tablesMap = new LinkedHashMap<String, Table>();

	/**
	 * 添加表定义
	 * 
	 * @param table
	 */
	public void putTable(Table table) {
		this.tablesMap.put(table.getName(), table);
	}

}
