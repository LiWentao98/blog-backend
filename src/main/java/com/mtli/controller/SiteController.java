package com.mtli.controller;

import com.mtli.config.SiteIntroductionConfig;
import com.mtli.model.entity.Result;
import com.mtli.model.entity.StatusCode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "站点介绍api", description = "站点api", basePath = "/site")
@RestController
@RequestMapping("/site")
public class SiteController {

    @Autowired
    private SiteIntroductionConfig siteIntroductionConfig;


    @ApiOperation(value = "站点介绍", notes = "站点介绍")
    @GetMapping
    public Result getIntroduction() {

        return Result.create(StatusCode.OK, "获取成功", siteIntroductionConfig.getIntroduction());
    }


}
