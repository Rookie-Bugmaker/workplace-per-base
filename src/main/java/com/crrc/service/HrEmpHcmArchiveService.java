package com.crrc.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.crrc.entity.HrEmpHcmArchive;

import java.util.List;

/**
 * 员工历史档案数据 服务接口
 *
 * <p>该接口定义了员工历史档案数据的查询方法，继承 IService 获得 MyBatis-Plus
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
 * @since 2026-06-23
 */
public interface HrEmpHcmArchiveService extends IService<HrEmpHcmArchive> {

    /**
     * 根据档案号精确查询
     *
     * <p>使用档案号（DA1 字段）进行精确查询，
     * 返回匹配的单条员工历史档案记录。</p>
     *
     * @param da1 员工档案号（DA1字段），不能为空
     * @return 员工历史档案信息，如果不存在返回 null
     */
    HrEmpHcmArchive getByDa1(String da1);

    /**
     * 条件查询（不分页）
     *
     * <p>根据传入的实体对象，自动拼装非空字段的查询条件，返回所有匹配的员工历史档案列表。
     * 支持的查询条件包括：
     * <ul>
     *     <li>da1 - 档案号（模糊查询）</li>
     *     <li>da3 - 姓名（模糊查询）</li>
     *     <li>da2 - 所在部门（模糊查询）</li>
     *     <li>da5 - 性别（精确查询）</li>
     *     <li>da6 - 民族（精确查询）</li>
     *     <li>da7 - 籍贯（模糊查询）</li>
     *     <li>da4 - 出生日期（精确查询）</li>
     *     <li>da24 - 参加工作时间（精确查询）</li>
     *     <li>da52 - 政治面貌（精确查询）</li>
     *     <li>da9 - 第一学历（精确查询）</li>
     * </ul>
     * </p>
     *
     * @param queryEntity 查询条件实体对象，非空字段作为查询条件。
     *                    如果传入 null，则查询所有数据
     * @return 匹配的员工历史档案列表，如果没有匹配数据返回空列表
     */
    List<HrEmpHcmArchive> queryByCondition(HrEmpHcmArchive queryEntity);

    /**
     * 条件查询（分页）
     *
     * <p>根据传入的实体对象，自动拼装非空字段的查询条件，并返回分页结果。
     * 支持的查询条件与 queryByCondition 方法相同。</p>
     *
     * @param page 分页参数，包含当前页码（current）和每页大小（size）
     * @param queryEntity 查询条件实体对象，非空字段作为查询条件。
     *                    如果传入 null，则查询所有数据
     * @return 分页结果，包含总记录数（total）、当前页数据（records）等
     */
    IPage<HrEmpHcmArchive> pageByCondition(IPage<HrEmpHcmArchive> page, HrEmpHcmArchive queryEntity);
}
