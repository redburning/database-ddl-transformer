package test;

import java.util.List;

import database.ddl.transformer.TestTransformer;
import database.ddl.transformer.core.metadata.BaseMetaDataFetcher;
import database.ddl.transformer.core.metadata.MetaDataFetcherFactory;

public class TestMetaDataFetcher {
	
	public static void getDataBases() throws Exception {
		BaseMetaDataFetcher metaDataFetcher = MetaDataFetcherFactory.getMetaDataFetcher(TestTransformer.postgresqlConn);
		List<String> databases = metaDataFetcher.getDataBases();
		for (String database : databases) {
			System.out.println(database);
		}
		System.out.println("------------------------");
		
		metaDataFetcher = MetaDataFetcherFactory.getMetaDataFetcher(TestTransformer.mysqlConn);
		databases = metaDataFetcher.getDataBases();
		for (String database : databases) {
			System.out.println(database);
		}
		System.out.println("------------------------");
		
		metaDataFetcher = MetaDataFetcherFactory.getMetaDataFetcher(TestTransformer.oracleConn);
		databases = metaDataFetcher.getDataBases();
		for (String database : databases) {
			System.out.println(database);
		}
	}
	
	public static void getTables() throws Exception {
		BaseMetaDataFetcher metaDataFetcher = MetaDataFetcherFactory.getMetaDataFetcher(TestTransformer.mysqlConn);
		List<String> tables = metaDataFetcher.getTables("cdc_test_source");
		for (String table : tables) {
			System.out.println(table);
		}
		System.out.println("------------------------");
		
		metaDataFetcher = MetaDataFetcherFactory.getMetaDataFetcher(TestTransformer.oracleConn);
		tables = metaDataFetcher.getTables("CDC_TEST");
		for (String table : tables) {
			System.out.println(table);
		}
	}
	
	public static void main(String[] args) throws Exception {
		getDataBases();
		// getTables();
	}

}
