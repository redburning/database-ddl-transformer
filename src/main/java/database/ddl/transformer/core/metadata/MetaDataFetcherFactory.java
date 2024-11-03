package database.ddl.transformer.core.metadata;

import java.lang.reflect.Constructor;

import database.ddl.transformer.bean.DataBaseConnection;

public class MetaDataFetcherFactory {

	public static BaseMetaDataFetcher getMetaDataFetcher(DataBaseConnection connection) throws Exception {
		Class<?> metaDataFetcherClazz = Class.forName("database.ddl.transformer.core.metadata.impl."
				+ connection.getDatabaseType().getType() + "MetaDataFetcher");
		Constructor<?> constructor = metaDataFetcherClazz.getConstructor(DataBaseConnection.class);
		return (BaseMetaDataFetcher) constructor.newInstance(connection);
	}
	
}
