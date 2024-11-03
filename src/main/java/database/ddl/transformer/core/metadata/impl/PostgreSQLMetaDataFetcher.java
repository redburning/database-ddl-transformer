package database.ddl.transformer.core.metadata.impl;

import java.util.Arrays;

import database.ddl.transformer.bean.DataBaseConnection;
import database.ddl.transformer.core.metadata.BaseMetaDataFetcher;

public class PostgreSQLMetaDataFetcher extends BaseMetaDataFetcher {

	public PostgreSQLMetaDataFetcher(DataBaseConnection connection) {
		super(connection);
	}

	@Override
	protected void initDefaultKeyAndFunction() {
		this.defaultKeyAndFunction = Arrays.asList("now\\(\\)", "nextval.*", "(.*?)::character varying");
	}
	
}
