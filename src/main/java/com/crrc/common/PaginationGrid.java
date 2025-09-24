package com.crrc.common;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 分页信息封装
 * 此实体可以兼容多种分页返回，建议所有接口都使用
 *
 * @author 谭永强
 * @date 2022-08-04
 */
public class PaginationGrid<T> {

    /**
     * 当前页码
     */
    private Integer page;
    /**
     * 每页显示条数
     */
    private Integer pageSize;
    /**
     * 起始记录
     */
    private Integer offset;
    /**
     * 结束记录
     */
    private Integer endset;
    /**
     * 总记录数
     */
    private Long total = 0L;
    /**
     * 数据集
     */
    private List<T> rows = new ArrayList<>();

    /**
     * 无参构造
     */
    public PaginationGrid() {

    }

    /**
     * 根据页码+偏离量创建实例
     *
     * @param page     页码
     * @param pageSize 偏移量
     */
    public PaginationGrid(Integer page, Integer pageSize) {
        if (page == null) {
            page = 1;
        }
        if (pageSize == null) {
            pageSize = 0;
        }
        this.page = page;
        this.pageSize = pageSize;
        if (page == 1) {
            this.offset = 0;
            this.endset = pageSize;
        } else {
            this.offset = (page - 1) * pageSize;
            this.endset = page * pageSize;
        }
    }

    /**
     * 分页信息结果返回（mybatis plus分頁）
     *
     * @param page 当前页数据
     * @return 分页数据
     */
    public static <T> PaginationGrid<T> success(Page<T> page) {
        if (ObjectUtils.isEmpty(page)) {
            return null;
        }
        PaginationGrid<T> page1 = new PaginationGrid<>((int) page.getCurrent(), (int) page.getSize());
        page1.setTotal(page.getTotal());
        page1.setRows(page.getRecords());
        return page1;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public Integer getEndset() {
        return endset;
    }

    public void setEndset(Integer endset) {
        this.endset = endset;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public List<T> getRows() {
        return rows;
    }

    public void setRows(List<T> rows) {
        this.rows = rows;
    }
}
