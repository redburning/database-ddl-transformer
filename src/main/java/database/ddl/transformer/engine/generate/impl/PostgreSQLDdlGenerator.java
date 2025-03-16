package database.ddl.transformer.engine.generate.impl;

import java.util.Arrays;

import database.ddl.transformer.engine.generate.BaseDdlGenerator;


public class PostgreSQLDdlGenerator extends BaseDdlGenerator {

	public PostgreSQLDdlGenerator() {
		super();
		this.caseSensitive = CaseSensitive.LOWERCASE;
	}

	@Override
	protected void initDataTypeNeedSizeList() {
		dataTypeNeedSizeList = Arrays.asList("CHAR", "VARCHAR", "VARBIT");
	}

	@Override
	protected void initDataTypeNeedScaleList() {
		dataTypeNeedScaleList = Arrays.asList("DECIMAL", "NUMERIC");
	}

}
