package database.ddl.transformer.core.metadata.impl;

import java.util.Arrays;

import database.ddl.transformer.bean.DataBaseConnection;
import database.ddl.transformer.core.metadata.BaseMetaDataFetcher;

public class DMMetaDataFetcher extends BaseMetaDataFetcher {

	public DMMetaDataFetcher(DataBaseConnection connection) {
		super(connection);
	}

	@Override
	protected void initDefaultKeyAndFunction() {
		this.defaultKeyAndFunction = Arrays.asList("CURRENT_TIMESTAMP", "CURRENT_DATE", "CURRENT_TIME");
	}
	
}
