package com.mtli.model.pojo;

import lombok.Data;
import lombok.ToString;

import java.util.Date;
import java.util.List;

/**
 * @Description:评论
 * @Author: Mt.Li
 */

@Data
@ToString
public class Discuss {

    private Integer id;
    private String body; // 评论的内容
    private Date time; // 评论的时间
    private User user; // 评论的用户
    private Blog blog; // 评论的博文
    private List<Reply> replyList; // 回复
}
