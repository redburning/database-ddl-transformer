package database.ddl.transformer.core.generate.impl;

import java.util.Arrays;

import database.ddl.transformer.core.generate.BaseDdlGenerator;

public class PostgreSQLDdlGenerator extends BaseDdlGenerator {

	public PostgreSQLDdlGenerator() {
		super();
		this.caseSensitive = CaseSensitive.LOWERCASE;
	}

	@Override
	protected void initDataTypeNeedSizeList() {
		dataTypeNeedSizeList = Arrays.asList("CHAR", "VARCHAR", "VARBIT");
	}

}
