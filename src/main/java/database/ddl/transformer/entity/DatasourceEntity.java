package database.ddl.transformer.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.AttributeConverter;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@Entity
@Table(name = "datasource")
@JsonIgnoreProperties(value={"hibernateLazyInitializer", "handler", "fieldHandler"})
public class DatasourceEntity {

	/** 数据源ID */
	@Id
	@GeneratedValue(generator="datasourceIdGenerator")
	@GenericGenerator(name="datasourceIdGenerator", strategy="uuid")
	private String id;

	/** 名称 */
	private String name;

	/** 描述 */
	private String description;

	/** 类型 */
	private String type;

	/** 连接信息 */
	@Convert(converter = ConnectionPropertyConverter.class)
	private ConnectionProperty property;

	/** 连通状态 */
	private String status;

	
	@Data
	public static class ConnectionProperty implements Serializable {
		
		private static final long serialVersionUID = 1L;
		
		private String user;
		private String passwd;
		private String url;
		private String database;
		private Map<String, String> additionalProperties = new HashMap<>();
		
		public String getAdditionalProperty(String key) {
			return additionalProperties.get(key);
		}
	}
}


class ConnectionPropertyConverter implements AttributeConverter<DatasourceEntity.ConnectionProperty, String> {

	@Override
	public String convertToDatabaseColumn(DatasourceEntity.ConnectionProperty attribute) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("user", attribute.getUser());
		jsonObject.put("passwd", attribute.getPasswd());
		jsonObject.put("url", attribute.getUrl());
		jsonObject.put("database", attribute.getDatabase());
		for (String key : attribute.getAdditionalProperties().keySet()) {
			jsonObject.put(key, attribute.getAdditionalProperties().get(key));
		}
		return jsonObject.toJSONString();
	}

	@Override
	public DatasourceEntity.ConnectionProperty convertToEntityAttribute(String str) {
		DatasourceEntity.ConnectionProperty connectionProperty = new DatasourceEntity.ConnectionProperty();
		JSONObject jsonObject = JSONObject.parseObject(str);
		for (String key : jsonObject.keySet()) {
			if (key.equals("user")) {
				connectionProperty.setUser(jsonObject.getString("user"));
			} else if (key.equals("passwd")) {
				connectionProperty.setPasswd(jsonObject.getString("passwd"));
			} else if (key.equals("url")) {
				connectionProperty.setUrl(jsonObject.getString("url"));
			} else if (key.equals("database")) {
				connectionProperty.setDatabase(jsonObject.getString("database"));
			} else {
				connectionProperty.getAdditionalProperties().put(key, jsonObject.getString(key));
			}
		}
		return connectionProperty;
	}
}