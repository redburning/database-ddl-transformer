package database.ddl.transformer.dto;

import java.util.HashMap;

public class Result extends HashMap<String, Object> {

	private static final long serialVersionUID = 1L;

	/** 状态码 */
    public static final String CODE = "code";

    /** 返回内容 */
    public static final String MSG = "msg";

    /** 数据对象 */
    public static final String DATA = "data";
    
    public Result() {}
	
    public Result(int code) {
    	super.put(CODE, 200);
    }
    
    public Result(int code, Object data) {
    	super.put(CODE, code);
    	super.put(DATA, data);
    }
    
    public Result(int code, String msg) {
    	super.put(CODE, code);
    	super.put(MSG, msg);
    }
    
    public static Result success() {
    	return new Result(200);
    }
    
    public static Result success(Object data) {
    	return new Result(200, data);
    }
    
    public static Result error(String msg) {
    	return new Result(500, msg);
    }
    
}
