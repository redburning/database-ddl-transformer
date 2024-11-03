package database.ddl.transformer.core.generate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import database.ddl.transformer.bean.Column;
import database.ddl.transformer.bean.DataBaseConnection;
import database.ddl.transformer.bean.Table;
import database.ddl.transformer.utils.StringUtil;

public abstract class BaseDdlGenerator {

	public enum CaseSensitive {
		UPPERCASE, LOWERCASE, NONE
	}
	
	protected static final String INDENT4 = "    ";
	protected static final String NEWLINE = "\n";
	
	protected DataBaseConnection connection;
	
	/**
	 * Whether table names and column names are case-sensitive
	 */
	protected CaseSensitive caseSensitive = CaseSensitive.NONE;
	
	protected List<String> dataTypeNeedSizeList = new ArrayList<>();
	
	public BaseDdlGenerator(DataBaseConnection connection) {
		this.connection = connection;
		initDataTypeNeedSizeList();
	}
	
	/**
	 * The fields that require length settings, should be implemented by subclasses.
	 */
	protected abstract void initDataTypeNeedSizeList();
	
	/**
	 * Convert table name, column name, column datatype to uppercase
	 * 
	 * @return
	 */
	public BaseDdlGenerator toUpperCase() {
		this.caseSensitive = CaseSensitive.UPPERCASE;
		return this;
	}
	
	/**
	 * Convert table name, column name, column datatype to lowercase
	 * 
	 * @return
	 */
	public BaseDdlGenerator toLowerCase() {
		this.caseSensitive = CaseSensitive.LOWERCASE;
		return this;
	}
	
	
	/**
	 * build primary key sql fragment
	 * 
	 * <pre>
	 * PRIMARY KEY (column1, column2, ...)
	 * </pre>
	 * 
	 * @param database
	 * @param table
	 * @return
	 */
	protected String buildPrimaryKeyFragment(List<String> primaryKeys) {
		if (primaryKeys != null && !primaryKeys.isEmpty()) {
			String primaryKeyFragment = INDENT4 + String.format("PRIMARY KEY (%s)", String.join(", ",
					primaryKeys.stream().map(item -> quote(convertCase(item))).collect(Collectors.toList())));
			return primaryKeyFragment;
		}
		return null;
	}
	
	/**
	 * get column definition sql fragment:
	 * 
	 * <pre>
	 * column_name data_type [NOT NULL] [DEFAULT default_value]
	 * <br>
	 * [NOT NULL] and [DEFAULT] cannot coexist.
	 * </pre>
	 * 
	 * @param column
	 * @return
	 * @throws Exception
	 */
	protected String buildColumnDefFragment(Column column) throws Exception {
		String columnDef = null;
		columnDef = INDENT4 + quote(convertCase(column.getName())) + " " + column.getType();
		if (dataTypeNeedSizeList.contains(column.getType().toUpperCase()) 
				&& !StringUtil.isBlank(column.getSize())) {
			columnDef += "(" + column.getSize() + ")";
		}
		
		// get default value of current column
		if (!StringUtil.isBlank(column.getDefaultValue())) {
			columnDef += " DEFAULT " + column.getDefaultValue();
		} else {
			// not null constraint
			if (!column.isNullAble()) {
				columnDef += " NOT NULL";
			}
		}
		return columnDef;
	}

	/**
	 * get column comment sql fragment:
	 * 
	 * <pre>
	 * COMMENT ON COLUMN TABLE1.COLUMN1 IS 'this is comment content of column1';
	 * </pre>
	 * 
	 * @param table
	 * @param column
	 * @return
	 */
	protected String buildColumnCommentFragment(String table, Column column) {
		String comment = null;
		if (!StringUtil.isBlank(column.getComment())) {
			comment = String.format("COMMENT ON COLUMN \"%s\".\"%s\" IS '%s';", convertCase(table),
					convertCase(column.getName()), convertCase(column.getComment()));
		}
		return comment;
	}
	
	protected String convertCase(String name) {
		String convert = null;
		switch (caseSensitive) {
			case UPPERCASE:
				convert = name.toUpperCase();
				break;
			case LOWERCASE:
				convert = name.toLowerCase();
				break;
			default:
				convert = name;
				break;
		}
		return convert;
	}
	
	protected String quote(String name) {
		return '"' + name + '"';
	}
	
	public String buildCreateTableDDL(Table table) throws Exception {
		List<String> columnDefFragmentList = new ArrayList<>();
		List<String> columnComments = new ArrayList<>();
		for (Column column : table.getColumns()) {
			String columnDef = buildColumnDefFragment(column);
			if (!StringUtil.isBlank(columnDef)) {
				columnDefFragmentList.add(columnDef);
			}
			
			// get comment of current column
			String columnComment = buildColumnCommentFragment(table.getName(), column);
			if (!StringUtil.isBlank(columnComment)) {
				columnComments.add(columnComment);
			}
		}
		
		// primary key fragment
		String primaryKeyFragment = buildPrimaryKeyFragment(table.getPrimaryKeys());
		columnDefFragmentList.add(primaryKeyFragment);
		
		String columnDefFragment = " (\n" + String.join(",\n", columnDefFragmentList) + "\n);";
		String columnCommentFragment = String.join(NEWLINE, columnComments);
		
		// build create table ddl
		String createTableFirstLineFragment = "CREATE TABLE " + quote(convertCase(table.getName()));
		String createTableDDL = createTableFirstLineFragment + columnDefFragment + NEWLINE + columnCommentFragment;
		return createTableDDL;
	}
	
}
