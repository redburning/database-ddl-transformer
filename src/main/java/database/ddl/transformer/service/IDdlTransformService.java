package database.ddl.transformer.service;

import java.util.List;
import java.util.Map;

import database.ddl.transformer.engine.bean.CreateMode;
import database.ddl.transformer.entity.DatasourceEntity;

public interface IDdlTransformService {

	void validateTask(DatasourceEntity sourceDatasource, DatasourceEntity targetDatasource) throws Exception;
	
	Map<String, String> transformDDLAsync(ProgressCallback progressCallback, 
			DatasourceEntity sourceDatasource,
			DatasourceEntity targetDatasource, 
			String sourceDatabase, 
			String targetDatabase, 
			List<String> sourceTables,
			List<CreateMode> createModes)
			throws Exception;

	Map<String, String> transformDDL(DatasourceEntity sourceDatasource, 
			DatasourceEntity targetDatasource,
			String sourceDatabase, 
			String targetDatabase, 
			List<String> sourceTables,
			List<CreateMode> createModes) throws Exception;
	
}
