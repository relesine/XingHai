package com.xinghai.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.util.List;

/**
 * 分页结果统一封装
 *
 * @param <T> 列表元素类型
 */
@Data
public class PageResult<T> {

    /** 当前页码（从1开始） */
    private Long pageNum;

    /** 每页条数 */
    private Long pageSize;

    /** 总条数 */
    private Long total;

    /** 总页数 */
    private Long pages;

    /** 当前页数据 */
    private List<T> list;

    /** 由 MyBatis Plus 的 IPage 转换为统一分页结构 */
    public static <T> PageResult<T> of(IPage<T> page) {
        PageResult<T> result = new PageResult<>();
        result.setPageNum(page.getCurrent());
        result.setPageSize(page.getSize());
        result.setTotal(page.getTotal());
        result.setPages(page.getPages());
        result.setList(page.getRecords());
        return result;
    }
}
