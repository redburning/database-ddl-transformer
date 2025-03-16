package database.ddl.transformer.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import database.ddl.transformer.engine.bean.DataBaseConnection;
import database.ddl.transformer.engine.transform.DdlExecutor;
import database.ddl.transformer.entity.DatasourceEntity;
import database.ddl.transformer.service.IDdlExecuteService;
import database.ddl.transformer.service.ProgressCallback;

@Service
public class DdlExecuteService implements IDdlExecuteService {

	@Override
	public void executeDDL(ProgressCallback progressCallback, DatasourceEntity targetDatasource,
			List<String> ddlList) throws IOException {
		List<String> result = new ArrayList<>();
		DdlExecutor ddlExecutor = new DdlExecutor(buildDataBaseConnection(targetDatasource));
		for (int i = 0; i < ddlList.size(); i++) {
			try {
				ddlExecutor.executeSql(ddlList.get(i));
				result.add(null);
				progressCallback.onProgress(Math.round((float) (i + 1) * 100 / ddlList.size()),
						String.format("%d/%d", i + 1, ddlList.size()), null);
			} catch (Exception e) {
				e.printStackTrace();
				progressCallback.onError(i + 1, e.getMessage());
				result.add(e.getMessage());
			}
		}
		progressCallback.onComplete(result);
	}

	@Override
	public void executeDDL(DatasourceEntity targetDatasource, List<String> ddlList) throws Exception {
		DdlExecutor ddlExecutor = new DdlExecutor(buildDataBaseConnection(targetDatasource));
		for (int i = 0; i < ddlList.size(); i++) {
			ddlExecutor.executeSql(ddlList.get(i));
		}
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
