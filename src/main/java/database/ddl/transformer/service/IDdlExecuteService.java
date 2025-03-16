package database.ddl.transformer.service;

import java.util.List;

import database.ddl.transformer.entity.DatasourceEntity;

public interface IDdlExecuteService {

	/**
	 * 执行ddl
	 * 
	 * @param progressCallback	执行进度回调
	 * @param targetDatasource	目标端数据源
	 * @param targetDatabase	目标端数据库
	 * @param ddlList			待执行的ddl list
	 * @throws Exception
	 */
	void executeDDL(ProgressCallback progressCallback, DatasourceEntity targetDatasource,
			List<String> ddlList) throws Exception;
	
	/**
	 * 同步执行ddl
	 * 
	 * @param targetDatasource
	 * @param ddlList
	 * @throws Exception
	 */
	void executeDDL(DatasourceEntity targetDatasource, List<String> ddlList) throws Exception;
	
}
