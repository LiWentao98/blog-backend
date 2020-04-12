package com.mtli.model.pojo;

import lombok.Data;
import lombok.ToString;

import java.util.Date;

/**
 * @Description: 留言
 * @Author: Mt.Li
 */

@Data
@ToString
public class Message {

    private Integer id;
    private String name; // 游客显示为ip地址
    private String body; // 留言内容
    private Date time; // 留言时间
}
