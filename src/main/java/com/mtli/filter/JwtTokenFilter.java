package com.mtli.filter;


import com.mtli.config.JwtConfig;
import com.mtli.config.RedisConfig;
import com.mtli.controller.ErrorController;
import com.mtli.service.UserService;
import com.mtli.utils.RequestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class JwtTokenFilter extends OncePerRequestFilter {

    @Autowired
    private UserService userService;

    @Autowired
    private RequestUtil requestUtil;

    /**
     * 范围时间内限制最大请求次数
     */
    private static final int LIMIT_REQUEST_FREQUENCY_COUNT = 8;


    @Autowired
    private RedisTemplate<String, String> redisTemplate;


    @Autowired
    private JwtConfig jwtConfig;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {

        String ipAddress = requestUtil.getIpAddress(request);
        String redisKey = RedisConfig.REDIS_IP_PREFIX + ipAddress;
        //缓存时间 2s 会影响swagger-ui的使用，建议开发时调整JwtTokenFilter.LIMIT_REQUEST_FREQUENCY_COUNT的值
        // 127.0.0.1_/blog/hotBlog
        if (redisTemplate.hasKey(redisKey)) {
            String value = redisTemplate.opsForValue().get(redisKey);
            Integer count = Integer.parseInt(value);
            if (count > JwtTokenFilter.LIMIT_REQUEST_FREQUENCY_COUNT) {
                //请求频繁
                request.getRequestDispatcher(ErrorController.FREQUENT_OPERATION).forward(request, response);
                return;
            } else {
                count++;
                // 次数加一并存入redis
                redisTemplate.opsForValue().set(redisKey, count.toString(), RedisConfig.REDIS_LIMIT_REQUEST_FREQUENCY_TIME, TimeUnit.MILLISECONDS);
            }

        } else {
            redisTemplate.opsForValue().set(redisKey, "1", RedisConfig.REDIS_LIMIT_REQUEST_FREQUENCY_TIME, TimeUnit.MILLISECONDS);
        }
        // 接收并缓存权限，然后对其进行校验
        checkPermission(request, response, chain);
    }

    /**
     * 校验权限
     *
     * @param request
     * @param response
     */
    private void checkPermission(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        boolean giveFlag = false;
        // 根据http部获取请求的头部 header: "Authorization"
        String authHeader = request.getHeader(jwtConfig.getHeader());

        // prefix: "Bearer "
        if (authHeader != null && authHeader.startsWith(jwtConfig.getPrefix())) {
            // 获取用户详细信息
            UserDetails userDetails = userService.loadUserByToken(authHeader);

            if (null != userDetails) {
                // 此请求是否校验过
                // SecurityContextHolder 这个工具类的目的是用来保存应用程序中当前使用人的安全上下文。
                // getAuthentication 获取当前用户上下文信息（比如角色权限）
                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    // 为空，说明该请求没有校验过，通过下面的方法添加其权限进入安全上下文

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                }
            } else {
                // 否则请求不符合
                giveFlag = true;
            }
        } else {
            //token校验失败
            giveFlag = true;
        }

        // 后期考虑添加游客选项
        if (giveFlag) {
            //token因某原因校验失败,给定游客身份->[游客]角色未写入数据库角色表
            // 省去每个方法上的permitAll注解
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("NORMAL"));
            //假定身份
            User user = new User("NORMAL", "NORMAL", authorities);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            //赋予权限，添加到安全上下文
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        chain.doFilter(request, response);
    }



}

