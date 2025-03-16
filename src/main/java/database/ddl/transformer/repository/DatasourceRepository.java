package database.ddl.transformer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import database.ddl.transformer.entity.DatasourceEntity;

public interface DatasourceRepository
		extends JpaRepository<DatasourceEntity, String>, JpaSpecificationExecutor<DatasourceEntity> {

}
