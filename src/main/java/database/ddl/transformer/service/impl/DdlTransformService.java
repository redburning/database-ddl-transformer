package database.ddl.transformer.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import database.ddl.transformer.engine.bean.CreateMode;
import database.ddl.transformer.engine.bean.DataBaseConnection;
import database.ddl.transformer.engine.bean.DataBaseType;
import database.ddl.transformer.engine.transform.Transformer;
import database.ddl.transformer.entity.DatasourceEntity;
import database.ddl.transformer.service.IDdlTransformService;
import database.ddl.transformer.service.ProgressCallback;

@Service
public class DdlTransformService implements IDdlTransformService {

	@Override
	public void validateTask(DatasourceEntity sourceDatasource, DatasourceEntity targetDatasource) throws Exception {
		new Transformer(buildDataBaseConnection(sourceDatasource), DataBaseType.of(targetDatasource.getType()));
	}
	
	@Override
	public Map<String, String> transformDDLAsync(ProgressCallback progressCallback, 
			DatasourceEntity sourceDatasource, DatasourceEntity targetDatasource, 
			String sourceDatabase, String targetDatabase, List<String> sourceTables, List<CreateMode> createModes)
			throws Exception {
		Transformer transformer = new Transformer(buildDataBaseConnection(sourceDatasource),
				DataBaseType.of(targetDatasource.getType()));
		Map<String, String> result = new HashMap<>();
		for (int i = 0; i < sourceTables.size(); i++) {
			try {
				Map<String, String> current = transformer.transformCreateTableDDL(sourceDatabase, sourceTables.get(i),
						targetDatabase, createModes.get(i));
				result.putAll(current);
				progressCallback.onProgress(Math.round((float) (i + 1) * 100 / sourceTables.size()),
						String.format("%d/%d", i + 1, sourceTables.size()), current);
			} catch (Exception e) {
				progressCallback.onError(i + 1, e.getMessage());
				e.printStackTrace();
			}
		}
		progressCallback.onComplete(result);
		return result;
	}
	
	@Override
	public Map<String, String> transformDDL(DatasourceEntity sourceDatasource, DatasourceEntity targetDatasource,
			String sourceDatabase, String targetDatabase, List<String> sourceTables, List<CreateMode> createModes) throws Exception {
		Transformer transformer = new Transformer(buildDataBaseConnection(sourceDatasource),
				DataBaseType.of(targetDatasource.getType()));
		Map<String, String> result = new HashMap<>();
		for (int i = 0; i < sourceTables.size(); i++) {
			String table = sourceTables.get(i);
			CreateMode mode = createModes.get(i);
			Map<String, String> current = transformer.transformCreateTableDDL(sourceDatabase, table, targetDatabase, mode);
			result.putAll(current);
		}
		return result;
	}

	private DataBaseConnection buildDataBaseConnection(DatasourceEntity datasource) {
		return DataBaseConnection.builder()
				.baseUrl(datasource.getProperty().getUrl())
				.user(datasource.getProperty().getUser())
				.password(datasource.getProperty().getPasswd())
				.additionalProperties(datasource.getProperty().getAdditionalProperties())
				.build();
	}
}
