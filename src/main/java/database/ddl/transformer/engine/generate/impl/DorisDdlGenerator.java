package database.ddl.transformer.engine.generate.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import database.ddl.transformer.engine.bean.Column;
import database.ddl.transformer.engine.bean.Table;
import database.ddl.transformer.engine.generate.BaseDdlGenerator;
import database.ddl.transformer.utils.StringUtil;


public class DorisDdlGenerator extends BaseDdlGenerator {

	public DorisDdlGenerator() {
		super();
		this.caseSensitive = CaseSensitive.LOWERCASE;
	}

	@Override
	protected void initDataTypeNeedSizeList() {
		dataTypeNeedSizeList = Arrays.asList("CHAR", "VARCHAR", "BINARY", "VARBINARY");
	}
	
	@Override
	protected void initDataTypeNeedScaleList() {
		// TODO Auto-generated method stub
	}
	
	/**
	 * build primary key sql fragment
	 * 
	 * <pre>
	 * UNIQUE KEY (column1, column2, ...)
	 * </pre>
	 * 
	 * @param database
	 * @param table
	 * @return
	 */
	protected String buildPrimaryKeyFragment(List<String> primaryKeys) {
		if (!StringUtil.isBlank(primaryKeys)) {
			String primaryKeyFragment = String.format("UNIQUE KEY(%s)", 
					String.join(", ",
						primaryKeys.stream()
							.map(item -> quote(convertCase(item)))
							.collect(Collectors.toList())
					)
				);
			String distBucketFragment = String.format("DISTRIBUTED BY HASH(%s) BUCKETS 10",
					String.join(", ",
							primaryKeys.stream()
								.map(item -> quote(convertCase(item)))
								.collect(Collectors.toList())
					)
				);
			return primaryKeyFragment + NEWLINE + distBucketFragment;
		}
		return null;
	}
	
	private String buildPropertiesFrag() {
		String propertiesFrag = "PROPERTIES(" + NEWLINE + INDENT4 + 
				'"' + "replication_num" + '"' + " = " + '"' + 1 + '"' +
				NEWLINE +
				");";
		return propertiesFrag;
	}
	
	@Override
	public String buildCreateTableDDL(Table table) throws Exception {
		String createTableFirstLineFragment = "CREATE TABLE " + quote(convertCase(table.getDatabase())) + "."
				+ quote(convertCase(table.getName()));
		String createTableDDL = createTableFirstLineFragment;
		
		List<String> primaryKeys = table.getPrimaryKeys();
		List<String> columnDefFragmentList = new ArrayList<>();
		// 所有key列必须定义在非key列之前
		for (Column column : table.getColumns()) {
			if (column.isPrimaryKey()) {
				String columnDef = buildColumnDefFragment(column);
				if (!StringUtil.isBlank(columnDef)) {
					columnDefFragmentList.add(columnDef);
				}
			}
		}
		for (Column column : table.getColumns()) {
			if (!column.isPrimaryKey()) {
				String columnDef = buildColumnDefFragment(column);
				if (!StringUtil.isBlank(columnDef)) {
					columnDefFragmentList.add(columnDef);
				}
			}
		}
		
		String columnDefFragment = " (\n" + String.join(",\n", columnDefFragmentList) + "\n)\n";
		createTableDDL += columnDefFragment;
		
		// primary key fragment
		String primaryKeyFragment = buildPrimaryKeyFragment(primaryKeys);
		if (!StringUtil.isBlank(primaryKeyFragment)) {
			createTableDDL += primaryKeyFragment + NEWLINE;
		}
		
		// properties fragment
		String propertiesFragment = buildPropertiesFrag();
		createTableDDL += propertiesFragment;
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
		
		// comments
		if (!StringUtil.isBlank(column.getComment())) {
			columnDef += " COMMENT " + "'" + column.getComment() + "'";
		}
		return columnDef;
	}
	
	@Override
	public String buildCreateIndexDDL(Table table) throws Exception {
		return "";
	}
	
	@Override
	protected String quote(String name) {
		return '`' + name + '`';
	}

}
