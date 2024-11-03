package database.ddl.transformer.core.metadata;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import database.ddl.transformer.bean.Column;
import database.ddl.transformer.bean.Column.ColumnBuilder;
import database.ddl.transformer.bean.DataBaseConnection;
import database.ddl.transformer.utils.StringUtil;

public abstract class BaseMetaDataFetcher {

	protected static final String COLUMN_NAME = "COLUMN_NAME";
	protected static final String TYPE_NAME = "TYPE_NAME";
	protected static final String COLUMN_SIZE = "COLUMN_SIZE";
	protected static final String REMARKS = "REMARKS";
	protected static final String COLUMN_DEF = "COLUMN_DEF";
	protected static final String IS_NULLABLE = "IS_NULLABLE";
	protected static final String TABLE = "TABLE";
	protected static final String TABLE_NAME = "TABLE_NAME";
	protected static final String TABLE_CAT = "TABLE_CAT";
	protected static final String TABLE_SCHEM = "TABLE_SCHEM";
	
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
