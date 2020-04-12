package com.mtli.model.pojo;

import lombok.Data;
import lombok.ToString;

/**
 * @Description: 角色
 * @Author: Mt.Li
 */

@Data
@ToString

public class Role {

    private Integer id;//角色id
    private String name;//角色名

    public Role() {
    }
    public Role(String name) {
        this.name = name;
    }
}
