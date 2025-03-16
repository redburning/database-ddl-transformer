package database.ddl.transformer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import database.ddl.transformer.entity.TransformTaskEntity;

public interface TransformTaskRepository
		extends JpaRepository<TransformTaskEntity, String>, JpaSpecificationExecutor<TransformTaskEntity> {

}
