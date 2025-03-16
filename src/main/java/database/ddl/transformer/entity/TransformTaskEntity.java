package database.ddl.transformer.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.AttributeConverter;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import database.ddl.transformer.engine.bean.CreateMode;
import lombok.Data;

@Data
@Entity
@Table(name = "transform_task")
@JsonIgnoreProperties(value={"hibernateLazyInitializer", "handler", "fieldHandler"})
public class TransformTaskEntity {
	
	@Id
	@GeneratedValue(generator="transformTaskIdGenerator")
	@GenericGenerator(name="transformTaskIdGenerator", strategy="uuid")
	private String id;
	
	private String name;

	@Column(name = "source_datasource_id")
	private String sourceDatasourceId;
	
	@Column(name = "target_datasource_id")
	private String targetDatasourceId;
	
	@Column(name = "source_database")
	private String sourceDatabase;
	
	@Column(name = "target_database")
	private String targetDatabase;
	
	@Convert(converter = SubtasksConverter.class)
	private List<SubTask> subtasks;
	
	public List<String> getSourceTables() {
		List<String> tableList = new ArrayList<>();
		for (SubTask subtask : subtasks) {
			tableList.add(subtask.getSourceTable());
		}
		return tableList;
	}
	
	public List<CreateMode> getCreateModes() {
		List<CreateMode> createModeList = new ArrayList<>();
		for (SubTask subtask : subtasks) {
			createModeList.add(CreateMode.of(subtask.getCreateMode()));
		}
		return createModeList;
	}

	@Data
	public static class SubTask implements Serializable {
		
		private static final long serialVersionUID = 1L;
		
		private String sourceDatabase;
		private String sourceTable;
		private String targetDatabase;
		private String targetTable;
		// 创建模式
		private String createMode;
		// SQL
		private String sql;
		// DDL转换状态
		private String transformStatus;
		// DDL执行状态
		private String executeStatus;
		// 转换错误/执行错误
		private String errorMsg;
		
		@Override
	    public String toString() {
	        return JSON.toJSONString(this);
		}
	}
}

class SubtasksConverter implements AttributeConverter<List<TransformTaskEntity.SubTask>, String> {

	@Override
	public String convertToDatabaseColumn(List<TransformTaskEntity.SubTask> subtasks) {
		JSONArray jsonArray = new JSONArray();
		for (int i = 0; i < subtasks.size(); i++) {
			TransformTaskEntity.SubTask subtask = subtasks.get(i);
			JSONObject jsonObject = JSONObject.parseObject(subtask.toString());
			jsonArray.add(jsonObject);
		}
		return jsonArray.toJSONString();
	}

	@Override
	public List<TransformTaskEntity.SubTask> convertToEntityAttribute(String str) {
		List<TransformTaskEntity.SubTask> subtasks = new ArrayList<>();
		JSONArray jsonArray = JSONArray.parseArray(str);
		for (int i = 0; i < jsonArray.size(); i++) {
			JSONObject jsonObject = jsonArray.getJSONObject(i);
			TransformTaskEntity.SubTask subtask = JSONObject.parseObject(jsonObject.toJSONString(),
					TransformTaskEntity.SubTask.class);
			subtasks.add(subtask);
		}
		return subtasks;
	}
}