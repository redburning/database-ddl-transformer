package database.ddl.transformer.engine.bean;

import java.util.List;

import lombok.Builder;
import lombok.Data;

/**
 * Table Definition
 */
@Data
@Builder
public class Table {
	/**
	 * database name
	 */
	private String database;
	/**
	 * table name
	 */
	private String name;

	/**
	 * 表排序规则
	 */
	private String collation;

	/**
	 * table comment
	 */
	private String comment;

	/**
	 * 列定义
	 */
	private List<Column> columns;

	/**
	 * table primary keys
	 */
	private List<String> primaryKeys;
	
	
	private List<Index> indexes;
	
}
