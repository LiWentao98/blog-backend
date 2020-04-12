package com.mtli.dao;

import com.mtli.model.pojo.Announcement;
import org.springframework.stereotype.Repository;

/**
 * @Description:
 * @Author: Mt.Li
 */
@Repository
public interface AnnouncementDao {

    /**
     * 保存公告
     */
    void saveAnnouncement(Announcement announcement);

    /**
     * 删除公告
     */
    void deleteAnnouncementById(Integer announcementId);

    /**
     * 置顶公告
     */
    void updateAnnouncementTop(Announcement announcement);

    /**
     * 查询公告数量
     */
    Long getAnnouncementCount();

    /**
     * 分页查询公告
     */
}
