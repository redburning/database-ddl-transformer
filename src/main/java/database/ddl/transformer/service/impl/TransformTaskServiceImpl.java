package database.ddl.transformer.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import database.ddl.transformer.entity.TransformTaskEntity;
import database.ddl.transformer.repository.TransformTaskRepository;
import database.ddl.transformer.service.ITransformTaskService;

@Service
public class TransformTaskServiceImpl implements ITransformTaskService {

	@Autowired
	private TransformTaskRepository transformTaskRepository;
	
	@Override
	public List<TransformTaskEntity> listTransformTasks() {
		return transformTaskRepository.findAll();
	}

	@Override
	public TransformTaskEntity saveTransformTask(TransformTaskEntity taskEntity) {
		return transformTaskRepository.save(taskEntity);
	}

	@Override
	public void deleteTransformTask(String id) {
		transformTaskRepository.deleteById(id);
	}

	@Override
	public TransformTaskEntity getTransformTask(String id) {
		Optional<TransformTaskEntity> task = transformTaskRepository.findById(id);
		if (task.isPresent()) {
			return task.get();
		}
		return null;
	}

}
