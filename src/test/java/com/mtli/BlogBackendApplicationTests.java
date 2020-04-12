package com.mtli;

import com.mtli.dao.AnnouncementDao;
import com.mtli.model.pojo.Announcement;

import com.mtli.service.AnnouncementService;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
class BlogBackendApplicationTests {

    @Autowired
    AnnouncementService announcementService;

    @Autowired
    private AnnouncementDao announcementDao;

    @Test
    void test01(){
        announcementService.saveAnnouncement("测试一","这是测试一内容");

    }
    @Test
    void contextLoads() {
    }

}
