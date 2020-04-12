package com.mtli.controller;


import com.mtli.service.AnnouncementService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description:
 * @Author: Mt.Li
 * @Create: 2020-04-12 14:01
 */

@Api(tags = "公告api", description = "公告api",basePath = "/announcement")
@RestController
@RequestMapping("/announcement")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    @PostMapping("/test_an")
    public String newAnnouncement(){
        String t = "这是测试1";
        String b = "这是测试1的内容";
        announcementService.saveAnnouncement(t,b);
        return "success";
    }
}
