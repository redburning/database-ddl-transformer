package database.ddl.transformer.engine.generate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import database.ddl.transformer.engine.bean.Column;
import database.ddl.transformer.engine.bean.Index;
import database.ddl.transformer.engine.bean.Table;
import database.ddl.transformer.utils.StringUtil;


public abstract class BaseDdlGenerator {

	public enum CaseSensitive {
		UPPERCASE, LOWERCASE, NONE
	}
	
	protected static final String INDENT4 = "    ";
	protected static final String NEWLINE = "\n";
	
	/**
	 * Whether table names and column names are case-sensitive
	 */
	protected CaseSensitive caseSensitive = CaseSensitive.NONE;
	
	/*
	 * Data type should set size parameter, such as char(10)
	 */
	protected List<String> dataTypeNeedSizeList = new ArrayList<>();
	
	/**
	 * Data type should set size & scale parameter, such as number(10, 2)
	 */
	protected List<String> dataTypeNeedScaleList = new ArrayList<>();
	
	public BaseDdlGenerator() {
		initDataTypeNeedSizeList();
		initDataTypeNeedScaleList();
	}
	
	/**
	 * The fields that require length settings, should be implemented by subclasses.
	 */
	protected abstract void initDataTypeNeedSizeList();
	
	/**
	 * The fields that require size and scale settings, should be implemented by subclasses.
	 */
	protected abstract void initDataTypeNeedScaleList();
	
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
		if (!StringUtil.isBlank(primaryKeys)) {
			String primaryKeyFragment = INDENT4 + String.format("PRIMARY KEY (%s)", 
					String.join(", ",
						primaryKeys.stream()
							.map(item -> quote(convertCase(item)))
							.collect(Collectors.toList())
					)
				);
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
				&& !StringUtil.isBlank(column.getSize())
				&& !column.getSize().equals("0")) {
			columnDef += "(" + column.getSize() + ")";
		} else if (dataTypeNeedScaleList.contains(column.getType().toUpperCase()) 
				&& !StringUtil.isBlank(column.getSize()) 
				&& !StringUtil.isBlank(column.getScale())
				&& !column.getSize().equals("0")) {
			columnDef += "(" + column.getSize() + ", " + column.getScale() + ")";
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
		if (!StringUtil.isBlank(primaryKeyFragment)) {
			columnDefFragmentList.add(primaryKeyFragment);
		}
		
		String columnDefFragment = " (\n" + String.join(",\n", columnDefFragmentList) + "\n);";
		String columnCommentFragment = String.join(NEWLINE, columnComments);
		
		// build create table ddl
		String createTableFirstLineFragment = "CREATE TABLE " + quote(convertCase(table.getDatabase())) + "."
				+ quote(convertCase(table.getName()));
		String createTableDDL = createTableFirstLineFragment + columnDefFragment + NEWLINE + columnCommentFragment;
		return createTableDDL;
	}
	
	protected String buildIndexDefFragment(String database, String table, Index index, List<String> primaryKeys) {
		// 避免重复索引主键列
		if (index != null && index.getColumns().size() == 1 
				&& primaryKeys.contains(index.getColumns().get(0)))
			return null;
		String createIndexFragment = "CREATE " + 
				(index.isUnique() ? "UNIQUE " : "") + "INDEX " +
				quote(index.getName()) + 
				" ON " + quote(database) + "." + quote(table) +
				" (" + index.getColumns().stream()
							.map(e -> quote(e))
							.collect(Collectors.joining(", ")) + 
				");";
		return createIndexFragment;
	}
	
	public String buildCreateIndexDDL(Table table) throws Exception {
		List<String> indexDefFragments = new ArrayList<>();
		for (Index index : table.getIndexes()) {
			String createIndexFragment = buildIndexDefFragment(table.getDatabase(), table.getName(), 
					index, table.getPrimaryKeys());
			if (!StringUtil.isBlank(createIndexFragment)) {
				indexDefFragments.add(createIndexFragment);
			}
		}
		return String.join(NEWLINE, indexDefFragments);
	}
	
}
