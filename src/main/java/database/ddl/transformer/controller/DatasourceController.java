package database.ddl.transformer.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import database.ddl.transformer.dto.Result;
import database.ddl.transformer.entity.DatasourceEntity;
import database.ddl.transformer.service.IDatasourceService;

@RestController
@RequestMapping("/datasource")
public class DatasourceController {

	@Autowired
	private IDatasourceService datasourceService;
	
	@GetMapping("/list")
	public Result listDatasources() {
		try {
			List<DatasourceEntity> datasourceList = datasourceService.listDatasources();
			return Result.success(datasourceList);
		} catch (Exception e) {
			e.printStackTrace();
			return Result.error(e.getMessage());
		}
	}
	
	@PostMapping("/test")
	public Result testDatasource(@RequestBody DatasourceEntity datasource) {
		try {
			// 测试连接
			boolean test = datasourceService.testDatasource(datasource);
			if (test) {
				return Result.success();
			} else {
				return Result.error("未获取到可用连接");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Result.error(e.getMessage());
		}
	}
	
	@PostMapping
	public Result saveDatasource(@RequestBody DatasourceEntity datasource) {
		try {
			DatasourceEntity savedDatasource = datasourceService.saveDatasource(datasource);
			return Result.success(savedDatasource);
		} catch (Exception e) {
			e.printStackTrace();
			return Result.error(e.getMessage());
		}
	}
	
	@DeleteMapping("/{id}")
	public Result deleteDatasource(@PathVariable("id") String id) {
		try {
			datasourceService.deleteDatasource(id);
			return Result.success();
		} catch (Exception e) {
			e.printStackTrace();
			return Result.error(e.getMessage());
		}
	}
	
	@GetMapping("/{id}")
	public Result getDatasource(@PathVariable("id") String id) {
		try {
			DatasourceEntity datasourceEntity = datasourceService.getDatasource(id);
			return Result.success(datasourceEntity);
		} catch (Exception e) {
			e.printStackTrace();
			return Result.error(e.getMessage());
		}
	}
	
	@GetMapping("/{id}/databases")
	public Result listDatabases(@PathVariable("id") String id) {
		try {
			List<String> databaseList = datasourceService.listDatabases(id);
			return Result.success(databaseList);
		} catch (Exception e) {
			e.printStackTrace();
			return Result.error(e.getMessage());
		}
	}
	
	@GetMapping("/{id}/{database}/tables")
	public Result listTables(@PathVariable("id") String id, @PathVariable("database") String database) {
		try {
			List<String> tableList = datasourceService.listTables(id, database);
			return Result.success(tableList);
		} catch (Exception e) {
			e.printStackTrace();
			return Result.error(e.getMessage());
		}
	}
	
}
