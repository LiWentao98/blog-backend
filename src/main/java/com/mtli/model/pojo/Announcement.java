package com.mtli.model.pojo;

import lombok.Data;
import lombok.ToString;

import java.util.Date;

/**
 * @Description: 公告
 * @Author: Mt.Li
 */

@Data
@ToString
public class Announcement {

    private Integer id; //公告id
    private String title; // 公告标题
    private String body; // 公告内容
    private Integer top; // 是否置顶，0置顶 1未置顶
    private Date time; // 发布时间
}
