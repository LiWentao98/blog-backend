package com.mtli.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

/**
 * @Description: 返回结果实体类
 * @Author: Mt.Li
 */

@Api("响应实体")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@ToString
public class Result {

    @ApiModelProperty(value = "返回状态码", dataType = "Integer")
    private Integer code; // 返回状态码

    @ApiModelProperty(value = "返回信息" , dataType = "String")
    private String message; // 返回信息

    @ApiModelProperty(value = "返回数据", dataType = "Object")
    private Object data; // 返回数据

    private Result(){

    }

    public Result(Integer code, String message) {
        super();
        this.code = code;
        this.message = message;
    }

    public Result(Integer code, String message, Object data) {
        super();
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static Result create(Integer code, String message){
        return new Result(code,message);
    }

    public static Result create(Integer code, String message, Object data){
        return new Result(code,message,data);
    }

}
