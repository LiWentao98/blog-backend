package com.mtli.model.pojo;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Description:博客
 * @Author: Mt.Li
 */

@Data
@ToString
public class Blog implements Serializable {

    // 自动生成serialVersionUID
    private static final long serialVersionUID = -4975736224186489579L;

    private Integer id; // 博客id
    private String title; // 博客标题
    private String body; // 博客内容
    private Integer discussCount; // 博客评论数
    private Integer blogViews; // 浏览数
    private Date time; // 发布时间
    private Integer state; // 博客状态——0删除 1正常
    private User user; // 所属用户
    private List<Tag> tags; //博客所属标签

}
