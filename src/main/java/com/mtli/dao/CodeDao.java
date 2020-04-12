package com.mtli.dao;


import com.mtli.model.pojo.Code;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CodeDao {

    /**
     * 根据id查询 激活码
     * @param inviteCode
     * @return
     */
    Code findCodeById(String inviteCode);

    /**
     * 根据 id 更新激活码状态
     *
     * @param code
     */
    void updateCode(Code code);

    /**
     * 保存激活码
     *
     * @param code
     */
    void saveCode(Code code);

    /**
     * 获取记录条数
     *
     * @return
     */
    Long getCodeCount();

    /**分页查询激活码
     * @param start
     * @param showCount
     * @return
     */
    List<Code> findCode(@Param("start") Integer start, @Param("showCount") Integer showCount);
}
