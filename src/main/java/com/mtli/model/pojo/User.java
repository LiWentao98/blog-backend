package com.mtli.model.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * @Description: 用户
 * @Author: Mt.Li
 */

@Data
@ToString
public class User implements Serializable {

    // 自动生成的serialVersionUID
    private static final long serialVersionUID = 7015283901517310682L;

    @ApiModelProperty(value = "用户id", dataType = "Integer")
    private Integer id;

    @ApiModelProperty(value = "用户名", dataType = "String")
    private String name;

    @ApiModelProperty(value = "密码", dataType = "String")
    private String password;

    @ApiModelProperty(value = "邮箱", dataType = "String")
    private String mail;

    @ApiModelProperty(value = "用户状态", dataType = "Integer")
    private Integer state;

    @ApiModelProperty(value = "打赏码路径", dataType = "String")
    private String reward;

    @ApiModelProperty(hidden = true)
    @JsonIgnore
    private List<Role> roles;

    private Login login;
}
