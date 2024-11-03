package database.ddl.transformer.core.transform;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

import database.ddl.transformer.bean.Column;
import database.ddl.transformer.bean.DataBaseConnection;
import database.ddl.transformer.bean.Table;
import database.ddl.transformer.core.generate.BaseDdlGenerator;
import database.ddl.transformer.core.generate.DdlGeneratorFactory;
import database.ddl.transformer.core.metadata.BaseMetaDataFetcher;
import database.ddl.transformer.core.metadata.MetaDataFetcherFactory;
import database.ddl.transformer.utils.JsonUtil;
import database.ddl.transformer.utils.StringUtil;

public class Transformer {

	private static final Logger logger = LoggerFactory.getLogger(Transformer.class);
	
	private static final String DATA_TYPE_MAPPING = "dataTypeMapping";
	private static final String DEFAULT_VALUE_MAPPING = "defaultValueMapping";
	
	/**
	 * Name of transformer, such as MySQL2Oracle, MySQL2PostgreSQL, Oracle2MySQL.
	 */
	private String name;
	
	/**
	 * Metadata fetcher of source database
	 */
	private BaseMetaDataFetcher sourceFetcher;
	
	/**
	 * DDL generator for target database
	 */
	private BaseDdlGenerator ddlGenerator;
	
	/**
	 * Source database connection
	 */
	private DataBaseConnection sourceConn;
	
	/**
	 * Target database connection
	 */
	private DataBaseConnection targetConn;
	
	/**
	 * Datatype mapping
	 */
	private Map<String, String> dataTypeMapping;
	
	/**
	 * Default value mapping
	 */
	private Map<String, String> defaultValueMapping;
	
	/**
	 * Transformed result - Create table DDL
	 */
	private String transformedCreateTableDDL;
	
	public Transformer(DataBaseConnection sourceConn, DataBaseConnection targetConn) throws Exception {
		this.name = sourceConn.getDatabaseType().getType() + "2" + targetConn.getDatabaseType().getType();
		this.sourceFetcher = MetaDataFetcherFactory.getMetaDataFetcher(sourceConn);
		this.ddlGenerator = DdlGeneratorFactory.getDdlGenerator(targetConn);
		this.sourceConn = sourceConn;
		this.targetConn = targetConn;
		initMapping();
	}
	
	private void initMapping() {
		if (!sameDataBaseType()) {
			JSONObject mappingJson = JsonUtil.loadJson(name + ".json");
			JSONObject typeMappingObject = mappingJson.getJSONObject(DATA_TYPE_MAPPING);
			dataTypeMapping = new HashMap<>();
			for (String key : typeMappingObject.keySet()) {
				dataTypeMapping.put(key, typeMappingObject.getString(key));
			}
			JSONObject defaultMappingObject = mappingJson.getJSONObject(DEFAULT_VALUE_MAPPING);
			defaultValueMapping = new HashMap<>();
			for (String key : defaultMappingObject.keySet()) {
				defaultValueMapping.put(key, defaultMappingObject.getString(key));
			}
		}
	}
	
	
	/**
	 * 判断字段是否需要设置size和scale.
	 * <p>
	 * 如果设置，则沿用源端的字段长度;
	 * <p>
	 * 如果不设置，则使用目标端默认的字段长度分配;
	 * 
	 * @param dataType
	 * @return
	 */
	private boolean fieldNeedSizeAndScale(String dataType) {
		return dataType.indexOf("(") != -1 && dataType.indexOf(")") != -1;
	}
	
	/**
	 * transform source database.table to target database's create table ddl
	 * 
	 * @param database
	 * @param tableReg table name reg, for example, ".*" for all tables
	 * @return
	 */
	public Transformer transformCreateTableDDL(String database, String tableReg) throws Exception {
		List<String> tableList = sourceFetcher.getTables(database);
		StringBuilder sb = new StringBuilder();
		for (String table : tableList) {
			if (table.matches(tableReg)) {
				sb.append("-- ------------------------------------------\n");
				sb.append("-- Table structure for " + table + "\n");
				sb.append("-- ------------------------------------------\n");
				List<Column> sourceColumns = sourceFetcher.getColumns(database, table);
				List<String> primaryKeys = sourceFetcher.getPrimaryKeys(database, table);
				Table sourceTableBean = Table.builder().name(table).database(database)
						.columns(sourceColumns).primaryKeys(primaryKeys).build();
				Table targetTableBean = null;
				if (sameDataBaseType()) {
					targetTableBean = sourceTableBean;
				} else {
					targetTableBean = transformTable(sourceTableBean);
				}
				sb.append(ddlGenerator.buildCreateTableDDL(targetTableBean));
				sb.append("\n\n");
			}
		}
		transformedCreateTableDDL = sb.toString();
		return this;
	}
	
	private Table transformTable(Table table) throws Exception {
		List<Column> transformedColumns = new ArrayList<>();
		for (Column column : table.getColumns()) {
			String sourceType = column.getType();
			Column transformedColumn = new Column();
			Optional<String> matchedType = findMatchedDataType(sourceType);
			if (matchedType.isPresent()) {
				// column name
				transformedColumn.setName(column.getName());
				
				// column type & size
				String targetType = dataTypeMapping.get(matchedType.get());
				String targetTypeWithoutSize = targetType.replaceAll("\\(.*\\)", "");
				transformedColumn.setType(targetTypeWithoutSize);
				if (fieldNeedSizeAndScale(targetType)) {
					transformedColumn.setSize(column.getSize());
				}
				
				// not null constraint
				transformedColumn.setNullAble(column.isNullAble());
				
				// default
				String sourceDefaultValue = column.getDefaultValue();
				if (!StringUtil.isBlank(sourceDefaultValue)) {
					Optional<String> matchedKey = findMatchedDefaultKey(sourceDefaultValue);
					if (matchedKey.isPresent()) {
						String targetDefaultValue = defaultValueMapping.get(matchedKey.get());
						if (!StringUtil.isBlank(targetDefaultValue)) {
							targetDefaultValue = sourceDefaultValue.replaceAll(matchedKey.get(), targetDefaultValue);
							transformedColumn.setDefaultValue(targetDefaultValue);
						}
					} else {
						transformedColumn.setDefaultValue(sourceDefaultValue);
					}
				}
				
				// comment 
				transformedColumn.setComment(column.getComment());
				transformedColumns.add(transformedColumn);
			} else {
				throw new Exception("datatype mapping of [" + sourceType + "] was not found.");
			}
		}
		Table transformedTableBean = Table.builder().name(table.getName()).database(table.getDatabase())
				.columns(transformedColumns).primaryKeys(table.getPrimaryKeys()).build();
		return transformedTableBean;
	}
	
	/**
	 * Get converted create table DDL
	 * 
	 * @return
	 */
	public String getTransformedCreateTableDDL() {
		return transformedCreateTableDDL;
	}
	
	/**
	 * Auto execute create table in target database
	 * 
	 * @return true for success, false for fail
	 */
	public boolean executeCreateTable() {
		logger.info(transformedCreateTableDDL);
		try (Connection conn = targetConn.connect(); Statement statement = conn.createStatement()) {
			statement.execute(transformedCreateTableDDL);
			logger.info("execute create table success");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private Optional<String> findMatchedDataType(String str) {
		return dataTypeMapping.keySet().stream()
				.filter(key -> str.replaceAll("\\(.*\\)", "").equalsIgnoreCase(key.replaceAll("\\(.*\\)", "")))
				.findAny();
	}
	
	private Optional<String> findMatchedDefaultKey(String str) {
		return defaultValueMapping.keySet().stream()
				.filter(key -> str.equals(key) || str.matches(key))
				.findAny();
	}
	
	private boolean sameDataBaseType() {
		return sourceConn.getDatabaseType() == targetConn.getDatabaseType();
	}
	
}
