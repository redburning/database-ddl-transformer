package database.ddl.transformer.engine.generate.impl;

import java.util.Arrays;

import database.ddl.transformer.engine.generate.BaseDdlGenerator;


public class OracleDdlGenerator extends BaseDdlGenerator {

	public OracleDdlGenerator() {
		super();
		this.caseSensitive = CaseSensitive.UPPERCASE;
	}

	@Override
	protected void initDataTypeNeedSizeList() {
		dataTypeNeedSizeList = Arrays.asList("CHAR", "VARCHAR2", "NCHAR", "NVARCHAR2", "RAW");
	}

	@Override
	protected void initDataTypeNeedScaleList() {
		dataTypeNeedScaleList = Arrays.asList("NUMBER", "DECIMAL", "NUMERIC");
	}

}
