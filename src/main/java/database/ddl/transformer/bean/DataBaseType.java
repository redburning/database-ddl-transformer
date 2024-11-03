package database.ddl.transformer.bean;

public enum DataBaseType {

    MYSQL("MySQL"), ORACLE("Oracle"), POSTGRESQL("PostgreSQL"), DAMENG("DM");
	
	private String type;
	
	DataBaseType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}
	
}
