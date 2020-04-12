package com.mtli.model.pojo;

import lombok.Data;
import lombok.ToString;

/**
 * @Description:邀请码
 * @Author: Mt.Li
 */

@Data
@ToString
public class Code {

    private String id;
    private Integer state; // 状态 0 未使用 1已使用 2 已删除
    private User user;

}
