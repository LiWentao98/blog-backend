package com.mtli.controller;

import com.mtli.model.entity.Result;
import com.mtli.model.entity.StatusCode;
import com.mtli.model.pojo.User;
import com.mtli.service.LoginService;
import com.mtli.service.UserService;
import com.mtli.utils.FormatUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @Description:
 * @Author: Mt.Li
 * @Create: 2020-04-13 10:45
 */

@Api(tags = "用户api", description = "用户api", basePath = "/user")
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private FormatUtil formatUtil;

    @Autowired
    private LoginService loginService;

    /**
     * 登录返回token
     */
    @ApiOperation(value = "用户登录", notes = "用户名+密码 name+password 返回token")
    @PostMapping("/login")
    public Result login(User user) {
        if (!formatUtil.checkStringNull(user.getName(), user.getPassword())) {
            return Result.create(StatusCode.ERROR, "参数错误");
        }

        try {
            Map map = userService.login(user);
            loginService.saveLoginInfo(user);
            return Result.create(StatusCode.OK, "登录成功", map);
        } catch (UsernameNotFoundException unfe) {
            return Result.create(StatusCode.LOGINERROR, "登录失败，用户名或密码错误");
        } catch (RuntimeException re) {
            return Result.create(StatusCode.LOGINERROR, re.getMessage());
        }
    }
}
