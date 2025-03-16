package database.ddl.transformer.engine.transform;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;

import database.ddl.transformer.engine.bean.Column;
import database.ddl.transformer.engine.bean.CreateMode;
import database.ddl.transformer.engine.bean.DataBaseConnection;
import database.ddl.transformer.engine.bean.DataBaseType;
import database.ddl.transformer.engine.bean.Index;
import database.ddl.transformer.engine.bean.Table;
import database.ddl.transformer.engine.generate.BaseDdlGenerator;
import database.ddl.transformer.engine.generate.DdlGeneratorFactory;
import database.ddl.transformer.engine.metadata.BaseMetaDataFetcher;
import database.ddl.transformer.engine.metadata.MetaDataFetcherFactory;
import database.ddl.transformer.utils.JsonUtil;
import database.ddl.transformer.utils.StringUtil;


public class Transformer {

	private static final String DATA_TYPE_MAPPING = "dataTypeMapping";
	private static final String DEFAULT_VALUE_MAPPING = "defaultValueMapping";
	
	// Name of transformer, such as MySQL2Oracle, MySQL2PostgreSQL.
	private String name;
	
	// Metadata fetcher of source database
	private BaseMetaDataFetcher sourceFetcher;
	
	// DDL generator for target database
	private BaseDdlGenerator ddlGenerator;
	
	// database type of source
	private DataBaseType sourceDataBaseType;
	
	// database type of target
	private DataBaseType targetDataBaseType;
	
	// Datatype mapping
	private Map<String, String> dataTypeMapping;
	
	// Default value mapping
	private Map<String, String> defaultValueMapping;
	
	
	public Transformer(DataBaseConnection sourceConn, DataBaseType targetDataBaseType)
			throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		this.sourceDataBaseType = sourceConn.getDatabaseType();
		this.targetDataBaseType = targetDataBaseType;
		this.name = sourceDataBaseType.getType() + "2" + targetDataBaseType.getType();
		this.sourceFetcher = MetaDataFetcherFactory.getMetaDataFetcher(sourceConn);
		this.ddlGenerator = DdlGeneratorFactory.getDdlGenerator(targetDataBaseType);
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
	 * 判断字段是否需要设置size.
	 * <p>
	 * 如果设置，则沿用源端的字段长度;
	 * <p>
	 * 如果不设置，则使用目标端默认的字段长度分配;
	 * 
	 * @param dataType
	 * @return
	 */
	private boolean fieldNeedSize(String dataType) {
		return dataType.indexOf("(") != -1 && dataType.indexOf(")") != -1 && dataType.indexOf(",") == -1;
	}
	
	/**
	 * 判断字段是否需要设置size + scale
	 * 
	 * @param dataType
	 * @return
	 */
	private boolean fieldNeedSizeAndScale(String dataType) {
		return dataType.indexOf("(") != -1 && dataType.indexOf(")") != -1 && dataType.indexOf(",") != -1;
	}
	
	/**
	 * 转换create table ddl
	 * 
	 * @param sourceDatabase	源端database
	 * @param sourceTableReg	源端table，支持正则写法
	 * @return					由于sourceTableReg可能匹配多个table，因此返回结果为map结构
	 * @throws Exception
	 */
	public Map<String, String> transformCreateTableDDL(String sourceDatabase, String sourceTableReg, 
			String targetDatabase, CreateMode createMode) throws Exception {
		// 检查table是否是正则表达式
		if (StringUtil.containsRegexCharacter(sourceTableReg)) {
			List<String> tableList = sourceFetcher.getTables(sourceDatabase);
			return transformBatch(sourceDatabase, tableList, targetDatabase, createMode);
		} else {
			return doTransform(sourceDatabase, sourceTableReg, targetDatabase, createMode);
		}
	}
	
	/**
	 * transform source database.table to target database's create table ddl
	 * 
	 * @param sourceDatabase
	 * @param tableReg... table name reg, for example, ".*" for all tables
	 * @return
	 */
	private Map<String, String> transformBatch(String sourceDatabase, List<String> sourceTables, String targetDatabase, 
			CreateMode createMode) throws Exception {
		ExecutorService executorService = Executors
				.newFixedThreadPool(Math.min(Runtime.getRuntime().availableProcessors(), sourceTables.size()));
		
		List<CompletableFuture<Map<String, String>>> futures = sourceTables.stream()
		        .filter(table -> matchesAnyPattern(table, sourceTables))
		        .map(table -> CompletableFuture.supplyAsync(() -> {
		            try {
		            	Map<String, String> result = doTransform(sourceDatabase, table, targetDatabase, createMode);
		                return result;
		            } catch (Exception e) {
		                throw new RuntimeException("Failed to process table: " + table, e);
		            }
		        }, executorService))
		        .collect(Collectors.toList());
		
		CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
		
		Map<String, String> result = new HashMap<>();
		try {
	        allFutures.get(); // Wait for all futures to complete
	        result = new HashMap<>();
	        for (CompletableFuture<Map<String, String>> future : futures) {
	        	result.putAll(future.get());
	        }
	    } catch (Exception e) {
	        throw new Exception("Failed to process tables", e);
	    } finally {
	        executorService.shutdown(); // Shutdown the executor service
	    }
		return result;
	}
	
	private boolean matchesAnyPattern(String table, List<String> patterns) {
	    for (String pattern : patterns) {
	        if (table.matches(pattern)) {
	            return true;
	        }
	    }
	    return false;
	}
	
	private Map<String, String> doTransform(String sourceDatabase, String sourceTable, String targetDatabase, 
			CreateMode createMode) throws Exception {
		Map<String, String> result = new HashMap<>();
		if (createMode == CreateMode.NOT_CREATE) {
			result.put(sourceDatabase + "." + sourceTable, null);
		} else {
			StringBuilder sb = new StringBuilder();
	        sb.append("-- ------------------------------------------\n");
	        sb.append("-- Table structure for " + sourceTable + "\n");
	        sb.append("-- ------------------------------------------\n");
	        
			if (createMode == CreateMode.DROP_EXISTS_AND_CREATE) {
				sb.append("DROP TABLE " + targetDatabase + "." + sourceTable + ";\n");
			}
	        
	        List<Column> sourceColumns = sourceFetcher.getColumns(sourceDatabase, sourceTable);
	        List<String> primaryKeys = sourceFetcher.getPrimaryKeys(sourceDatabase, sourceTable);
	        List<Index> indexes = sourceFetcher.getIndexes(sourceDatabase, sourceTable);
	        
	        Table sourceTableBean = Table.builder()
	                .name(sourceTable)
	                .database(targetDatabase)
	                .columns(sourceColumns)
	                .primaryKeys(primaryKeys)
	                .indexes(indexes)
	                .build();
	        
	        Table targetTableBean = sameDataBaseType() ? sourceTableBean : transformTable(sourceTableBean);
	        sb.append(ddlGenerator.buildCreateTableDDL(targetTableBean) + "\n\n");
	        // sb.append(ddlGenerator.buildCreateIndexDDL(targetTableBean) + "\n\n");
	        
	        result.put(sourceDatabase + "." + sourceTable, sb.toString());
		}
        
        return result;
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
				if (fieldNeedSize(targetType)) {
					transformedColumn.setSize(column.getSize());
				} else if (fieldNeedSizeAndScale(targetType)) {
					transformedColumn.setSize(column.getSize());
					transformedColumn.setScale(column.getScale());
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
		Table transformedTableBean = Table.builder()
				.name(table.getName())
				.database(table.getDatabase())
				.columns(transformedColumns)
				.primaryKeys(table.getPrimaryKeys())
				.indexes(table.getIndexes())
				.build();
		return transformedTableBean;
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
		return sourceDataBaseType == targetDataBaseType;
	}
	
}
