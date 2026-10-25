package com.xinghai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinghai.common.PageResult;
import com.xinghai.common.ResultCode;
import com.xinghai.entity.*;
import com.xinghai.exception.BusinessException;
import com.xinghai.mapper.*;
import com.xinghai.security.UserContext;
import com.xinghai.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 内容运营服务：分类、标签、公告、广告、举报（用户端只读 + 管理端增删改）
 * <p>性能：分类/标签/公告/广告属于「低频变更、高频读取」的热点数据，已接入 Redis 缓存，
 * 管理端增删改后主动失效对应缓存；Redis 异常时自动降级直查数据库，不影响主流程。</p>
 */
@Slf4j
@Service
public class ContentService {

    /** 缓存 Key 前缀 */
    private static final String CACHE_CATEGORY = "cache:category:list";
    private static final String CACHE_TAG_PREFIX = "cache:tag:list:";
    private static final String CACHE_NOTICE = "cache:notice:list";
    private static final String CACHE_ADVERT_PREFIX = "cache:advert:list:";
    /** 缓存有效期 10 分钟（秒） */
    private static final long CACHE_TTL = 600L;

    private final BizCategoryMapper categoryMapper;
    private final BizTagMapper tagMapper;
    private final BizNoticeMapper noticeMapper;
    private final BizAdvertMapper advertMapper;
    private final BizReportMapper reportMapper;
    private final BizVideoMapper videoMapper;
    private final RedisUtil redisUtil;

    public ContentService(BizCategoryMapper categoryMapper, BizTagMapper tagMapper,
                          BizNoticeMapper noticeMapper, BizAdvertMapper advertMapper,
                          BizReportMapper reportMapper, BizVideoMapper videoMapper,
                          RedisUtil redisUtil) {
        this.categoryMapper = categoryMapper;
        this.tagMapper = tagMapper;
        this.noticeMapper = noticeMapper;
        this.advertMapper = advertMapper;
        this.reportMapper = reportMapper;
        this.videoMapper = videoMapper;
        this.redisUtil = redisUtil;
    }

    /** 读缓存（带类型兜底；Redis 异常或类型不符时返回 null，调用方走 DB） */
    @SuppressWarnings("unchecked")
    private <T> List<T> getCachedList(String key) {
        try {
            Object obj = redisUtil.get(key);
            if (obj instanceof List) {
                return (List<T>) obj;
            }
        } catch (Exception e) {
            log.warn("读取缓存失败，降级直查DB，key={}: {}", key, e.getMessage());
        }
        return null;
    }

    /** 写缓存（失败不影响主流程） */
    private <T> void putCachedList(String key, List<T> data) {
        try {
            redisUtil.set(key, data, CACHE_TTL);
        } catch (Exception e) {
            log.warn("写入缓存失败，key={}: {}", key, e.getMessage());
        }
    }

    /** 失效缓存（按前缀删除，失败不影响主流程） */
    private void evict(String... keys) {
        for (String key : keys) {
            try {
                redisUtil.delete(key);
            } catch (Exception e) {
                log.warn("失效缓存失败，key={}: {}", key, e.getMessage());
            }
        }
    }

    /** 失效某一类的全部缓存（前缀匹配，如 tag 按分类、广告按位置） */
    private void evictByPrefix(String prefix) {
        try {
            java.util.Set<String> keys = redisUtil.keys(prefix + "*");
            if (keys != null && !keys.isEmpty()) {
                redisUtil.delete(keys);
            }
        } catch (Exception e) {
            log.warn("前缀失效缓存失败，prefix={}: {}", prefix, e.getMessage());
        }
    }

    public List<BizCategory> listCategories() {
        List<BizCategory> cached = getCachedList(CACHE_CATEGORY);
        if (cached != null) {
            return cached;
        }
        List<BizCategory> list = categoryMapper.selectList(new LambdaQueryWrapper<BizCategory>()
                .eq(BizCategory::getStatus, 1)
                .orderByAsc(BizCategory::getSort));
        putCachedList(CACHE_CATEGORY, list);
        return list;
    }

    public void saveCategory(String name, Integer sort) {
        BizCategory c = new BizCategory();
        c.setName(name);
        c.setSort(sort == null ? 0 : sort);
        c.setStatus(1);
        categoryMapper.insert(c);
        evict(CACHE_CATEGORY);
    }

    public void updateCategory(Long id, String name, Integer sort, Integer status) {
        BizCategory c = categoryMapper.selectById(id);
        if (c == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        if (name != null && !name.isBlank()) {
            c.setName(name);
        }
        if (sort != null) {
            c.setSort(sort);
        }
        if (status != null) {
            c.setStatus(status);
        }
        categoryMapper.updateById(c);
        evict(CACHE_CATEGORY);
    }

    public void deleteCategory(Long id) {
        categoryMapper.deleteById(id);
        evict(CACHE_CATEGORY);
        evictByPrefix(CACHE_TAG_PREFIX);
    }

    public List<BizTag> listTags(Long categoryId) {
        String key = CACHE_TAG_PREFIX + (categoryId == null ? "all" : categoryId);
        List<BizTag> cached = getCachedList(key);
        if (cached != null) {
            return cached;
        }
        List<BizTag> list = tagMapper.selectList(new LambdaQueryWrapper<BizTag>()
                .eq(categoryId != null, BizTag::getCategoryId, categoryId)
                .orderByAsc(BizTag::getSort));
        putCachedList(key, list);
        return list;
    }

    public void saveTag(String name, Long categoryId, Integer hot) {
        BizTag t = new BizTag();
        t.setName(name);
        t.setCategoryId(categoryId);
        t.setHot(hot == null ? 0 : hot);
        t.setSort(0);
        tagMapper.insert(t);
        evictByPrefix(CACHE_TAG_PREFIX);
    }

    public void updateTag(Long id, String name, Long categoryId, Integer hot) {
        BizTag t = tagMapper.selectById(id);
        if (t == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        if (name != null && !name.isBlank()) {
            t.setName(name);
        }
        if (categoryId != null) {
            t.setCategoryId(categoryId);
        }
        if (hot != null) {
            t.setHot(hot);
        }
        tagMapper.updateById(t);
        evictByPrefix(CACHE_TAG_PREFIX);
    }

    public void deleteTag(Long id) {
        tagMapper.deleteById(id);
        evictByPrefix(CACHE_TAG_PREFIX);
    }

    public List<BizNotice> listNotices() {
        List<BizNotice> cached = getCachedList(CACHE_NOTICE);
        if (cached != null) {
            return cached;
        }
        List<BizNotice> list = noticeMapper.selectList(new LambdaQueryWrapper<BizNotice>()
                .eq(BizNotice::getStatus, 1)
                .orderByAsc(BizNotice::getSort));
        putCachedList(CACHE_NOTICE, list);
        return list;
    }

    public void saveNotice(String title, String content) {
        BizNotice n = new BizNotice();
        n.setTitle(title);
        n.setContent(content);
        n.setStatus(1);
        n.setSort(0);
        noticeMapper.insert(n);
        evict(CACHE_NOTICE);
    }

    public void updateNotice(Long id, String title, String content, Integer status) {
        BizNotice n = noticeMapper.selectById(id);
        if (n == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        if (title != null && !title.isBlank()) {
            n.setTitle(title);
        }
        if (content != null && !content.isBlank()) {
            n.setContent(content);
        }
        if (status != null) {
            n.setStatus(status);
        }
        noticeMapper.updateById(n);
        evict(CACHE_NOTICE);
    }

    public void deleteNotice(Long id) {
        noticeMapper.deleteById(id);
        evict(CACHE_NOTICE);
    }

    public List<BizAdvert> listAdverts(String position) {
        String key = CACHE_ADVERT_PREFIX + (position == null || position.isBlank() ? "all" : position);
        List<BizAdvert> cached = getCachedList(key);
        if (cached != null) {
            return cached;
        }
        List<BizAdvert> list = advertMapper.selectList(new LambdaQueryWrapper<BizAdvert>()
                .eq(BizAdvert::getStatus, 1)
                .eq(position != null && !position.isBlank(), BizAdvert::getPosition, position)
                .orderByAsc(BizAdvert::getSort));
        putCachedList(key, list);
        return list;
    }

    public void saveAdvert(BizAdvert advert) {
        advert.setStatus(1);
        advertMapper.insert(advert);
        evictByPrefix(CACHE_ADVERT_PREFIX);
    }

    public void updateAdvert(BizAdvert advert) {
        advertMapper.updateById(advert);
        evictByPrefix(CACHE_ADVERT_PREFIX);
    }

    public void deleteAdvert(Long id) {
        advertMapper.deleteById(id);
        evictByPrefix(CACHE_ADVERT_PREFIX);
    }

    /** 用户提交举报 */
    public Long createReport(String targetType, Long targetId, String reason, String detail) {
        BizReport report = new BizReport();
        report.setUserId(UserContext.getUserId());
        report.setTargetType(targetType);
        report.setTargetId(targetId);
        report.setReason(reason);
        report.setDetail(detail);
        report.setStatus(0);
        reportMapper.insert(report);
        return report.getId();
    }

    /** 管理端：举报列表 */
    public PageResult<BizReport> reportList(Integer status, long pageNum, long pageSize) {
        Page<BizReport> page = reportMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<BizReport>()
                        .eq(status != null, BizReport::getStatus, status)
                        .orderByDesc(BizReport::getCreateTime));
        return PageResult.of(page);
    }

    /** 管理端：处理举报（agree=处理 reject=驳回） */
    @Transactional
    public void handleReport(Long reportId, String action) {
        BizReport report = reportMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        if (!"agree".equals(action) && !"reject".equals(action)) {
            throw new BusinessException("处理动作不合法");
        }
        report.setStatus("agree".equals(action) ? 1 : 2);
        report.setHandleUserId(UserContext.getUserId());
        report.setHandleTime(LocalDateTime.now());
        reportMapper.updateById(report);

        // 同意举报：联动处理被举报对象（视频下架）
        if ("agree".equals(action) && "video".equals(report.getTargetType())) {
            BizVideo v = videoMapper.selectById(report.getTargetId());
            if (v != null && v.getStatus() == 1) {
                v.setStatus(2);
                v.setRejectReason("因用户举报被下架");
                videoMapper.updateById(v);
            }
        }
    }
}
