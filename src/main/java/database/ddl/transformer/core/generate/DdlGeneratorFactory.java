package database.ddl.transformer.core.generate;

import java.lang.reflect.Constructor;

import database.ddl.transformer.bean.DataBaseConnection;

public class DdlGeneratorFactory {

	public static BaseDdlGenerator getDdlGenerator(DataBaseConnection connection) throws Exception {
		Class<?> metaDataFetcherClazz = Class.forName("database.ddl.transformer.core.generate.impl."
				+ connection.getDatabaseType().getType() + "DdlGenerator");
		Constructor<?> constructor = metaDataFetcherClazz.getConstructor(DataBaseConnection.class);
		return (BaseDdlGenerator) constructor.newInstance(connection);
	}
	
}
