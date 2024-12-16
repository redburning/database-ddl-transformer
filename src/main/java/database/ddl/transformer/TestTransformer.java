package database.ddl.transformer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import database.ddl.transformer.bean.DataBaseConnection;
import database.ddl.transformer.bean.DataBaseType;
import database.ddl.transformer.core.transform.DdlExecutor;
import database.ddl.transformer.core.transform.Transformer;

public class TestTransformer {

	private static final Logger logger = LoggerFactory.getLogger(TestTransformer.class);
	
	public static DataBaseConnection mysqlConn = DataBaseConnection.builder()
			.jdbcUrl("jdbc:mysql://10.4.45.207:3306")
			.user("root")
			.password("aaa123+-*/")
			.build();
	
	public static DataBaseConnection mysqlConn2 = DataBaseConnection.builder()
			.jdbcUrl("jdbc:mysql://10.4.130.17:3306")
			.user("root")
			.password("123456")
			.build();
	
	public static DataBaseConnection oracleConn = DataBaseConnection.builder()
			.jdbcUrl("jdbc:oracle:thin:@10.4.45.206:1522/cdc")
			.user("dxp")
			.password("123456")
			.build();
	
	public static DataBaseConnection postgresqlConn = DataBaseConnection.builder()
			.jdbcUrl("jdbc:postgresql://10.4.79.24:5432/postgres")
			.user("postgres")
			.password("Psd1@3+-*/")
			.build();
	
	public static DataBaseConnection damengConn = DataBaseConnection.builder()
			.jdbcUrl("jdbc:dm://10.4.45.207:5236")
			.user("CDC")
			.password("cDC%%2024")
			.build();
	
	public static void testMySQL2Oracle() throws Exception {
		String createTableDDL = new Transformer(mysqlConn, DataBaseType.ORACLE)
				.transformCreateTableDDL("cdc_test_source", "test_table_.*")    // .* for all tables
				.getTransformedCreateTableDDL();
		System.out.println(createTableDDL);
		
		new DdlExecutor(oracleConn).executeSql(createTableDDL);
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
	
	public static void testMySQL2Dameng() throws Exception {
		String createTableDDL = new Transformer(mysqlConn, damengConn)
				.transformCreateTableDDL("cdc_test_source", "stress_testing.*")
				.getTransformedCreateTableDDL();
		logger.info(createTableDDL);
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
		testMySQL2Dameng();
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
