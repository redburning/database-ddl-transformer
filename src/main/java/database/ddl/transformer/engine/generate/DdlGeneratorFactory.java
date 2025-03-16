package database.ddl.transformer.engine.generate;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import database.ddl.transformer.engine.bean.DataBaseType;


public class DdlGeneratorFactory {

	public static BaseDdlGenerator getDdlGenerator(DataBaseType databaseType)
			throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class<?> ddlGeneratorClazz = Class.forName(
				DdlGeneratorFactory.class.getPackage().getName() + ".impl." + databaseType.getType() + "DdlGenerator");
		Constructor<?> constructor = ddlGeneratorClazz.getConstructor();
		return (BaseDdlGenerator) constructor.newInstance();
	}
	
}
