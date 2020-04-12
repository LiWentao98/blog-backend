package com.mtli.model.pojo;

import lombok.Data;
import lombok.ToString;

import java.util.Date;

/**
 * @Description: 回复
 * @Author: Mt.Li
 */

@Data
@ToString
public class Reply {

    private Integer id;
    private String body; // 回复内容
    private Date time; //
    private User user; //
    private Discuss discuss; //
    private Reply reply; //
}
