package database.ddl.transformer.bean;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

/**
 * Column Definition
 */
@Data
@Builder
public class Column {

	@Tolerate
	public Column() {}

	/**
	 * 列名
	 */
	private String name;

	/**
	 * 列数据类型 例如：bigint(20)
	 */
	private String type;
	
	private String size;

	/**
	 * 列顺序
	 */
	private int order;

	/**
	 * 是否允许为空
	 */
	private boolean nullAble;

	/**
	 * 默认值定义
	 */
	private String defaultValue;

	/**
	 * 字段描述
	 */
	private String comment;
	
	/**
	 * 是否是主键
	 */
	private boolean isPrimaryKey;

}
