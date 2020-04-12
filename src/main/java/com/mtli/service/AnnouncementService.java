package com.mtli.service;

import com.mtli.dao.AnnouncementDao;
import com.mtli.model.pojo.Announcement;
import com.mtli.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description:
 * @Author: Mt.Li
 */
@Service
public class AnnouncementService {

    @Autowired
    private AnnouncementDao announcementDao;

    @Autowired
    DateUtil dateUtil;

    public void saveAnnouncement(String announcementTitle, String announcementBody) {
        announcementBody = announcementBody.replaceAll("\n", "<br/>");
        Announcement announcement = new Announcement();
        announcement.setTitle(announcementTitle);
        announcement.setBody(announcementBody);
        announcement.setTime(dateUtil.getCurrentDate());
        announcement.setTop(1);
        announcementDao.saveAnnouncement(announcement);
    }
}
