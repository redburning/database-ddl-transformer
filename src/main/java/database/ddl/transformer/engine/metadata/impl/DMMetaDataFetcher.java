package database.ddl.transformer.engine.metadata.impl;

import java.util.Arrays;

import database.ddl.transformer.engine.bean.DataBaseConnection;
import database.ddl.transformer.engine.metadata.BaseMetaDataFetcher;


public class DMMetaDataFetcher extends BaseMetaDataFetcher {

	public DMMetaDataFetcher(DataBaseConnection connection) {
		super(connection);
	}

	@Override
	protected void initDefaultKeyAndFunction() {
		this.defaultKeyAndFunction = Arrays.asList("CURRENT_TIMESTAMP", "CURRENT_DATE", "CURRENT_TIME");
	}
	
}
