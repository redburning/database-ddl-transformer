package database.ddl.transformer.core.metadata.impl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import database.ddl.transformer.bean.DataBaseConnection;
import database.ddl.transformer.core.metadata.BaseMetaDataFetcher;

public class MySQLMetaDataFetcher extends BaseMetaDataFetcher {

	public MySQLMetaDataFetcher(DataBaseConnection connection) {
		super(connection);
	}

	@Override
	protected void initDefaultKeyAndFunction() {
		this.defaultKeyAndFunction = Arrays.asList("CURRENT_TIMESTAMP", "CURRENT_DATE", "CURRENT_TIME");
	}
	
	@Override
	public List<String> getDataBases() {
		List<String> databaseList = new ArrayList<>();
		try (Connection conn = connection.connect()) {
			DatabaseMetaData metaData = conn.getMetaData();
			ResultSet databases = metaData.getCatalogs();
			while (databases.next()) {
				String tableName = databases.getString(TABLE_CAT);
				databaseList.add(tableName);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return databaseList;
	}
	
}
