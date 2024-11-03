package database.ddl.transformer;

import database.ddl.transformer.bean.DataBaseConnection;
import database.ddl.transformer.core.transform.Transformer;

public class TestTransformer {

	public static DataBaseConnection mysqlConn = DataBaseConnection.builder()
			.jdbcUrl("jdbc:mysql://127.0.0.1:3306")
			.user("username")
			.password("password")
			.build();
	
	public static DataBaseConnection mysqlConn2 = DataBaseConnection.builder()
			.jdbcUrl("jdbc:mysql://127.0.0.1:3308")
			.user("username")
			.password("password")
			.build();
	
	public static DataBaseConnection oracleConn = DataBaseConnection.builder()
			.jdbcUrl("jdbc:oracle:thin:@127.0.0.1:1521/cdc")
			.user("username")
			.password("password")
			.build();
	
	public static DataBaseConnection postgresqlConn = DataBaseConnection.builder()
			.jdbcUrl("jdbc:postgresql://127.0.0.1:5432/postgres")
			.user("username")
			.password("password")
			.build();
	
	public static DataBaseConnection damengConn = DataBaseConnection.builder()
			.jdbcUrl("jdbc:dm://127.0.0.1:5236")
			.user("username")
			.password("password")
			.build();
	
	public static void testMySQL2Oracle() throws Exception {
		String createTableDDL = new Transformer(mysqlConn, oracleConn)
				.transformCreateTableDDL("cdc_test_source", ".*")    // .* for all tables
				.getTransformedCreateTableDDL();
		System.out.println(createTableDDL);
	}
	
	public static void testOracle2MySQL() throws Exception {
		String createTableDDL = new Transformer(oracleConn, mysqlConn)
				.transformCreateTableDDL("DXP", "TEST_TABLE_1")
				.getTransformedCreateTableDDL();
		System.out.println(createTableDDL);
	}
	
	public static void testMySQL2PostgreSQL() throws Exception {
		String createTableDDL = new Transformer(mysqlConn, postgresqlConn)
				.transformCreateTableDDL("cdc_test_source", "test_table_1")
				.getTransformedCreateTableDDL();
		System.out.println(createTableDDL);
	}
	
	public static void testPostgreSQL2MySQL() throws Exception {
		String createTableDDL = new Transformer(postgresqlConn, mysqlConn)
				.transformCreateTableDDL("public", "test_table_2")
				.getTransformedCreateTableDDL();
		System.out.println(createTableDDL);
	}
	
	public static void testMySQL2MySQL() throws Exception {
		String createTableDDL = new Transformer(mysqlConn, mysqlConn2)
				.transformCreateTableDDL("cdc_test_source", "test_table_1")
				.getTransformedCreateTableDDL();
		System.out.println(createTableDDL);
	}
	
	public static void testOracle2Oracle() throws Exception {
		String createTableDDL = new Transformer(oracleConn, oracleConn)
				.transformCreateTableDDL("DXP", "TEST_TABLE_1")
				.getTransformedCreateTableDDL();
		System.out.println(createTableDDL);
	}
	
	public static void testDameng2Dameng() throws Exception {
		String createTableDDL = new Transformer(damengConn, damengConn)
				.transformCreateTableDDL("CDC", "TEST_TABLE_1")
				.getTransformedCreateTableDDL();
		System.out.println(createTableDDL);
	}
	
	public static void testDameng2Oracle() throws Exception {
		String createTableDDL = new Transformer(damengConn, oracleConn)
				.transformCreateTableDDL("CDC", "TEST_TABLE_1")
				.getTransformedCreateTableDDL();
		System.out.println(createTableDDL);
	}
	
	public static void testOracle2Dameng() throws Exception {
		String createTableDDL = new Transformer(oracleConn, oracleConn)
				.transformCreateTableDDL("DXP", "TEST_TABLE_1")
				.getTransformedCreateTableDDL();
		System.out.println(createTableDDL);
	}
	
	public static void main(String[] args) throws Exception {
		testMySQL2Oracle();
		// testOracle2MySQL();
		// testMySQL2PostgreSQL();
		// testPostgreSQL2MySQL();
		// testMySQL2MySQL();
		// testOracle2Oracle();
		// testDameng2Dameng();
		// testDameng2Oracle();
		// testOracle2Dameng();
	}
	
}
