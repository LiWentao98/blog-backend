package com.mtli.model.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @Description: 标签
 * @Author: Mt.Li
 */

@Data
@ToString
public class Tag implements Serializable {

    private static final long serialVersionUID = 2922281045375124885L;
    private Integer id;//id
    private String name;//标签名

    @JsonIgnore
    private User user;//用户

}
