package com.mtli.service;

import com.mtli.config.JwtConfig;
import com.mtli.config.MailConfig;
import com.mtli.config.RabbitMqConfig;
import com.mtli.dao.CodeDao;
import com.mtli.dao.RoleDao;
import com.mtli.dao.UserDao;
import com.mtli.model.pojo.Code;
import com.mtli.model.pojo.Role;
import com.mtli.model.pojo.User;
import com.mtli.utils.JwtTokenUtil;
import com.mtli.utils.RandomUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @Description:
 * @Author: Mt.Li
 */

@Service
public class UserService implements UserDetailsService {

    @Autowired
    UserDao userDao;

    @Autowired
    private CodeDao codeDao;

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private RoleService roleService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private JwtConfig jwtConfig;

    @Autowired
    private RandomUtil randomUtil;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 用户登录
     * @param user
     * @return
     * @throws UsernameNotFoundException
     * @throws RuntimeException
     */
    public Map login(User user) throws UsernameNotFoundException,RuntimeException{
        User dbUser = this.findUserByName(user.getName());
        // 用户不存在 或者 密码错误
        if (null == dbUser || !encoder.matches(user.getPassword(), dbUser.getPassword())) {
            throw new UsernameNotFoundException("用户名或密码错误");
        }

        // 用户已被封禁
        if (0 == dbUser.getState()) {
            throw new RuntimeException("你已被封禁");
        }

        // 用户名 密码匹配，获取用户详细信息（包含角色Role）
        final UserDetails userDetails = this.loadUserByUsername(user.getName());

        // 根据用户详细信息生成token
        final String token = jwtTokenUtil.generateToken(userDetails);
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        List<String> roles = new ArrayList<>();
        for (GrantedAuthority authority : authorities) { // SimpleGrantedAuthority是GrantedAuthority实现类
            // GrantedAuthority包含类型为String的获取权限的getAuthority()方法
            // 提取角色并放入List中
            roles.add(authority.getAuthority());
        }

        Map<String, Object> map = new HashMap<>(3);

        map.put("token", jwtConfig.getPrefix() + token);
        map.put("name", user.getName());
        map.put("roles", roles);

        //将token存入redis(TOKEN_username, Bearer + token, jwt存放五天 过期时间) jwtConfig.time 单位[s]
        redisTemplate.opsForValue().
                set(JwtConfig.REDIS_TOKEN_KEY_PREFIX + user.getName(), jwtConfig.getPrefix() + token, jwtConfig.getTime(), TimeUnit.SECONDS);

        return map;

    }

    /**
     * 退出登录
     * 删除redis中的key
     */
    public void logout() {
        String username = jwtTokenUtil.getUsernameFromRequest(request);
        redisTemplate.delete(JwtConfig.REDIS_TOKEN_KEY_PREFIX + username);
    }

    /**
     * 注册
     * @param userToAdd
     * @param mailCode
     * @param inviteCode
     * @throws RuntimeException
     */
    @Transactional(rollbackFor = Exception.class)
    public void register(User userToAdd, String mailCode, String inviteCode) throws RuntimeException {

        //验证码无效 throw 异常
        if (!checkMailCode(userToAdd.getMail(), mailCode)) {
            throw new RuntimeException("验证码错误");
        }

        //有效
        //查询邀请码是否有效
        Code code = codeDao.findCodeById(inviteCode);

        if (code == null || code.getState() != 0) {
            //无效 throw 异常
            throw new RuntimeException("邀请码无效");
        }
        //有效 保存用户
        final String username = userToAdd.getName();
        if (userDao.findUserByName(username) != null) {
            throw new RuntimeException("用户名已存在");
        }

        if (userDao.findUserByMail(userToAdd.getMail()) != null) {
            throw new RuntimeException("邮箱已使用");
        }

        List<Role> roles = new ArrayList<>(1);
        roles.add(roleService.findRoleByName("USER"));
        userToAdd.setRoles(roles);//新注册用户赋予USER权限

        final String rawPassword = userToAdd.getPassword();
        userToAdd.setPassword(encoder.encode(rawPassword));//加密密码
        userToAdd.setState(1);//正常状态
        userDao.saveUser(userToAdd);//保存角色
        //保存该用户的所有角色
        for (Role role : roles) {
            roleDao.saveUserRoles(userToAdd.getId(), role.getId());
        }
        // 更新邀请码状态
        code.setUser(userToAdd);
        code.setState(1);
        codeDao.updateCode(code);
    }

    /**
     * 校验验证码是否正确
     *
     * @param mail
     * @param code
     * @return
     */
    public boolean checkMailCode(String mail, String code) {
        // 从缓存中获取验证码
        String mailCode = getMailCodeFromRedis(mail);

        if (code.equals(mailCode)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 将 邮件 和 验证码发送到消息队列
     *
     * @param mail
     */
    public void sendMail(String mail) {
        //貌似线程不安全 范围100000 - 999999
        Integer random = randomUtil.nextInt(100000, 999999);
        Map<String, String> map = new HashMap<>();
        String code = random.toString();
        map.put("mail", mail);
        map.put("code", code);

        //保存发送记录
        redisTemplate.opsForValue()
                .set(MailConfig.REDIS_MAIL_KEY_PREFIX + mail, code, MailConfig.EXPIRED_TIME, TimeUnit.MINUTES);

        // 转交给队列处理
        rabbitTemplate.convertAndSend(RabbitMqConfig.MAIL_QUEUE, map);

    }

    /**
     * 从redis中提取 验证码
     *
     * @param mail 邮箱
     * @return 验证码
     */
    public String getMailCodeFromRedis(String mail) {
        return redisTemplate.opsForValue().get(MailConfig.REDIS_MAIL_KEY_PREFIX + mail);
    }


    /**
     * 根据用户名查询用户
     *
     * @param name
     * @return
     */
    public User findUserByName(String name) {
        return userDao.findUserByName(name);
    }

    /**
     * 查询打赏码
     * @return
     */
//    public String findUserReward() {
//        User user = userDao.findUserByName(jwtTokenUtil.getUsernameFromRequest(request));
//        return user.getReward();
//    }

    /**
     * 根据用户名查询用户
     *
     * @param name
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {
        User user = userDao.findUserByName(name);
        // 定义权限列表，SimpleGrantedAuthority是GrantedAuthority实现类
        List<SimpleGrantedAuthority> authorities = new ArrayList<>(1);
        //用于添加用户的权限。将用户权限添加到authorities
        List<Role> roles = roleDao.findUserRoles(user.getId()); // 查询该用户的角色
        for (Role role : roles) {
            // 遍历角色权限，将其添加到权限列表
            authorities.add(new SimpleGrantedAuthority(role.getName()));
        }
        return new org.springframework.security.core.userdetails.User(user.getName(), "***********", authorities);
    }

    /**
     * 从token中提取信息
     *
     * @param authHeader
     * @return
     */
    public UserDetails loadUserByToken(String authHeader) {
        final String authToken = authHeader.substring(jwtConfig.getPrefix().length());//除去前缀，获取token

        String username = jwtTokenUtil.getUsernameFromToken(authToken);
        //token非法
        if (null == username) {
            return null;
        }

        String redisToken = redisTemplate.opsForValue().get(JwtConfig.REDIS_TOKEN_KEY_PREFIX + username);
        //从redis中取不到值 或 值 不匹配
        if (!authHeader.equals(redisToken)) {
            return null;
        }
        User user = new User();
        user.setName(username);

        List<String> roles = jwtTokenUtil.getRolesFromToken(authToken);

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority(role));
        }

        return new org.springframework.security.core.userdetails.User(user.getName(), "***********", authorities);
    }

    /**
     * 封禁或解禁用户
     *
     * @param id
     * @param state
     */
    public void updateUserState(Integer id, Integer state) {
        User user = new User();
        user.setId(id);
        user.setState(state);
        //更改用户状态
        userDao.updateUser(user);

        User userById = userDao.findUserById(id);

        //封禁 从redis中移除token
        if (0 == state) {
            redisTemplate.delete(JwtConfig.REDIS_TOKEN_KEY_PREFIX + userById.getName());
        }
    }

    /**
     * 修改用户密码
     *
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @param code        邮箱验证码
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateUserPassword(String oldPassword, String newPassword, String code) {
        // 校验原密码
        String name = jwtTokenUtil.getUsernameFromRequest(request);
        User user = new User();
        user.setName(name);

        user = userDao.findUserByName(user.getName()); //
        if (!encoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        //校验邮箱验证码
        if (!checkMailCode(user.getMail(), code)) {
            throw new RuntimeException("验证码无效");
        }
        //更新密码
        user.setPassword(encoder.encode(newPassword));
        userDao.updateUser(user);

    }

    /**
     * 重置密码
     *
     * @param userName
     * @param mailCode
     * @param newPassword
     */
    @Transactional(rollbackFor = Exception.class)
    public void forgetPassword(String userName, String mailCode, String newPassword) {
        User user = userDao.findUserByName(userName);
        if (user == null) {
            throw new RuntimeException("用户名不存在");
        }


        //与用户输入的邮箱验证码进行匹配
        if (!checkMailCode(user.getMail(), mailCode)) {
            throw new RuntimeException("无效验证码");
        }
        user.setPassword(encoder.encode(newPassword));
        //更新密码
        userDao.updateUser(user);

    }

    /**
     * 根据用户名分页搜索用户
     *
     * @param userName
     * @return
     */
    public List<User> searchUserByName(String userName, Integer page, Integer showCount) {
        return userDao.searchUserByName(userName, (page - 1) * showCount, showCount);
    }

    /**
     * 分页查询用户
     *
     * @param page
     * @param showCount
     * @return
     */
    public List<User> findUser(Integer page, Integer showCount) {
        return userDao.findUser((page - 1) * showCount, showCount);
    }

    /**
     * 查询用户数
     *
     * @return
     */
    public Long getUserCount() {
        return userDao.getUserCount();
    }


    /**
     * 查询用户邮箱
     *
     * @return
     */
    public String findUserMail() {
        User user = userDao.findUserByName(jwtTokenUtil.getUsernameFromRequest(request));
        return user.getMail();
    }

    /**
     * 模糊查询用户名 返回记录数
     *
     * @param userName
     * @return
     */
    public Long getUserCountByName(String userName) {
        return userDao.getUserCountByName(userName);
    }

    /**
     * 获取用户打赏码
     *
     * @return
     */
    public String findUserReward() {
        User user = userDao.findUserByName(jwtTokenUtil.getUsernameFromRequest(request));
        return user.getReward();
    }

    /**
     * 更改用户打赏码
     *
     * @param imgPath
     */
    public void updateUserReward(String imgPath) {
        User user = userDao.findUserByName(jwtTokenUtil.getUsernameFromRequest(request));
        user.setReward(imgPath);
        userDao.updateUser(user);
    }

    /**
     * 更新用户邮箱
     *
     * @param newMail     新邮箱
     * @param oldMailCode 旧邮箱验证码
     * @param newMailCode 新邮箱验证码
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateUserMail(String newMail, String oldMailCode, String newMailCode) {

        // 获取向旧邮箱发出的验证码
        String userName = jwtTokenUtil.getUsernameFromRequest(request);
        User user = userDao.findUserByName(userName);

        // 与用户输入的旧邮箱验证码进行匹配
        if (!checkMailCode(user.getMail(), oldMailCode)) {
            throw new RuntimeException("旧邮箱无效验证码");
        }

        //检查新邮箱是否已存在
        if (userDao.findUserByMail(newMail) != null) {
            throw new RuntimeException("此邮箱已使用");
        }


        //校验新邮箱验证码
        if (!checkMailCode(newMail, newMailCode)) {
            throw new RuntimeException("新邮箱无效验证码");
        }


        user.setMail(newMail);
        //更新用户邮箱信息
        userDao.updateUser(user);

    }

}
