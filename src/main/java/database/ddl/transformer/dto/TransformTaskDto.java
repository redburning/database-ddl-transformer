package database.ddl.transformer.dto;

import java.util.List;

import database.ddl.transformer.entity.DatasourceEntity;
import database.ddl.transformer.entity.TransformTaskEntity;
import database.ddl.transformer.entity.TransformTaskEntity.SubTask;
import lombok.Data;

@Data
public class TransformTaskDto {
	
	private String id;
	
	private String name;

	private DatasourceEntity sourceDatasource;
	
	private DatasourceEntity targetDatasource;
	
	private String sourceDatasourceId;
	
	private String targetDatasourceId;
	
	private String sourceDatabase;
	
	private String targetDatabase;
	
	private List<SubTask> subtasks;
	
	public TransformTaskDto(TransformTaskEntity transformTask) {
		this.id = transformTask.getId();
		this.name = transformTask.getName();
		this.sourceDatasourceId = transformTask.getSourceDatasourceId();
		this.targetDatasourceId = transformTask.getTargetDatasourceId();
		this.sourceDatabase = transformTask.getSourceDatabase();
		this.targetDatabase = transformTask.getTargetDatabase();
		this.subtasks = transformTask.getSubtasks();
	}
	
}
