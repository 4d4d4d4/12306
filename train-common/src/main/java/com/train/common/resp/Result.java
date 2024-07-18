package com.train.common.resp;

import com.train.common.resp.enmus.ResultStatusEnum;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

// 结果返回类
@Data
public class Result {
    private Boolean success; // 请求是否成功

    private Integer code; // 响应状态码

    private String message; // 响应信息

    private Map<String,Object> data = new HashMap<String,Object>(); // 响应数据

    // 构造方法私有化
    private Result(){}

    // 成功
    public static Result ok(){
        Result result = new Result();
        result.setCode(ResultStatusEnum.CODE_200.getCode());
        result.setSuccess(true);
        result.setMessage(ResultStatusEnum.CODE_200.getDescription());
        return result;
    }

    //失败
    public static Result error(){
        Result result = new Result();
        result.setCode(ResultStatusEnum.CODE_500.getCode());
        result.setSuccess(false);
        result.setMessage(ResultStatusEnum.CODE_500.getDescription());
        return result;
    }

    //添加数据
    public Result data(Map<String,Object> map){
        this.setData(map);
        return this;
    }

    //添加状态
    public Result success(Boolean flag){
        this.setSuccess(flag);
        return this;
    }

    //添加状态码
    public Result code(Integer code){
        this.setCode(code);
        return this;
    }
    public Result data(String key, Object value){
        this.data.put(key,value);
        return this;
    }

    //添加返回信息
    public Result message(String message){
        this.setMessage(message);
        return this;
    }

}
