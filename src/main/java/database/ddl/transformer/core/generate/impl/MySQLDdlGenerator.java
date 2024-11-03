package database.ddl.transformer.core.generate.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import database.ddl.transformer.bean.Column;
import database.ddl.transformer.bean.DataBaseConnection;
import database.ddl.transformer.bean.Table;
import database.ddl.transformer.core.generate.BaseDdlGenerator;
import database.ddl.transformer.utils.StringUtil;

public class MySQLDdlGenerator extends BaseDdlGenerator {

	public MySQLDdlGenerator(DataBaseConnection connection) {
		super(connection);
		this.caseSensitive = CaseSensitive.LOWERCASE;
	}

	@Override
	protected void initDataTypeNeedSizeList() {
		dataTypeNeedSizeList = Arrays.asList("CHAR", "VARCHAR", "BINARY", "VARBINARY");
	}
	
	@Override
	public String buildCreateTableDDL(Table table) throws Exception {
		List<String> columnDefFragmentList = new ArrayList<>();
		List<String> columnComments = new ArrayList<>();
		for (Column column : table.getColumns()) {
			String columnDef = buildColumnDefFragment(column);
			if (!StringUtil.isBlank(columnDef)) {
				columnDefFragmentList.add(columnDef);
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
				&& !StringUtil.isBlank(column.getSize())) {
			columnDef += "(" + column.getSize() + ")";
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
