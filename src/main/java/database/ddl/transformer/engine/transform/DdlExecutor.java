package database.ddl.transformer.engine.transform;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import database.ddl.transformer.engine.bean.DataBaseConnection;


public class DdlExecutor {

	private DataBaseConnection conn;
	
	public DdlExecutor(DataBaseConnection conn) {
		this.conn = conn;
	}
	
	/**
	 * 执行sql, 可以是';'分割的多个sql
	 * 
	 * @param sqlBatch
	 * @return
	 * @throws SQLException
	 */
	public boolean executeSql(String sqlBatch) throws SQLException {
		List<String> sqlList = new ArrayList<>();
		for (String sql : sqlBatch.split(";")) {
			sql = sql.trim();
			if (!sql.isEmpty()) {
				sqlList.add(sql);
			}
		}
		return executeSql(sqlList);
	}
	
	public boolean executeSql(String[] sqlArray) throws SQLException {
		return executeSql(Arrays.asList(sqlArray));
	}
	
	public boolean executeSql(Collection<String> sqlCollection) throws SQLException {
		return executeSql(new ArrayList<>(sqlCollection));
	}
	
	public boolean executeSql(List<String> sqlList) throws SQLException {
		try (Connection connection = conn.connect(); Statement stmt = connection.createStatement()) {
			for (String sql : sqlList) {
				stmt.execute(sql);
			}
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		}
	}
}
