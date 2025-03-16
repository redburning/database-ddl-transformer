package database.ddl.transformer.service;

import java.util.List;

import database.ddl.transformer.entity.TransformTaskEntity;

public interface ITransformTaskService {

	public List<TransformTaskEntity> listTransformTasks();
	
	public TransformTaskEntity saveTransformTask(TransformTaskEntity taskEntity);
	
	public void deleteTransformTask(String id);
	
	public TransformTaskEntity getTransformTask(String id);
	
}
