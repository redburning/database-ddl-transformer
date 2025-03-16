package database.ddl.transformer.engine.bean;

public enum DataBaseType {

    MYSQL("MySQL"), ORACLE("Oracle"), POSTGRESQL("PostgreSQL"), DAMENG("DM"), Doris("Doris");
	
	private String type;
	
	DataBaseType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}
	
	/**
     * 根据字符串获取对应的枚举值
     * 
     * @param value 可以是枚举名称（不区分大小写）或枚举的 type 值
     * @return 对应的枚举值
     * @throws IllegalArgumentException 如果找不到对应的枚举值
     */
    public static DataBaseType of(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }
        
        // 遍历所有枚举值
        for (DataBaseType dbType : DataBaseType.values()) {
            // 检查枚举名称是否匹配（不区分大小写）
            if (dbType.name().equalsIgnoreCase(value)) {
                return dbType;
            }
            // 检查枚举的 type 值是否匹配（不区分大小写）
            if (dbType.getType().equalsIgnoreCase(value)) {
                return dbType;
            }
        }
        
        // 如果找不到对应的枚举值，抛出异常
        throw new IllegalArgumentException("No enum constant for value: " + value);
    }
	
}
