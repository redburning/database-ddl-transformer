package database.ddl.transformer.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.alibaba.fastjson.JSONObject;

import database.ddl.transformer.dto.Result;
import database.ddl.transformer.dto.TransformTaskDto;
import database.ddl.transformer.entity.DatasourceEntity;
import database.ddl.transformer.entity.TransformTaskEntity;
import database.ddl.transformer.service.IDatasourceService;
import database.ddl.transformer.service.IDdlExecuteService;
import database.ddl.transformer.service.IDdlTransformService;
import database.ddl.transformer.service.ITransformTaskService;
import database.ddl.transformer.service.ProgressCallback;

@RestController
@RequestMapping("/task")
public class TransformTaskController {

	@Autowired
	private IDdlTransformService ddlTransformService;
	
	@Autowired
	private IDdlExecuteService ddlExecuteService;
	
	@Autowired
	private IDatasourceService datasourceService;
	
	@Autowired
	private ITransformTaskService transformTaskService;
	
	private AtomicInteger taskCounter = new AtomicInteger(0);
	
	private TaskContext taskContext = new TaskContext();
	
	@PutMapping("/save")
	public Result saveTask(@RequestBody TransformTaskEntity taskEntity) {
		try {
			TransformTaskEntity savedTaskEntity = transformTaskService.saveTransformTask(taskEntity);
			return Result.success(savedTaskEntity);
		} catch (Exception e) {
			e.printStackTrace();
			return Result.error(e.getMessage());
		}
	}
	
	@GetMapping("/list")
	public Result listTasks() {
		try {
			List<TransformTaskDto> taskDtoList = new ArrayList<>();
			List<TransformTaskEntity> taskList = transformTaskService.listTransformTasks();
			for (TransformTaskEntity task : taskList) {
				// 获取task关联的source datasource
				String sourceDatasourceId = task.getSourceDatasourceId();
				DatasourceEntity sourceDatasource = datasourceService.getDatasource(sourceDatasourceId);
				
				// 获取task关联的target datasource
				String targetDatasourceId = task.getTargetDatasourceId();
				DatasourceEntity targetDatasource = datasourceService.getDatasource(targetDatasourceId);
				
				TransformTaskDto taskDto = new TransformTaskDto(task);
				taskDto.setSourceDatasource(sourceDatasource);
				taskDto.setTargetDatasource(targetDatasource);
				taskDtoList.add(taskDto);
			}
			return Result.success(taskDtoList);
		} catch (Exception e) {
			e.printStackTrace();
			return Result.error(e.getMessage());
		}
	}
	
	@DeleteMapping("/{id}")
	public Result deleteTask(@PathVariable("id") String id) {
		try {
			transformTaskService.deleteTransformTask(id);
			return Result.success();
		} catch (Exception e) {
			e.printStackTrace();
			return Result.error(e.getMessage());
		}
	}
	
	@GetMapping("/{id}")
	public Result getTask(@PathVariable("id") String id) {
		try {
			TransformTaskEntity task = transformTaskService.getTransformTask(id);
			
			// 获取task关联的source datasource
			String sourceDatasourceId = task.getSourceDatasourceId();
			DatasourceEntity sourceDatasource = datasourceService.getDatasource(sourceDatasourceId);
			
			// 获取task关联的target datasource
			String targetDatasourceId = task.getTargetDatasourceId();
			DatasourceEntity targetDatasource = datasourceService.getDatasource(targetDatasourceId);
			
			TransformTaskDto taskDto = new TransformTaskDto(task);
			taskDto.setSourceDatasource(sourceDatasource);
			taskDto.setTargetDatasource(targetDatasource);
			
			return Result.success(taskDto);
		} catch (Exception e) {
			e.printStackTrace();
			return Result.error(e.getMessage());
		}
	}
	
	/**
	 * 创建ddl转换任务
	 * 
	 * @param taskEntity
	 * @return
	 */
	@PostMapping("/ddltransfer")
	public Result transformDdl(@RequestBody TransformTaskEntity taskEntity) {
		int taskId = taskCounter.incrementAndGet();
		SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
		
		ProgressCallback progressCallback = new ProgressCallback() {
			
			@Override
			public void onProgress(float progress, String message, Object data) throws IOException {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("progress", progress);
				jsonObject.put("message", message);
				jsonObject.put("data", data);
				emitter.send(SseEmitter.event().name("progress").data(jsonObject.toJSONString()));
			}
			
			@Override
			public void onComplete(Object data) throws IOException {
				emitter.send(SseEmitter.event().name("result").data(data));
			}

			@Override
			public void onError(int index, String message) throws IOException {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("index", index);
				jsonObject.put("message", message);
				emitter.send(SseEmitter.event().name("error").data(jsonObject.toJSONString()));
			}
		};
		
		// validate task
		String sourceDatasourceId = taskEntity.getSourceDatasourceId();
		String targetDatasourceId = taskEntity.getTargetDatasourceId();
		DatasourceEntity sourceDatasource = datasourceService.getDatasource(sourceDatasourceId);
		DatasourceEntity targetDatasource = datasourceService.getDatasource(targetDatasourceId);
		try {
			ddlTransformService.validateTask(sourceDatasource, targetDatasource);
		} catch (Exception e) {
			e.printStackTrace();
			return Result.error(e.getMessage());
		}
		
		CompletableFuture.runAsync(() -> {
			try {
				ddlTransformService.transformDDLAsync(progressCallback, 
						sourceDatasource, 
						targetDatasource,
						taskEntity.getSourceDatabase(), 
						taskEntity.getTargetDatabase(),
						taskEntity.getSourceTables(),
						taskEntity.getCreateModes());
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		
		taskContext.saveSseEmitter(taskId, emitter);
		return Result.success(taskId);
	}
	
	/**
	 * 查询ddl转换进度
	 * 
	 * @param taskId	任务id
	 * @return
	 */
	@GetMapping(value = "/ddltransfer-progress/{taskId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter getTransformProgress(@PathVariable("taskId") int taskId) {
		SseEmitter emitter = taskContext.getSseEmitter(taskId);
		if (emitter == null) {
	        emitter = new SseEmitter();
	        emitter.completeWithError(new RuntimeException("Task not found"));
	    }
		return emitter;
	}
	
	
	@PostMapping("/ddlexecute-async")
	public Result executeDdlAsync(@RequestBody TransformTaskEntity taskEntity) {
		int taskId = taskCounter.incrementAndGet();
		SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
		
		ProgressCallback progressCallback = new ProgressCallback() {
			
			@Override
			public void onProgress(float progress, String message, Object data) throws IOException {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("progress", progress);
				jsonObject.put("message", message);
				jsonObject.put("data", data);
				emitter.send(SseEmitter.event().name("progress").data(jsonObject.toJSONString()));
			}
			
			@Override
			public void onComplete(Object data) throws IOException {
				emitter.send(SseEmitter.event().name("result").data(data));
			}

			@Override
			public void onError(int index, String message) throws IOException {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("index", index);
				jsonObject.put("message", message);
				emitter.send(SseEmitter.event().name("error").data(jsonObject.toJSONString()));
			}
		};
		
		String targetDatasourceId = taskEntity.getTargetDatasourceId();
		DatasourceEntity targetDatasource = datasourceService.getDatasource(targetDatasourceId);
		List<String> ddlList = taskEntity.getSubtasks()
				.stream().map(t -> t.getSql())
				.collect(Collectors.toList());
		
		CompletableFuture.runAsync(() -> {
			try {
				ddlExecuteService.executeDDL(progressCallback, targetDatasource, ddlList);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		
		taskContext.saveSseEmitter(taskId, emitter);
		return Result.success(taskId);
	}
	
	
	/**
	 * 查询ddl执行进度
	 * 
	 * @param taskId	任务id
	 * @return
	 */
	@GetMapping(value = "/ddlexecute-async-progress/{taskId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter getDdlExecuteProgress(@PathVariable("taskId") int taskId) {
		SseEmitter emitter = taskContext.getSseEmitter(taskId);
		if (emitter == null) {
	        emitter = new SseEmitter();
	        emitter.completeWithError(new RuntimeException("Task not found"));
	    }
		return emitter;
	}
	
	@PostMapping("/ddlexecute")
	public Result executeDdl(@RequestBody TransformTaskEntity taskEntity) {
		try {
			String targetDatasourceId = taskEntity.getTargetDatasourceId();
			DatasourceEntity targetDatasource = datasourceService.getDatasource(targetDatasourceId);
			List<String> ddlList = taskEntity.getSubtasks()
					.stream().map(t -> t.getSql())
					.collect(Collectors.toList());
			ddlExecuteService.executeDDL(targetDatasource, ddlList);
			return Result.success();
		} catch (Exception e) {
			e.printStackTrace();
			return Result.error(e.getMessage());
		}
	}
	
}

class TaskContext {
	
	private Map<Integer, SseEmitter> sseEmitterMap = new ConcurrentHashMap<>();
	
	public TaskContext() {
		this.sseEmitterMap = new ConcurrentHashMap<>();
	}
	
	public void saveSseEmitter(int taskId, SseEmitter sseEmitter) {
		sseEmitterMap.put(taskId, sseEmitter);
	}
	
	public SseEmitter getSseEmitter(int taskId) {
		return sseEmitterMap.get(taskId);
	}
}
