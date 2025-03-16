package database.ddl.transformer.engine.bean;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Index {

	private String name;
	
	private boolean isUnique;
	
	private List<String> columns;
	
	private String comment;
	
}
