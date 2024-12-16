package database.ddl.transformer.core.transform;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import database.ddl.transformer.bean.DataBaseConnection;
import database.ddl.transformer.utils.StringUtil;

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
		try (Connection connection = conn.connect(); Statement stmt = connection.createStatement()) {
			for (String sql : sqlBatch.split(";")) {
				if (!StringUtil.isBlank(sql)) {
					stmt.execute(sql.trim());
				}
			}
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		}
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
