package database.ddl.transformer.service;

import java.sql.SQLException;
import java.util.List;

import database.ddl.transformer.entity.DatasourceEntity;

public interface IDatasourceService {

	public List<DatasourceEntity> listDatasources();
	
	public DatasourceEntity saveDatasource(DatasourceEntity datasource);
	
	public void deleteDatasource(String id);
	
	public DatasourceEntity getDatasource(String id);
	
	public boolean testDatasource(DatasourceEntity datasource) throws SQLException;
	
	public List<String> listDatabases(String id) throws Exception;
	
	public List<String> listTables(String id, String database) throws Exception;
	
}
