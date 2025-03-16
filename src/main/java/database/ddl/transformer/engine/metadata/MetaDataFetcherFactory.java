package database.ddl.transformer.engine.metadata;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import database.ddl.transformer.engine.bean.DataBaseConnection;


public class MetaDataFetcherFactory {

	public static BaseMetaDataFetcher getMetaDataFetcher(DataBaseConnection connection)
			throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class<?> metaDataFetcherClazz = Class.forName(MetaDataFetcherFactory.class.getPackage().getName() + ".impl."
				+ connection.getDatabaseType().getType() + "MetaDataFetcher");
		Constructor<?> constructor = metaDataFetcherClazz.getConstructor(DataBaseConnection.class);
		return (BaseMetaDataFetcher) constructor.newInstance(connection);
	}
	
}
