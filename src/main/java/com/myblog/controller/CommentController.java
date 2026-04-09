package com.myblog.controller;

import com.myblog.common.Result;
import com.myblog.entity.Comment;
import com.myblog.service.CommentService;
import com.myblog.service.CommentService.CommentVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评论控制器
 *
 * 思考：评论API的设计
 * 1. 评论属于文章，所以URL设计为 /api/articles/{id}/comments
 * 2. 发表评论不需要登录（匿名评论）
 * 3. 返回树形结构的评论，方便前端展示
 */
@RestController
@RequestMapping("/api/articles")
public class CommentController {

    @Autowired
    private CommentService commentService;

    /**
     * 评论创建请求DTO
     *
     * 思考：参数校验的重要性
     * 1. 防止非法数据进入系统
     * 2. 提供友好的错误提示
     * 3. 减少后续代码的防御性判断
     */
    @Data
    public static class CommentRequest {
        @NotBlank(message = "昵称不能为空")
        @Size(max = 50, message = "昵称最多50个字符")
        private String nickname;

        @Email(message = "邮箱格式不正确")
        private String email;

        @NotBlank(message = "评论内容不能为空")
        @Size(max = 1000, message = "评论内容最多1000个字符")
        private String content;

        /**
         * 父评论ID（回复时需要）
         * null表示顶级评论
         */
        private Long parentId;
    }

    /**
     * 获取文章的评论列表
     * 公开接口，无需登录
     */
    @GetMapping("/{id}/comments")
    public Result<List<CommentVO>> list(@PathVariable Long id) {
        List<CommentVO> comments = commentService.getTreeByArticleId(id);
        return Result.success(comments);
    }

    /**
     * 发表评论
     * 公开接口，无需登录（支持匿名评论）
     */
    @PostMapping("/{id}/comments")
    public Result<Comment> create(
            @PathVariable Long id,
            @Valid @RequestBody CommentRequest request) {

        Comment comment = new Comment();
        comment.setArticleId(id);
        comment.setNickname(request.getNickname());
        comment.setEmail(request.getEmail());
        comment.setContent(request.getContent());
        comment.setParentId(request.getParentId());

        Comment created = commentService.create(comment);
        return Result.success(created);
    }
}
