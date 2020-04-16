package com.mtli.dao;

import com.mtli.model.pojo.Announcement;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

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
     *
     * @param start
     * @param showCount
     * @return
     */
    List<Announcement> findAnnouncement(@Param("start") Integer start, @Param("showCount") Integer showCount);
}
