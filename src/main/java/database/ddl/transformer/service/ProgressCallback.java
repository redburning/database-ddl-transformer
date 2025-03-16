package database.ddl.transformer.service;

import java.io.IOException;

public interface ProgressCallback {

	/**
	 * 处理进行中
	 * 
	 * @param progress	进度：2/10
	 * @param message	信息
	 * @param data		当前结果片段
	 * @throws IOException
	 */
	void onProgress(float progress, String message, Object data) throws IOException;
	
	/**
	 * 任务完成
	 * 
	 * @param data		结果全集
	 * @throws IOException
	 */
	void onComplete(Object data) throws IOException;
	
	/**
	 * 遇到错误
	 * 
	 * @param index		错误subtask的index
	 * @param message	错误详细message
	 */
	void onError(int index, String message) throws IOException;
	
}
