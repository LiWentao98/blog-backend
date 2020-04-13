package com.mtli.service;

import com.mtli.config.JwtConfig;
import com.mtli.dao.CodeDao;
import com.mtli.dao.RoleDao;
import com.mtli.dao.UserDao;
import com.mtli.model.pojo.Role;
import com.mtli.model.pojo.User;
import com.mtli.utils.JwtTokenUtil;
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
    private BCryptPasswordEncoder encoder;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private JwtConfig jwtConfig;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

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
}
