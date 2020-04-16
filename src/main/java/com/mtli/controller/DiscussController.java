package com.mtli.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mtli.model.entity.PageResult;
import com.mtli.model.entity.Result;
import com.mtli.model.entity.StatusCode;
import com.mtli.model.pojo.Discuss;
import com.mtli.service.DiscussService;
import com.mtli.utils.FormatUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Api(tags = "评论api", description = "评论api", basePath = "/discuss")
@RestController
@RequestMapping("/discuss")
public class DiscussController {

    @Autowired
    private DiscussService discussService;

    @Autowired
    private FormatUtil formatUtil;

    /**
     * 发布评论
     *
     * @param discussBody
     * @param blogId
     * @return
     */
    @ApiOperation(value = "发布评论", notes = "评论内容+博文id")
    @PreAuthorize("hasAuthority('USER')")
    @PostMapping("/{blogId}")
    public Result discuss(String discussBody, @PathVariable Integer blogId) throws JsonProcessingException {
        if (!formatUtil.checkStringNull(discussBody)) {
            return Result.create(StatusCode.ERROR, "参数错误");
        }
        if (!formatUtil.checkPositive(blogId)) {
            return Result.create(StatusCode.ERROR, "参数错误");
        }

        discussService.saveDiscuss(discussBody, blogId);
        return Result.create(StatusCode.OK, "评论成功");
    }


    /**
     * 删除评论
     *
     * @param discussId
     * @return
     */
    @ApiOperation(value = "删除评论", notes = "评论id")
    @PreAuthorize("hasAuthority('USER')")
    @DeleteMapping("/{discussId}")
    public Result deleteDiscuss(@PathVariable Integer discussId) {
        if (!formatUtil.checkPositive(discussId)) {
            return Result.create(StatusCode.ERROR, "参数错误");
        }
        try {
            discussService.deleteDiscuss(discussId);
            return Result.create(StatusCode.OK, "删除评论成功");
        } catch (RuntimeException e) {
            return Result.create(StatusCode.ERROR, "删除失败" + e.getMessage());
        }

    }

    /**
     * 管理员删除评论
     *
     * @param discussId
     * @return
     */
    @ApiOperation(value = "管理员删除评论", notes = "评论id")
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/admin/{discussId}")
    public Result adminDeleteDiscuss(@PathVariable Integer discussId) {
        if (!formatUtil.checkPositive(discussId)) {
            return Result.create(StatusCode.ERROR, "参数错误");
        }

        try {
            discussService.adminDeleteDiscuss(discussId);
            return Result.create(StatusCode.OK, "删除评论成功");
        } catch (RuntimeException e) {
            return Result.create(StatusCode.ERROR, "删除失败" + e.getMessage());
        }

    }

    /**
     * 分页查询博文评论以及回复
     *
     * @param blogId
     * @param page
     * @param showCount
     * @return
     */
    @ApiOperation(value = "分页查询博文评论以及回复", notes = "博文id+页码+显示数")
    @GetMapping("/{blogId}/{page}/{showCount}")
    public Result getDiscussByBlog(@PathVariable Integer blogId,
                                   @PathVariable Integer page,
                                   @PathVariable Integer showCount) {

        if (!formatUtil.checkPositive(blogId, page, showCount)) {
            return Result.create(StatusCode.ERROR, "参数错误");
        }
        // 这里只计算评论数，不计算回复
        PageResult<Discuss> pageResult = new PageResult<>(discussService.getDiscussCountByBlogId(blogId), discussService.findDiscussByBlogId(blogId, page, showCount));

        return Result.create(StatusCode.OK, "查询成功", pageResult);
    }

    /**
     * 首页获取最新评论
     *
     * @return
     */
    @ApiOperation(value = "首页获取最新评论", notes = "获取最新六条评论")
    @GetMapping("/newDiscuss")
    public Result newDiscuss() {
        return Result.create(StatusCode.OK, "查询成功", discussService.findNewDiscuss());
    }

    @ApiOperation(value = "获取用户发布的所有博文下的评论", notes = "获取用户发布的所有博文下的评论")
    @PreAuthorize("hasAuthority('USER')")
    @GetMapping("/userNewDiscuss")
    public Result userNewDiscuss() {
        return Result.create(StatusCode.OK, "查询成功", discussService.findUserNewDiscuss());
    }


}
