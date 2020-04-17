package com.mtli.controller;

import com.mtli.config.MailConfig;
import com.mtli.model.entity.PageResult;
import com.mtli.model.entity.Result;
import com.mtli.model.entity.StatusCode;
import com.mtli.model.pojo.User;
import com.mtli.service.LoginService;
import com.mtli.service.UserService;
import com.mtli.utils.FormatUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @Description:
 * @Author: Mt.Li
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

    /**
     * 用户退出登录
     * 删除redis中的token
     *
     * @param
     * @return
     */
    @ApiOperation(value = "用户退出登录")
    @GetMapping("/logout")
    public Result logout() {

        userService.logout();
        return Result.create(StatusCode.OK, "退出成功");
    }

    /**
     * 用户注册
     *
     * @param user
     * @param mailCode   邮箱验证码
     * @param inviteCode 邀请码
     * @return
     */
    @ApiOperation(value = "用户注册", notes = "用户名+密码+邮箱+邮箱验证码+邀请码 name+password+mail+mailCode+inviteCode")
    @PostMapping("/register")
    public Result register(User user, String mailCode, String inviteCode) {
        if (!formatUtil.checkStringNull(
                user.getName(),
                mailCode,
                user.getPassword(),
                user.getMail(),
                inviteCode)) {
            return Result.create(StatusCode.ERROR, "注册失败，字段不完整");
        }
        try {
            userService.register(user, mailCode, inviteCode);
            return Result.create(StatusCode.OK, "注册成功");
        } catch (RuntimeException e) {
            return Result.create(StatusCode.ERROR, "注册失败，" + e.getMessage());
        }
    }

    /**
     * 发送邮件验证码
     *
     * @param mail
     * @return
     */
    @ApiOperation(value = "发送验证邮件", notes = "mail 冷却五分钟")
    @PostMapping("/sendMail")
    public Result sendMail(String mail) {
        //邮箱格式校验
        if (!(formatUtil.checkStringNull(mail)) || (!formatUtil.checkMail(mail))) {
            return Result.create(StatusCode.ERROR, "邮箱格式错误");
        }
        String redisMailCode = userService.getMailCodeFromRedis(mail);

        //此邮箱发送过验证码
        if (redisMailCode != null) {

            return Result.create(StatusCode.ERROR, MailConfig.EXPIRED_TIME + "分钟内不可重发验证码");
        } else {
            userService.sendMail(mail);

            return Result.create(StatusCode.OK, "发送成功");
        }
    }

    /**
     * 获取用户的打赏码
     *
     * @return
     */
    @ApiOperation(value = "获取用户的打赏码", notes = "获取用户的打赏码")
    @PreAuthorize("hasAuthority('USER')")
    @GetMapping("/getReward")
    public Result getUserReward() {
        return Result.create(StatusCode.OK, "查询成功", userService.findUserReward());
    }

    /**
     * 更新用户打赏码
     *
     * @return
     */
    @ApiOperation(value = "更新用户打赏码", notes = "更新用户打赏码")
    @PreAuthorize("hasAuthority('USER')")
    @PutMapping("/updateReward")
    public Result updateReward(String imgPath) {
        if (!formatUtil.checkStringNull(imgPath)) {
            return Result.create(StatusCode.ERROR, "格式错误");
        }
        userService.updateUserReward(imgPath);
        return Result.create(StatusCode.OK, "更新成功");
    }

    /**
     * 用户封禁或解禁
     *
     * @param id
     * @param state
     * @return
     */
    @ApiOperation(value = "用户封禁或解禁", notes = "用户id+状态 id+state")
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/ban/{id}/{state}")
    public Result banUser(@PathVariable Integer id, @PathVariable Integer state) {

        if (!formatUtil.checkObjectNull(id, state)) {
            return Result.create(StatusCode.ERROR, "参数错误");
        }


        if (state == 0) {
            userService.updateUserState(id, state);
            return Result.create(StatusCode.OK, "封禁成功");
        } else if (state == 1) {
            userService.updateUserState(id, state);
            return Result.create(StatusCode.OK, "解禁成功");
        } else {
            return Result.create(StatusCode.ERROR, "参数错误");
        }
    }

    /**
     * 重置密码
     *
     * @param mailCode
     * @param newPassword
     * @return
     */
    @ApiOperation(value = "重置密码", notes = "用户名+验证码+新密码")
//    @PreAuthorize("hasAuthority('USER')")
    @PostMapping("/forgetPassword")
    public Result forgetPassword(String userName, String mailCode, String newPassword) {

        if (!formatUtil.checkStringNull(userName, mailCode, newPassword)) {
            return Result.create(StatusCode.ERROR, "参数错误");
        }

        try {
            userService.forgetPassword(userName, mailCode, newPassword);
            return Result.create(StatusCode.OK, "重置成功");
        } catch (RuntimeException e) {
            return Result.create(StatusCode.ERROR, e.getMessage());
        }
    }

    /**
     * 修改密码
     *
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @param code        邮箱验证码
     * @return
     */
    @ApiOperation(value = "用户修改密码", notes = "旧密码+新密码+验证码")
    @PreAuthorize("hasAuthority('USER')")
    @PostMapping("/updatePassword")
    public Result updatePassword(String oldPassword, String newPassword, String code) {
        if (!formatUtil.checkStringNull(oldPassword, newPassword, code)) {
            return Result.create(StatusCode.ERROR, "参数错误");
        }
        try {
            userService.updateUserPassword(oldPassword, newPassword, code);
            return Result.create(StatusCode.OK, "修改密码成功");
        } catch (RuntimeException e) {
            return Result.create(StatusCode.ERROR, e.getMessage());
        }
    }

    /**
     * 获取用户绑定的邮箱
     *
     * @return
     */
    @ApiOperation(value = "获取用户绑定的邮箱", notes = "获取用户绑定的邮箱")
    @PreAuthorize("hasAuthority('USER')")
    @GetMapping("/mail")
    public Result getUserMail() {
        return Result.create(StatusCode.OK, "查询成功", userService.findUserMail());
    }

    /**
     * 改绑邮箱
     *
     * @param newMail     新邮箱
     * @param oldMailCode 旧邮箱验证码
     * @param newMailCode 新邮箱验证码
     * @return
     */
    @ApiOperation(value = "改绑邮箱", notes = "新邮箱+旧邮箱验证码+新邮箱验证码")
    @PreAuthorize("hasAuthority('USER')")
    @PostMapping("/updateMail")
    public Result updateMail(String newMail, String oldMailCode, String newMailCode) {
        if (!formatUtil.checkStringNull(newMail, oldMailCode, newMailCode)) {
            return Result.create(StatusCode.ERROR, "参数错误");
        }
        //检查邮箱格式
        if (!formatUtil.checkMail(newMail)) {
            return Result.create(StatusCode.ERROR, "参数错误");
        }
        try {
            userService.updateUserMail(newMail, oldMailCode, newMailCode);
            return Result.create(StatusCode.OK, "改绑成功");
        } catch (RuntimeException e) {
            return Result.create(StatusCode.ERROR, e.getMessage());
        }
    }

    /**
     * 分页查询用户
     *
     * @param page
     * @param showCount
     * @return
     */
    @ApiOperation(value = "分页查询用户", notes = "页码+显示数量")
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/{page}/{showCount}")
    public Result findUser(@PathVariable Integer page, @PathVariable Integer showCount) {
        if (!formatUtil.checkPositive(page, showCount)) {
            return Result.create(StatusCode.ERROR, "参数错误");
        }
        PageResult<User> pageResult =
                new PageResult<>(userService.getUserCount(), userService.findUser(page, showCount));
        return Result.create(StatusCode.OK, "查询成功", pageResult);
    }

    /**
     * 根据用户名分页搜索用户
     *
     * @param userName
     * @param page
     * @param showCount
     * @return
     */
    @ApiOperation(value = "根据用户名分页搜索用户", notes = "页码+显示数量+搜索内容")
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/search/{page}/{showCount}")
    public Result searchUser(String userName, @PathVariable Integer page, @PathVariable Integer showCount) {
        if (!formatUtil.checkStringNull(userName)) {
            return Result.create(StatusCode.ERROR, "参数错误");
        }
        if (!formatUtil.checkPositive(page, showCount)) {
            return Result.create(StatusCode.ERROR, "参数错误");
        }
        PageResult<User> pageResult =
                new PageResult<>(userService.getUserCountByName(userName), userService.searchUserByName(userName, page, showCount));

        return Result.create(StatusCode.OK, "查询成功", pageResult);

    }
}

