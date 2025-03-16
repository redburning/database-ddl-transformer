package database.ddl.transformer.engine.generate.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import database.ddl.transformer.engine.bean.Column;
import database.ddl.transformer.engine.bean.Table;
import database.ddl.transformer.engine.generate.BaseDdlGenerator;
import database.ddl.transformer.utils.StringUtil;

public class MySQLDdlGenerator extends BaseDdlGenerator {

	public MySQLDdlGenerator() {
		super();
		this.caseSensitive = CaseSensitive.LOWERCASE;
	}

	@Override
	protected void initDataTypeNeedSizeList() {
		dataTypeNeedSizeList = Arrays.asList("CHAR", "VARCHAR", "BINARY", "VARBINARY");
	}
	
	@Override
	protected void initDataTypeNeedScaleList() {
		dataTypeNeedScaleList = Arrays.asList("DECIMAL", "NUMERIC");
	}
	
	@Override
	public String buildCreateTableDDL(Table table) throws Exception {
		List<String> columnDefFragmentList = new ArrayList<>();
		for (Column column : table.getColumns()) {
			String columnDef = buildColumnDefFragment(column);
			if (!StringUtil.isBlank(columnDef)) {
				columnDefFragmentList.add(columnDef);
			}
		}
		
		// primary key fragment
		String primaryKeyFragment = buildPrimaryKeyFragment(table.getPrimaryKeys());
		if (!StringUtil.isBlank(primaryKeyFragment)) {
			columnDefFragmentList.add(primaryKeyFragment);
		}
		
		String columnDefFragment = " (\n" + String.join(",\n", columnDefFragmentList) + "\n);";
		
		// build create table ddl
		String createTableFirstLineFragment = "CREATE TABLE " + quote(convertCase(table.getDatabase())) + "."
				+ quote(convertCase(table.getName()));
		String createTableDDL = createTableFirstLineFragment + columnDefFragment;
		return createTableDDL;
	}

	/**
	 * get column definition sql fragment:
	 * 
	 * <pre>
	 * column_name data_type [NOT NULL] [DEFAULT default_value] [COMMENT 'comment_text']
	 * </pre>
	 * 
	 * @param column
	 * @return
	 * @throws Exception
	 */
	@Override
	protected String buildColumnDefFragment(Column column) throws Exception {
		String columnDef = null;
		columnDef = INDENT4 + quote(convertCase(column.getName())) + " " + convertCase(column.getType());
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
		// not null constraint
		if (!column.isNullAble()) {
			columnDef += " NOT NULL";
		}
		// get default value of current column
		if (!StringUtil.isBlank(column.getDefaultValue())) {
			columnDef += " DEFAULT " + column.getDefaultValue();
		}
		
		// comments
		if (!StringUtil.isBlank(column.getComment())) {
			columnDef += " COMMENT " + "'" + column.getComment() + "'";
		}
		return columnDef;
	}
	
	@Override
	protected String quote(String name) {
		return '`' + name + '`';
	}

}
