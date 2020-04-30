package com.mtli.service;

import com.mtli.dao.LoginDao;
import com.mtli.dao.UserDao;
import com.mtli.model.pojo.Login;
import com.mtli.model.pojo.User;
import com.mtli.utils.DateUtil;
import com.mtli.utils.RequestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;


@Service
public class LoginService {

    @Autowired
    private LoginDao loginDao;

    @Autowired
    private UserService userService;

    @Autowired
    private UserDao userDao;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private DateUtil dateUtil;

    @Autowired
    private RequestUtil requestUtil;

    /**
     * 保存登录信息
     * @Transactional(rollbackFor=Exception.class)，如果类加了这个注解，那么这个类里面的方法抛出异常，
     * 就会回滚，数据库里面的数据也会回滚.
     * 在@Transactional注解中如果不配置rollbackFor属性,那么事物只会在遇到RuntimeException的时候才会回滚,
     * 加上rollbackFor=Exception.class,可以让事物在遇到非运行时异常时也回滚
     * @param user
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveLoginInfo(User user) {

        user = userDao.findUserByName(user.getName());
        Login login = new Login();
        login.setUser(user);//绑定用户
        login.setIp(requestUtil.getIpAddress(request));//获取操作ip
        login.setTime(dateUtil.getCurrentDate());//操作时间
        if(userDao.findUserById(user.getId()) == null){
            loginDao.saveLogin(login);
        }else {
            loginDao.updateLogin(login);
        }
    }

}
