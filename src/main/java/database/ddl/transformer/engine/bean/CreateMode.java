package database.ddl.transformer.engine.bean;

public enum CreateMode {

	DROP_EXISTS_AND_CREATE("dropExistAndCreate"), CREATE("create"), NOT_CREATE("notCreate");
	
	private String mode;
	
	CreateMode(String mode) {
		this.mode = mode;
	}

	public String getMode() {
		return mode;
	}
	
	public static CreateMode of(String mode) {
		for (CreateMode createMode : CreateMode.values()) {
			if (createMode.getMode().equals(mode)) {
				return createMode;
			}
		}
		throw new IllegalArgumentException("No CreateMode with mode: " + mode);
	}
	
}
