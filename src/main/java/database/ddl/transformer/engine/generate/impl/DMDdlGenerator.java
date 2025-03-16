package database.ddl.transformer.engine.generate.impl;

import java.util.Arrays;

import database.ddl.transformer.engine.generate.BaseDdlGenerator;


public class DMDdlGenerator extends BaseDdlGenerator {

	public DMDdlGenerator() {
		super();
		this.caseSensitive = CaseSensitive.UPPERCASE;
	}

	@Override
	protected void initDataTypeNeedSizeList() {
		dataTypeNeedSizeList = Arrays.asList("CHAR", "VARCHAR", "BINARY", "VARBINARY");
	}

	@Override
	protected void initDataTypeNeedScaleList() {
		dataTypeNeedScaleList = Arrays.asList("NUMBER", "DECIMAL", "NUMERIC");
	}

}
