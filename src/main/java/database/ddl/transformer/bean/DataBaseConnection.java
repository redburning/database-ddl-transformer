package database.ddl.transformer.bean;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Builder;
import lombok.Data;

/**
 * DataBase Connection Definition
 */
@Data
@Builder(builderClassName = "DataBaseConnectionBuilder")
public class DataBaseConnection {

	private String host;
	
	private int port;
	
	private String user;
	
	private String password;
	
	private String jdbcUrl;
	
	private String database;
	
	private DataBaseType databaseType;
	
	private static Connection connection = null;
	
	public Connection connect() throws SQLException {
		if (connection == null || connection.isClosed()) {
			return DriverManager.getConnection(database == null ? jdbcUrl : jdbcUrl + "/" + database, user, password);
		}
		return connection;
	}
	
	public static class DataBaseConnectionBuilder {
		
		public DataBaseConnectionBuilder jdbcUrl(String jdbcUrl) {
			this.jdbcUrl = jdbcUrl;
			this.host = parseHostFromJdbcUrl(jdbcUrl);
			this.port = parsePortFromJdbcUrl(jdbcUrl);
			this.databaseType = parseDataBaseTypeFromJdbcUrl(jdbcUrl);
			return this;
		}
		
		/**
		 * 从jdbcUrl中解析数据库类型
		 * 
		 * @param jdbcUrl
		 * @return
		 */
		private DataBaseType parseDataBaseTypeFromJdbcUrl(String jdbcUrl) {
			if (jdbcUrl.startsWith("jdbc:mysql")) {
				return DataBaseType.MYSQL;
			} else if (jdbcUrl.startsWith("jdbc:oracle")) {
				return DataBaseType.ORACLE;
			} else if (jdbcUrl.startsWith("jdbc:dm")) {
				return DataBaseType.DAMENG;
			} else if (jdbcUrl.startsWith("jdbc:postgresql")) {
				return DataBaseType.POSTGRESQL;
			} else {
				throw new IllegalArgumentException("Unsupported jdbc type for " + jdbcUrl);
			}
		}
		
		/**
		 * 从jdbcUrl中解析出host
		 * 
		 * @param jdbcUrl
		 * @return
		 */
		private String parseHostFromJdbcUrl(String jdbcUrl) {
			Pattern pattern = Pattern.compile("(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}):(\\d+)");
			Matcher matcher = pattern.matcher(jdbcUrl);
			if (matcher.find()) {
				String ip = matcher.group(1);
				return ip;
			} else {
				throw new IllegalArgumentException("parse host from jdbcUrl '" + jdbcUrl + "' failed");
			}
		}
		
		/**
		 * 从jdbcUrl中解析出port
		 * 
		 * @param jdbcUrl
		 * @return
		 */
		private int parsePortFromJdbcUrl(String jdbcUrl) {
			Pattern pattern = Pattern.compile("(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}):(\\d+)");
	        Matcher matcher = pattern.matcher(jdbcUrl);
	        if (matcher.find()) {
	        	String port = matcher.group(2);
	        	return Integer.valueOf(port);
	        } else {
	        	throw new IllegalArgumentException("parse port from jdbcUrl '" + jdbcUrl + "' failed");
	        }
		}
		
	}
	
}
