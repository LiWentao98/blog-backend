package com.mtli.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description:
 * @Author: Mt.Li
 */

@Api(value = "mtli")
@RestController
public class TestController {

    @ApiOperation(value = "接口的功能介绍",notes = "提示接口使用者注意事项",httpMethod = "GET")
    @GetMapping("/test")
    public String testSwagger(String name){
        return "hello" + name;
    }
}
