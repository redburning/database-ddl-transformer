package database.ddl.transformer.core.generate;

import java.lang.reflect.Constructor;

import database.ddl.transformer.bean.DataBaseType;

public class DdlGeneratorFactory {

	public static BaseDdlGenerator getDdlGenerator(DataBaseType databaseType) throws Exception {
		Class<?> ddlGeneratorClazz = Class.forName("database.ddl.transformer.core.generate.impl."
				+ databaseType.getType() + "DdlGenerator");
		Constructor<?> constructor = ddlGeneratorClazz.getConstructor();
		return (BaseDdlGenerator) constructor.newInstance();
	}
	
}
