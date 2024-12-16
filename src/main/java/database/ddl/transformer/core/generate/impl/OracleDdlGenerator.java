package database.ddl.transformer.core.generate.impl;

import java.util.Arrays;

import database.ddl.transformer.core.generate.BaseDdlGenerator;

public class OracleDdlGenerator extends BaseDdlGenerator {

	public OracleDdlGenerator() {
		super();
		this.caseSensitive = CaseSensitive.UPPERCASE;
	}

	@Override
	protected void initDataTypeNeedSizeList() {
		dataTypeNeedSizeList = Arrays.asList("CHAR", "VARCHAR2", "NCHAR", "NVARCHAR2", "RAW");
	}

}
