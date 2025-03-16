package database.ddl.transformer.engine.metadata.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import database.ddl.transformer.engine.bean.DataBaseConnection;
import database.ddl.transformer.engine.metadata.BaseMetaDataFetcher;


public class OracleMetaDataFetcher extends BaseMetaDataFetcher {

	public OracleMetaDataFetcher(DataBaseConnection connection) {
		super(connection);
	}

	@Override
	protected void initDefaultKeyAndFunction() {
		this.defaultKeyAndFunction = Arrays.asList("CURRENT_TIMESTAMP", "CURRENT_DATE", "CURRENT_TIME");
	}
	
	@Override
	public Map<String, String> getColumnComments(String database, String table) {
		Map<String, String> columnComments = new HashMap<>();
		String sql = "SELECT COLUMN_NAME, COMMENTS FROM USER_COL_COMMENTS WHERE TABLE_NAME = ?";
		try (Connection conn = connection.connect();
				PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, table.toUpperCase()); // Oracle表名通常是大写
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String columnName = rs.getString(COLUMN_NAME);
                String comments = rs.getString("COMMENTS");
                columnComments.put(columnName, comments);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
		return columnComments;
	}

}
