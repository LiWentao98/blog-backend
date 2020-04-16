package com.mtli.service;

import com.mtli.dao.CodeDao;
import com.mtli.model.pojo.Code;
import com.mtli.utils.UUIDUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CodeService {

    @Autowired
    private CodeDao codeDao;

    @Autowired
    private UUIDUtil uuidUtil;

    /**
     * 生成激活码
     *
     * @return
     */
    public Code generateCode() {
        Code code = new Code();
        code.setState(0);
        // 转换成大写
        code.setId(uuidUtil.generateUUID().toUpperCase());
        codeDao.saveCode(code);
        return code;
    }


    /**
     * 获取激活码记录条数
     *
     * @return
     */
    public Long getCodeCount() {
        return codeDao.getCodeCount();
    }

    public List<Code> findCode(Integer page, Integer showCount) {
        return codeDao.findCode((page - 1) * showCount, showCount);
    }
}
