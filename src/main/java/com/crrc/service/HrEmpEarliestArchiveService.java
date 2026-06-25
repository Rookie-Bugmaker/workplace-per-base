package com.crrc.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.crrc.entity.HrEmpEarliestArchive;

import java.util.List;

/**
 * 员工早期档案数据 服务接口
 *
 * <p>该接口定义了员工早期档案数据的查询方法，继承 IService 获得 MyBatis-Plus
 * 的标准 CRUD 方法。本模块仅提供查询功能，不提供增删改操作。</p>
 *
 * <p>查询方法包括：
 * <ul>
 *     <li>getByDa1 - 根据档案号（DA1）精确查询单条记录</li>
 *     <li>queryByCondition - 根据条件查询列表（不分页）</li>
 *     <li>pageByCondition - 根据条件分页查询</li>
 * </ul>
 * </p>
 *
 * <p>所有查询方法都支持动态条件拼装，根据传入实体对象的非空字段自动构建查询条件。</p>
 *
 * @author yukun
 * @since 2026-06-24
 */
public interface HrEmpEarliestArchiveService extends IService<HrEmpEarliestArchive> {

    /**
     * 根据档案号精确查询
     *
     * @param da1 员工档案号（DA1字段），不能为空
     * @return 员工早期档案信息，如果不存在返回 null
     */
    HrEmpEarliestArchive getByDa1(String da1);

    /**
     * 条件查询（不分页）
     *
     * @param queryEntity 查询条件实体对象，非空字段作为查询条件。传入 null 则查询所有数据
     * @return 匹配的员工早期档案列表
     */
    List<HrEmpEarliestArchive> queryByCondition(HrEmpEarliestArchive queryEntity);

    /**
     * 条件查询（分页）
     *
     * @param page 分页参数
     * @param queryEntity 查询条件实体对象，非空字段作为查询条件。传入 null 则查询所有数据
     * @return 分页结果
     */
    IPage<HrEmpEarliestArchive> pageByCondition(IPage<HrEmpEarliestArchive> page, HrEmpEarliestArchive queryEntity);
}
