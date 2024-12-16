package database.ddl.transformer.core.generate.impl;

import java.util.Arrays;

import database.ddl.transformer.core.generate.BaseDdlGenerator;

public class DMDdlGenerator extends BaseDdlGenerator {

	public DMDdlGenerator() {
		super();
		this.caseSensitive = CaseSensitive.UPPERCASE;
	}

	@Override
	protected void initDataTypeNeedSizeList() {
		dataTypeNeedSizeList = Arrays.asList("CHAR", "VARCHAR", "BINARY", "VARBINARY");
	}

}
