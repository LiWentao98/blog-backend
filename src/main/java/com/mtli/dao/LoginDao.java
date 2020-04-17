package com.mtli.dao;

import com.mtli.model.pojo.Login;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Repository
public interface LoginDao {

    /**
     * 根据用户id删除登录记录
     * @param id
     */
    void deleteLoginByUserId(Integer id);


    /**
     * 保存登录信息
     */
    void saveLogin(Login login);


    /**
     * 更新登录信息
     * @param login
     */
    void updateLogin(Login login);
}
