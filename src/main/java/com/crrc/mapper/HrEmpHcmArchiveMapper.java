package com.crrc.mapper;

import com.crrc.entity.HrEmpHcmArchive;
import com.github.yulichang.base.MPJBaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 员工历史档案数据 Mapper 接口
 *
 * <p>该接口继承 MPJBaseMapper，获得 MyBatis-Plus 的所有基础查询能力，包括：
 * <ul>
 *     <li>selectList(Wrapper) - 根据条件查询列表</li>
 *     <li>selectPage(IPage, Wrapper) - 根据条件分页查询</li>
 *     <li>selectOne(Wrapper) - 根据条件查询单条记录</li>
 *     <li>selectCount(Wrapper) - 根据条件统计数量</li>
 * </ul>
 * </p>
 *
 * <p>所有查询操作均通过 ServiceImpl 使用 LambdaQueryWrapper 构建，
 * 不使用 XML 映射文件，符合项目规范。</p>
 *
 * @author yukun
 * @since 2026-06-23
 */
@Mapper
public interface HrEmpHcmArchiveMapper extends MPJBaseMapper<HrEmpHcmArchive> {

    // 继承自 MPJBaseMapper 的默认方法已满足所有查询需求
    // 无需额外定义自定义方法

}
