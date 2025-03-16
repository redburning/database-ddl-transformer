package database.ddl.transformer.engine.metadata.impl;

import java.util.Arrays;

import database.ddl.transformer.engine.bean.DataBaseConnection;
import database.ddl.transformer.engine.metadata.BaseMetaDataFetcher;


public class PostgreSQLMetaDataFetcher extends BaseMetaDataFetcher {

	public PostgreSQLMetaDataFetcher(DataBaseConnection connection) {
		super(connection);
	}

	@Override
	protected void initDefaultKeyAndFunction() {
		this.defaultKeyAndFunction = Arrays.asList("now\\(\\)", "nextval.*", "(.*?)::character varying");
	}
	
}
