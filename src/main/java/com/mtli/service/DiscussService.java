package com.mtli.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtli.dao.BlogDao;
import com.mtli.dao.DiscussDao;
import com.mtli.dao.ReplyDao;
import com.mtli.dao.UserDao;
import com.mtli.model.pojo.Blog;
import com.mtli.model.pojo.Discuss;
import com.mtli.model.pojo.Reply;
import com.mtli.model.pojo.User;
import com.mtli.utils.DateUtil;
import com.mtli.utils.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Service
public class DiscussService {
    @Autowired
    private DiscussDao discussDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private ReplyDao replyDao;

    @Autowired
    private BlogDao blogDao;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private DateUtil dateUtil;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    ObjectMapper objectMapper;


    /**
     * 发布评论
     * 博文评论数加1
     *
     * @param discussBody
     * @param blogId
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveDiscuss(String discussBody, Integer blogId) throws JsonProcessingException {
        // 获取评论者的信息
        User user = userDao.findUserByName(jwtTokenUtil.getUsernameFromRequest(request));
        Blog blog = blogDao.findBlogById(blogId);
        Discuss discuss = new Discuss();
        // 保存评论信息
        discuss.setUser(user);
        discuss.setBody(discussBody);
        discuss.setBlog(blog);
        discuss.setTime(dateUtil.getCurrentDate());
        discussDao.saveDiscuss(discuss);
        // user隐藏相关字段
        blog.getUser().setPassword(null);
        blog.getUser().setMail(null);
        blog.getUser().setState(null);
        // 评论数加一
        blog.setDiscussCount(blog.getDiscussCount() + 1);
        blogDao.updateBlog(blog);
        // 同时更新redis
        // redisTemplate.opsForValue().set(RedisConfig.REDIS_BLOG_PREFIX + blog.getId(), objectMapper.writeValueAsString(blog));
    }

    /**
     * 删除评论
     * 级联删除评论下的所有回复
     * 博文评论数 - (评论数+回复数)
     *
     * @param discussId
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteDiscuss(Integer discussId) {
        // 获取删除者用户信息
        User user = userDao.findUserByName(jwtTokenUtil.getUsernameFromRequest(request));
        Discuss discuss = discussDao.findDiscussById(discussId);
        if (discuss == null) {
            throw new RuntimeException("评论不存在");
        }
        if (!user.getId().equals(discuss.getUser().getId())) {
            throw new RuntimeException("无权删除");
        }

        discussDao.deleteDiscussById(discussId);

        //返回受影响行数
        Integer rows = replyDao.deleteReplyByDiscussId(discussId);


        Blog blog = blogDao.findBlogById(discuss.getBlog().getId());
        blog.setDiscussCount(blog.getDiscussCount() - 1 - rows);
        blogDao.updateBlog(blog);
    }

    /**
     * 管理员删除评论
     * 博文评论数-1
     *
     * @param discussId
     */
    public void adminDeleteDiscuss(Integer discussId) {
        Discuss discuss = discussDao.findDiscussById(discussId);
        if (discuss == null) {
            throw new RuntimeException("评论不存在");
        }
        discussDao.deleteDiscussById(discussId);

        Integer rows = replyDao.deleteReplyByDiscussId(discussId); //返回受影响行数

        Blog blog = blogDao.findBlogById(discuss.getBlog().getId());
        blog.setDiscussCount(blog.getDiscussCount() - 1 - rows);
        blogDao.updateBlog(blog);
    }

    /**
     * 根据博文id查询 该博文下的评论及回复
     *
     * @param blogId
     * @return
     */
    public List<Discuss> findDiscussByBlogId(Integer blogId, Integer page, Integer showCount) {

        List<Discuss> discusses = discussDao.findDiscussByBlogId(blogId, (page - 1) * showCount, showCount);

        for (Discuss discuss : discusses) {
            List<Reply> replyList = replyDao.findReplyByDiscussId(discuss.getId());
            for (Reply reply : replyList) {
                if (reply.getReply() != null) {
                    reply.setReply(replyDao.findReplyById(reply.getReply().getId()));
                }
            }
            discuss.setReplyList(replyList);
        }
        return discusses;
    }

    /**
     * 获取博文下评论数量
     *
     * @param blogId
     * @return
     */
    public Long getDiscussCountByBlogId(Integer blogId) {
        return discussDao.getDiscussCountByBlogId(blogId);
    }

    /**
     * 获取最新六条评论
     *
     * @return
     */
    public List<Discuss> findNewDiscuss() {
        return discussDao.findNewDiscuss(6);
    }


    /**
     * 获取用户发布的所有博文下的评论
     *
     * @return
     */
    public List<Discuss> findUserNewDiscuss() {
        User user = userDao.findUserByName(jwtTokenUtil.getUsernameFromRequest(request));
        return discussDao.findUserNewDiscuss(user.getId(), 6);
    }
}

