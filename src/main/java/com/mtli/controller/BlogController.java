package com.mtli.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mtli.config.RedisConfig;
import com.mtli.model.entity.PageResult;
import com.mtli.model.entity.Result;
import com.mtli.model.entity.StatusCode;
import com.mtli.model.pojo.Blog;
import com.mtli.service.BlogService;
import com.mtli.utils.FormatUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * @Description:
 * @Author: Mt.Li
 */

@Api(tags = "博文api", description = "博文api", basePath = "/blog")
@RestController
@RequestMapping("/blog")
public class BlogController {

    @Autowired
    private BlogService blogService;

    @Autowired
    private FormatUtil formatUtil;

    /**
     * 发布新博文
     * @param blogTitle
     * @param blogBody
     * @param tagId
     * @return
     */
    @ApiOperation(value = "发布博文", notes = "博文标题+博文内容+博文标签")
    @PreAuthorize("hasAuthority('USER')")
    @PostMapping
    public Result newBlog(String blogTitle, String blogBody, Integer[] tagId){
        if (!formatUtil.checkStringNull(blogTitle, blogBody) || !formatUtil.checkPositive(tagId)) {
            return Result.create(StatusCode.ERROR, "参数错误");
        }
        try {
            blogService.saveBlog(blogTitle, blogBody, tagId);
            return Result.create(StatusCode.OK, "发布成功");
        } catch (IOException e) {
            return Result.create(StatusCode.ERROR, "非法操作");
        }
    }

    /**
     * 根据id查询博文
     * @param blogId
     * @param isHistory
     * @return
     */
    @ApiOperation(value = "根据id查询博文", notes = "博文id")
    @GetMapping("/{blogId}/{isHistory}")
    public Result findBlogById(@PathVariable Integer blogId, @PathVariable boolean isHistory) {
        if (!formatUtil.checkObjectNull(blogId, isHistory)) {
            return Result.create(StatusCode.ERROR, "参数错误");
        }
        try {
            return Result.create(StatusCode.OK, "查询成功", blogService.findBlogById(blogId, isHistory));
        } catch (RuntimeException e) {
            return Result.create(StatusCode.NOTFOUND, "此博客不存在");
        } catch (IOException e) {
            return Result.create(StatusCode.ERROR, "此博客不存在");
        }

    }

    /**
     * 根据用户分页查询博文
     *
     * @param page      页数
     * @param showCount 显示条数
     * @return
     */
    @ApiOperation(value = "根据用户分页查询博文", notes = "页数+显示数量")
    @PreAuthorize("hasAuthority('USER')")
    @GetMapping("/myblog/{page}/{showCount}")
    public Result findBlogByUser(@PathVariable Integer page, @PathVariable Integer showCount) {

        if (!formatUtil.checkPositive(page, showCount)) {
            return Result.create(StatusCode.OK, "参数错误");
        }

        PageResult<Blog> pageResult =
                new PageResult<>(blogService.getBlogCountByUser(), blogService.findBlogByUser(page, showCount));

        return Result.create(StatusCode.OK, "查询成功", pageResult);
    }

    /**
     * 首页分页查询
     * 查询的范围在 最近10条博客 内
     *
     * @param page      页码
     * @param showCount 显示条数
     * @return
     */
    @ApiOperation(value = "首页分页查询博文", notes = "页数+显示数量")
    @GetMapping("/home/{page}/{showCount}")
    public Result homeBlog(@PathVariable Integer page, @PathVariable Integer showCount) {
        if (!formatUtil.checkPositive(page, showCount) || showCount > RedisConfig.REDIS_NEW_BLOG_COUNT) {
            return Result.create(StatusCode.OK, "参数错误");
        }


        try {
            PageResult<Blog> pageResult = new PageResult<>(blogService.getHomeBlogCount(), blogService.findHomeBlog(page, showCount));
            return Result.create(StatusCode.OK, "查询成功", pageResult);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.create(StatusCode.SERVICEERROR, "服务异常");
        }


    }

    /**
     * 查询热门博文
     * 正常状态
     *
     * @return
     */
    @ApiOperation(value = "首页热门博文", notes = "首页热门博文")
    @GetMapping("/hotBlog")
    public Result hotBlog() {

        try {
            return Result.create(StatusCode.OK, "查询成功", blogService.findHotBlog());
        } catch (IOException e) {
            return Result.create(StatusCode.SERVICEERROR, "服务异常");
        }
    }

    /**
     * 博文搜索
     * 正常状态
     *
     * @param search
     * @param page
     * @param showCount
     * @return
     */
    @ApiOperation(value = "分页搜索博文", notes = "搜索内容+页码+显示条数")
    @GetMapping("/searchBlog/{page}/{showCount}")
    public Result searchBlog(String search,
                             @PathVariable Integer page,
                             @PathVariable Integer showCount) {
        if (!formatUtil.checkPositive(page, showCount) || showCount > RedisConfig.REDIS_NEW_BLOG_COUNT) {
            return Result.create(StatusCode.OK, "参数错误");
        }

        if (!formatUtil.checkStringNull(search)) {
            return Result.create(StatusCode.OK, "参数错误");
        }

        PageResult<Blog> pageResult = new PageResult<>(blogService.getSearchBlogCount(search),
                blogService.searchBlog(search, page, showCount));
        return Result.create(StatusCode.OK, "查询成功", pageResult);
    }

    /**
     * 查询所有博客，包括删除状态
     *
     * @return
     */
    @ApiOperation(value = "管理员查询博文", notes = "管理员查询博文")
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/AllBlog/{page}/{showCount}")
    public Result findAllBlog(@PathVariable Integer page, @PathVariable Integer showCount) {
        if (!formatUtil.checkPositive(page, showCount)) {
            return Result.create(StatusCode.ERROR, "参数错误");
        }
        PageResult<Blog> pageResult = new PageResult<>(blogService.getAllBlogCount(), blogService.findAllBlog(page, showCount));

        return Result.create(StatusCode.OK, "查询成功", pageResult);
    }

    /**
     * 修改博文
     *
     * @param blogId
     * @param blogTitle
     * @param blogBody
     * @param tagId
     * @return
     */
    @ApiOperation(value = "修改博文", notes = "博文id+博文标题+博文内容+博文标签")
    @PreAuthorize("hasAuthority('USER')")
    @PutMapping("/{blogId}")
    public Result updateBlog(@PathVariable Integer blogId, String blogTitle, String blogBody, Integer[] tagId) {

        if (!formatUtil.checkStringNull(blogTitle, blogBody)) {
            return Result.create(StatusCode.ERROR, "参数错误");
        }

        if (!formatUtil.checkPositive(tagId) || !formatUtil.checkPositive(blogId)) {
            return Result.create(StatusCode.ERROR, "参数错误");
        }

        try {
            blogService.updateBlog(blogId, blogTitle, blogBody, tagId);
            return Result.create(StatusCode.OK, "修改成功");
        } catch (RuntimeException e) {
            return Result.create(StatusCode.ERROR, "修改失败" + e.getMessage());
        } catch (JsonProcessingException e) {
            return Result.create(StatusCode.SERVICEERROR, "服务异常");
        }
    }

    /**
     * 用户删除博文
     *
     * @param blogId
     * @return
     */
    @ApiOperation(value = "用户删除博文", notes = "博文id")
    @PreAuthorize("hasAuthority('USER')")
    @DeleteMapping("/{blogId}")
    public Result deleteBlog(@PathVariable Integer blogId) {
        if (!formatUtil.checkPositive(blogId)) {
            return Result.create(StatusCode.ERROR, "参数错误");
        }

        try {
            blogService.deleteBlog(blogId);
            return Result.create(StatusCode.OK, "删除成功");
        } catch (RuntimeException e) {
            return Result.create(StatusCode.ERROR, "删除失败" + e.getMessage());
        } catch (JsonProcessingException e) {
            return Result.create(StatusCode.SERVICEERROR, "服务异常");
        }
    }

    /**
     * 管理员删除博文
     *
     * @param blogId
     * @return
     * @throws JsonProcessingException
     */
    @ApiOperation(value = "管理员删除博文", notes = "博文id")
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/admin/{blogId}")
    public Result adminDeleteBlog(@PathVariable Integer blogId) throws JsonProcessingException {
        if (!formatUtil.checkPositive(blogId)) {
            return Result.create(StatusCode.ERROR, "参数错误");
        }
        blogService.adminDeleteBlog(blogId);
        return Result.create(StatusCode.OK, "删除成功");
    }

    /**
     * 管理员分页搜索博文
     * @param search
     * @param page
     * @param showCount
     * @return
     */
    @ApiOperation(value = "管理员分页搜索博文", notes = "搜索内容+页码+显示条数")
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/searchAllBlog/{page}/{showCount}")
    public Result searchAllBlog(String search,
                                @PathVariable Integer page,
                                @PathVariable Integer showCount) {

        if (!formatUtil.checkPositive(page, showCount)) {
            return Result.create(StatusCode.OK, "参数错误");
        }
        if (!formatUtil.checkStringNull(search)) {
            return Result.create(StatusCode.OK, "参数错误");
        }

        PageResult<Blog> pageResult = new PageResult<>(blogService.getSearchAllBlogCount(search),
                blogService.searchAllBlog(search, page, showCount));


        return Result.create(StatusCode.OK, "查询成功", pageResult);
    }


    /**
     * 按月份归档博客
     *
     * @return
     */
    @ApiOperation(value = "按月份归档博客", notes = "按月份归档博客")
    @GetMapping("/statisticalBlogByMonth")
    public Result statisticalBlogByMonth() {
        try {
            return Result.create(StatusCode.OK, "查询成功", blogService.statisticalBlogByMonth());
        } catch (IOException e) {
            return Result.create(StatusCode.SERVICEERROR, "服务异常");
        }
    }

}