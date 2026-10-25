package com.xinghai.controller;

import com.xinghai.common.PageResult;
import com.xinghai.common.Result;
import com.xinghai.dto.AuditDTO;
import com.xinghai.dto.VideoPublishDTO;
import com.xinghai.dto.VideoUpdateDTO;
import com.xinghai.security.RequireRole;
import com.xinghai.service.VideoService;
import com.xinghai.vo.VideoVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 视频接口：信息流（推荐/关注/同城/搜索/分类）、发布、详情、审核
 */
@RestController
@RequestMapping("/api/video")
public class VideoController {

    private final VideoService videoService;

    public VideoController(VideoService videoService) {
        this.videoService = videoService;
    }

    /** 推荐流（简化标签推荐算法，未登录=热门兜底） */
    @GetMapping("/feed/recommend")
    public Result<PageResult<VideoVO>> recommend(@RequestParam(defaultValue = "1") long pageNum,
                                                 @RequestParam(defaultValue = "10") long pageSize) {
        return Result.ok(videoService.recommend(pageNum, pageSize));
    }

    /** 关注流 */
    @GetMapping("/feed/follow")
    public Result<PageResult<VideoVO>> followFeed(@RequestParam(defaultValue = "1") long pageNum,
                                                  @RequestParam(defaultValue = "10") long pageSize) {
        return Result.ok(videoService.followFeed(pageNum, pageSize));
    }

    /** 同城流 */
    @GetMapping("/feed/nearby")
    public Result<PageResult<VideoVO>> nearby(@RequestParam(required = false) String city,
                                              @RequestParam(defaultValue = "1") long pageNum,
                                              @RequestParam(defaultValue = "10") long pageSize) {
        return Result.ok(videoService.nearby(city, pageNum, pageSize));
    }

    /** 搜索 */
    @GetMapping("/search")
    public Result<PageResult<VideoVO>> search(@RequestParam String keyword,
                                              @RequestParam(defaultValue = "1") long pageNum,
                                              @RequestParam(defaultValue = "10") long pageSize) {
        return Result.ok(videoService.search(keyword, pageNum, pageSize));
    }

    /** 分类浏览 */
    @GetMapping("/category/{categoryId}")
    public Result<PageResult<VideoVO>> byCategory(@PathVariable Long categoryId,
                                                  @RequestParam(defaultValue = "1") long pageNum,
                                                  @RequestParam(defaultValue = "10") long pageSize) {
        return Result.ok(videoService.byCategory(categoryId, pageNum, pageSize));
    }

    /** 视频详情（未登录可看公开视频） */
    @GetMapping("/detail/{videoId}")
    public Result<VideoVO> detail(@PathVariable Long videoId) {
        return Result.ok(videoService.detail(videoId));
    }

    /** 相关推荐（同分类热门，未登录可看） */
    @GetMapping("/related/{videoId}")
    public Result<java.util.List<VideoVO>> related(@PathVariable Long videoId,
                                                   @RequestParam(defaultValue = "8") int limit) {
        return Result.ok(videoService.related(videoId, limit));
    }

    /** 编辑作品（创作者本人） */
    @PutMapping("/{videoId}")
    public Result<Void> update(@PathVariable Long videoId,
                               @Validated @RequestBody VideoUpdateDTO dto) {
        videoService.update(videoId, dto);
        return Result.ok();
    }

    /** 发布视频（登录用户） */
    @PostMapping("/publish")
    public Result<Long> publish(@Validated @RequestBody VideoPublishDTO dto) {
        return Result.ok("发布成功，等待审核", videoService.publish(dto));
    }

    /** 我的作品 */
    @GetMapping("/mine")
    public Result<PageResult<VideoVO>> mine(@RequestParam(defaultValue = "1") long pageNum,
                                            @RequestParam(defaultValue = "10") long pageSize) {
        return Result.ok(videoService.mine(pageNum, pageSize));
    }

    /** 某用户公开作品 */
    @GetMapping("/user/{userId}")
    public Result<PageResult<VideoVO>> userVideos(@PathVariable Long userId,
                                                  @RequestParam(defaultValue = "1") long pageNum,
                                                  @RequestParam(defaultValue = "10") long pageSize) {
        return Result.ok(videoService.userVideos(userId, pageNum, pageSize));
    }

    /** 删除视频 */
    @DeleteMapping("/{videoId}")
    public Result<Void> delete(@PathVariable Long videoId) {
        videoService.delete(videoId);
        return Result.ok();
    }

    /** 设置作品可见性（创作者权限） */
    @PutMapping("/visibility/{videoId}")
    public Result<Void> visibility(@PathVariable Long videoId, @RequestBody Map<String, Integer> body) {
        videoService.changeVisibility(videoId, body.get("isPublic"));
        return Result.ok();
    }

    /** 管理端：视频审核（通过/拒绝/下架/上架） */
    @PostMapping("/audit")
    @RequireRole("ADMIN")
    public Result<Void> audit(@Validated @RequestBody AuditDTO dto) {
        videoService.audit(dto.getVideoId(), dto.getAction(), dto.getRejectReason());
        return Result.ok();
    }

    /** 管理端：按状态筛选视频列表 */
    @GetMapping("/admin/list")
    @RequireRole("ADMIN")
    public Result<PageResult<VideoVO>> adminList(@RequestParam(required = false) Integer status,
                                                 @RequestParam(required = false) String keyword,
                                                 @RequestParam(defaultValue = "1") long pageNum,
                                                 @RequestParam(defaultValue = "10") long pageSize) {
        return Result.ok(videoService.adminList(status, keyword, pageNum, pageSize));
    }
}
