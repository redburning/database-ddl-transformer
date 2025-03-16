package database.ddl.transformer.service.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import database.ddl.transformer.engine.bean.DataBaseConnection;
import database.ddl.transformer.engine.metadata.BaseMetaDataFetcher;
import database.ddl.transformer.engine.metadata.MetaDataFetcherFactory;
import database.ddl.transformer.entity.DatasourceEntity;
import database.ddl.transformer.repository.DatasourceRepository;
import database.ddl.transformer.service.IDatasourceService;

@Service
public class DatasourceServiceImpl implements IDatasourceService {

	@Autowired
	private DatasourceRepository datasourceRepository;
	
	@Override
	public List<DatasourceEntity> listDatasources() {
		return datasourceRepository.findAll();
	}

	@Override
	public DatasourceEntity saveDatasource(DatasourceEntity datasource) {
		return datasourceRepository.save(datasource);
	}

	@Override
	public void deleteDatasource(String id) {
		datasourceRepository.deleteById(id);
	}

	@Override
	public DatasourceEntity getDatasource(String id) {
		Optional<DatasourceEntity> datasource = datasourceRepository.findById(id);
		if (datasource.isPresent()) {
			return datasource.get();
		}
		return null;
	}

	@Override
	public boolean testDatasource(DatasourceEntity datasource) throws SQLException {
		String url = datasource.getProperty().getUrl();
		String user = datasource.getProperty().getUser();
		String passwd = datasource.getProperty().getPasswd();
		Connection connection = DriverManager.getConnection(url, user, passwd);
		return connection != null;
	}
	
	@Override
	public List<String> listDatabases(String id) throws Exception {
		DatasourceEntity datasourceEntity = datasourceRepository.findById(id).get();
		String jdbcUrl = datasourceEntity.getProperty().getUrl();
		String user = datasourceEntity.getProperty().getUser();
		String password = datasourceEntity.getProperty().getPasswd();
		Map<String, String> additionalProperties = datasourceEntity.getProperty().getAdditionalProperties();
		BaseMetaDataFetcher metaDataFetcher = MetaDataFetcherFactory.getMetaDataFetcher(
				DataBaseConnection.builder().baseUrl(jdbcUrl).user(user).password(password)
					.additionalProperties(additionalProperties)
					.build()
			);
		return metaDataFetcher.getDataBases();
	}

	@Override
	public List<String> listTables(String id, String database) throws Exception {
		DatasourceEntity datasourceEntity = datasourceRepository.findById(id).get();
		String jdbcUrl = datasourceEntity.getProperty().getUrl();
		String user = datasourceEntity.getProperty().getUser();
		String password = datasourceEntity.getProperty().getPasswd();
		Map<String, String> additionalProperties = datasourceEntity.getProperty().getAdditionalProperties();
		BaseMetaDataFetcher metaDataFetcher = MetaDataFetcherFactory.getMetaDataFetcher(
				DataBaseConnection.builder().baseUrl(jdbcUrl).user(user).password(password)
					.additionalProperties(additionalProperties)
					.build()
			);
		return metaDataFetcher.getTables(database);
	}

}
