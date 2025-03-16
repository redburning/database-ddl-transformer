package database.ddl.transformer.engine.bean;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Data;

/**
 * DataBase Connection Definition
 */
@Data
public class DataBaseConnection {

	private String host;
	
	private int port;
	
	private String user;
	
	private String password;
	
	private String baseUrl;
	
	private String database;
	
	private DataBaseType databaseType;
	
	private Map<String, String> additionalProperties;
	
	private static Connection connection = null;	
	
	public static DataBaseConnectionBuilder builder() {
		return new DataBaseConnectionBuilder();
	}
	
	public DataBaseConnection(DataBaseConnectionBuilder builder) {
		this.baseUrl = builder.baseUrl;
		this.user = builder.user;
		this.password = builder.password;
		this.host = builder.host;
		this.port = builder.port;
		this.database = builder.database;
		this.databaseType = builder.databaseType;
		this.additionalProperties = builder.additionalProperties;
	}
	
	public Connection connect() throws SQLException {
		if (connection == null || connection.isClosed()) {
			String url = baseUrl;
			if (database != null) {
				url += "/" + database;
			}
			if (additionalProperties != null && !additionalProperties.isEmpty()) {
				url += "?";
				for (String key : additionalProperties.keySet()) {
					url += key + "=" + additionalProperties.get(key);
				}
			}
			return DriverManager.getConnection(url, user, password);
		}
		return connection;
	}
	
	public static class DataBaseConnectionBuilder {
		
		private String baseUrl;
		private String user;
		private String password;
		private String database;
		private String host;
		private int port;
		private DataBaseType databaseType;
		private Map<String, String> additionalProperties;
		
		public DataBaseConnectionBuilder() {
			this.additionalProperties = new HashMap<>();
		}
		
		public DataBaseConnectionBuilder baseUrl(String baseUrl) {
			this.baseUrl = baseUrl;
			this.host = parseHostFromJdbcUrl(baseUrl);
			this.port = parsePortFromJdbcUrl(baseUrl);
			this.databaseType = parseDataBaseTypeFromJdbcUrl(baseUrl);
			return this;
		}
		
		public DataBaseConnectionBuilder user(String user) {
			this.user = user;
			return this;
		}
		
		public DataBaseConnectionBuilder password(String password) {
			this.password = password;
			return this;
		}
		
		public DataBaseConnectionBuilder database(String database) {
			this.database = database;
			return this;
		}
		
		public DataBaseConnectionBuilder additionalProperties(Map<String, String> additionalProperties) {
			this.additionalProperties = additionalProperties;
			return this;
		}
		
		public DataBaseConnectionBuilder addProperty(String key, String value) {
			this.additionalProperties.put(key, value);
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

		public DataBaseConnection build() {
			return new DataBaseConnection(this);
		}
		
	}
	
}
