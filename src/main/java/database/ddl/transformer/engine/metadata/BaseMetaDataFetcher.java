package database.ddl.transformer.engine.metadata;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import database.ddl.transformer.engine.bean.Column;
import database.ddl.transformer.engine.bean.Column.ColumnBuilder;
import database.ddl.transformer.utils.StringUtil;
import database.ddl.transformer.engine.bean.DataBaseConnection;
import database.ddl.transformer.engine.bean.Index;


public abstract class BaseMetaDataFetcher {

	protected static final String COLUMN_NAME = "COLUMN_NAME";
	protected static final String TYPE_NAME = "TYPE_NAME";
	protected static final String COLUMN_SIZE = "COLUMN_SIZE";
	protected static final String DECIMAL_DIGITS = "DECIMAL_DIGITS";
	protected static final String REMARKS = "REMARKS";
	protected static final String COLUMN_DEF = "COLUMN_DEF";
	protected static final String IS_NULLABLE = "IS_NULLABLE";
	protected static final String TABLE = "TABLE";
	protected static final String TABLE_NAME = "TABLE_NAME";
	protected static final String TABLE_CAT = "TABLE_CAT";
	protected static final String TABLE_SCHEM = "TABLE_SCHEM";
	protected static final String INDEX_NAME = "INDEX_NAME";
	protected static final String TYPE = "TYPE";
	protected static final String NON_UNIQUE = "NON_UNIQUE";
	protected static final String UNIQUE = "UNIQUE";
	protected static final String PRIMARY = "PRIMARY";
	
	
	protected DataBaseConnection connection;
	
	/**
	 * column default key/function
	 */
	protected List<String> defaultKeyAndFunction = new ArrayList<>();
	
	public BaseMetaDataFetcher(DataBaseConnection connection) {
		this.connection = connection;
		initDefaultKeyAndFunction();
	}
	
	/**
	 * Default keywords or functions, should be implemented by subclasses
	 */
	protected abstract void initDefaultKeyAndFunction();
	
	/**
	 * Get primary keys of database.table
	 * 
	 * @param database
	 * @param table
	 * @return
	 */
	public List<String> getPrimaryKeys(String database, String table) {
		List<String> primaryKeyList = new ArrayList<>();
        try (Connection conn = connection.connect()) {
        	DatabaseMetaData metaData = conn.getMetaData();
        	ResultSet primaryKeys = metaData.getPrimaryKeys(database, database, table);
    		while (primaryKeys.next()) {
    			String columnName = primaryKeys.getString(COLUMN_NAME);
    			primaryKeyList.add(columnName);
    		}
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return primaryKeyList;
	}
	
	/**
	 * Get Index info of database.table
	 * 
	 * @param database
	 * @param table
	 * @return
	 */
	public List<Index> getIndexes(String database, String table) {
		List<Index> indexList = new ArrayList<>();
		Map<String, Index> indexMap = new LinkedHashMap<>();
		try (Connection conn = connection.connect()) {
        	DatabaseMetaData metaData = conn.getMetaData();
        	ResultSet indexes = metaData.getIndexInfo(database, database, table, false, false);
    		while (indexes.next()) {
    			String indexName = indexes.getString(INDEX_NAME);
    			// 过滤统计信息行 + 主键
    			if (indexName == null || indexName.equals(PRIMARY)) continue;
    			
    			// 判断索引类型（唯一/非唯一）
                boolean isUnique = !indexes.getBoolean(NON_UNIQUE);
                
                // 处理多列索引
                Index index = indexMap.computeIfAbsent(indexName, k -> 
	                    Index.builder()
	                        .name(indexName)
	                        .isUnique(isUnique)
	                        .columns(new ArrayList<>())
	                        .build()
                );
                
    			String columnName = indexes.getString(COLUMN_NAME);
    			// 添加索引列
                if (columnName != null && !index.getColumns().contains(columnName)) {
                    index.getColumns().add(columnName);
                }
    		}
    		indexList.addAll(indexMap.values());
        } catch (SQLException e) {
            e.printStackTrace();
        }
		return indexList;
	}
	
	/**
	 * Get all columns of database.table
	 * 
	 * @param database
	 * @param table
	 * @return
	 */
	public List<Column> getColumns(String database, String table) {
		List<Column> columnList = new ArrayList<>();
		Map<String, String> columnComments = getColumnComments(database, table);
        try (Connection conn = connection.connect()) {
        	DatabaseMetaData metaData = conn.getMetaData();
        	List<String> primaryKeyList = getPrimaryKeys(database, table);        	
            ResultSet columns = metaData.getColumns(database, database, table, null);
            while (columns.next()) {
                String columnName = columns.getString(COLUMN_NAME);
                String columnType = columns.getString(TYPE_NAME);
                String columnSize = columns.getString(COLUMN_SIZE);
                String columnScale = columns.getString(DECIMAL_DIGITS);
                // TODO: decimal digits获取到了-127的情况，实际值应该是0
                if (!StringUtil.isBlank(columnScale) && columnScale.startsWith("-")) {
                	columnScale = "0";
                }
                String columnDefaultValue = columns.getString(COLUMN_DEF);
                if (!StringUtil.isBlank(columnDefaultValue)) {
                	if (!checkIfNumber(columnDefaultValue) && !checkIfBoolean(columnDefaultValue) 
                			&& !checkIfConservedKey(columnDefaultValue)) {
                		if (!checkIfString(columnDefaultValue)) {
                			columnDefaultValue = "'" + columnDefaultValue + "'";
                		}
                	}
                }
                String columnNullAble = columns.getString(IS_NULLABLE);
				ColumnBuilder columnBuilder = Column.builder();
				Column column = columnBuilder.name(columnName)
						.type(columnType)
						.size(columnSize)
						.scale(columnScale)
						.nullAble("YES".equals(columnNullAble))
						.defaultValue(columnDefaultValue)
						.isPrimaryKey(primaryKeyList.contains(columnName))
						.comment(columnComments.get(columnName))
						.build();
                columnList.add(column);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return columnList;
    }
	
	/**
	 * Get all databases
	 * 
	 * @return
	 */
	public List<String> getDataBases() {
		List<String> databaseList = new ArrayList<>();
		try (Connection conn = connection.connect()) {
			DatabaseMetaData metaData = conn.getMetaData();
			ResultSet databases = metaData.getSchemas();
			while (databases.next()) {
				String tableName = databases.getString(TABLE_SCHEM);
				databaseList.add(tableName);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return databaseList;
	}
	
	/**
	 * Get all tables of database
	 * 
	 * @param database
	 * @return
	 */
	public List<String> getTables(String database) {
		List<String> tableList = new ArrayList<>();
		try (Connection conn = connection.connect()) {
			DatabaseMetaData metaData = conn.getMetaData();
			ResultSet tables = metaData.getTables(database, database, null, new String[] { TABLE });
			while (tables.next()) {
				String tableName = tables.getString(TABLE_NAME);
				tableList.add(tableName);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tableList;
	}
	
	/**
	 * Get column comments of database.table
	 * 
	 * @param database
	 * @param table
	 * @return
	 */
	public Map<String, String> getColumnComments(String database, String table) {
		Map<String, String> columnCommentMap = new HashMap<>();
		try (Connection conn = connection.connect()) {
        	DatabaseMetaData metaData = conn.getMetaData();
            ResultSet columns = metaData.getColumns(database, null, table, null);
            while (columns.next()) {
                String columnName = columns.getString(COLUMN_NAME);
                String columnComment = columns.getString(REMARKS);
                columnCommentMap.put(columnName, columnComment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
		return columnCommentMap;
	}
	
	private boolean checkIfNumber(String str) {
		boolean isNumber = str.matches("-?\\d+(\\.\\d*)?");
		return isNumber;
	}
	
	private boolean checkIfBoolean(String str) {
		return str.matches("(?i)true|false|t|f");
	}
	
	private boolean checkIfString(String str) {
		return str.startsWith("'") && str.endsWith("'");
	}
	
	private boolean checkIfConservedKey(String str) {
		for (String key : defaultKeyAndFunction) {
			if (str.toUpperCase().matches(key)) {
				return true;
			}
		}
		return false;
	}
	
}
