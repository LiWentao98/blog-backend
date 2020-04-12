package com.mtli.service;

import com.mtli.dao.CodeDao;
import com.mtli.model.pojo.Code;
import com.mtli.utils.UUIDUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description:
 * @Author: Mt.Li
 */

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
        code.setId(uuidUtil.generateUUID().toUpperCase()); // 转换为大些
        codeDao.saveCode(code);
        return code;
    }
}
