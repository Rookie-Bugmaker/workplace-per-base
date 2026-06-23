package com.crrc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.crrc.entity.HrEmpHcmArchive;
import com.crrc.mapper.HrEmpHcmArchiveMapper;
import com.crrc.service.HrEmpHcmArchiveService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 员工历史档案数据 服务实现类
 *
 * <p>该实现类负责处理员工历史档案数据的查询业务逻辑，所有数据操作使用 LambdaQueryWrapper 构建，
 * 不使用 XML 映射文件。继承 WorkServiceImpl 获得 MyBatis-Plus 的标准实现。</p>
 *
 * <p>主要功能包括：
 * <ul>
 *     <li>根据档案号精确查询</li>
 *     <li>根据条件动态查询（不分页）</li>
 *     <li>根据条件动态分页查询</li>
 * </ul>
 * </p>
 *
 * <p>所有查询方法都进行参数校验，避免无效查询。参数为空时返回空列表或 null，避免异常。</p>
 *
 * @author AI Generated
 * @since 2026-06-23
 */
@Service
@Slf4j
public class HrEmpHcmArchiveServiceImpl
        extends WorkServiceImpl<HrEmpHcmArchiveMapper, HrEmpHcmArchive>
        implements HrEmpHcmArchiveService {

    /**
     * 根据档案号精确查询
     *
     * <p>使用 LambdaQueryWrapper 构建等值查询条件，通过档案号（userCode）精确匹配查询单条记录。
     * 如果档案号为空，记录警告日志并返回 null。</p>
     *
     * @param userCode 员工档案号（DA2字段），不能为空
     * @return 员工历史档案信息，如果不存在返回 null
     */
    @Override
    public HrEmpHcmArchive getByUserCode(String userCode) {
        // 参数校验：档案号不能为空，避免无效查询
        if (StringUtils.isBlank(userCode)) {
            log.warn("档案号为空，无法查询");
            return null;
        }

        // 构建 LambdaQueryWrapper 查询条件
        LambdaQueryWrapper<HrEmpHcmArchive> wrapper = new LambdaQueryWrapper<>();

        // 使用等值查询匹配档案号（DA2字段对应 userCode）
        // 等价于 SQL: WHERE user_code = #{userCode}
        wrapper.eq(HrEmpHcmArchive::getUserCode, userCode);

        // 执行查询并返回结果
        // selectOne 会在查询到多条记录时抛出异常，确保数据唯一性
        return baseMapper.selectOne(wrapper);
    }

    /**
     * 条件查询（不分页）
     *
     * <p>根据传入的实体对象，动态拼装非空字段的查询条件，返回所有匹配的员工历史档案列表。
     * 如果查询实体为空，则查询所有数据。</p>
     *
     * @param queryEntity 查询条件实体对象，非空字段作为查询条件
     * @return 匹配的员工历史档案列表，如果没有匹配数据返回空列表
     */
    @Override
    public List<HrEmpHcmArchive> queryByCondition(HrEmpHcmArchive queryEntity) {
        // 如果查询实体为空，创建默认实体（查询所有数据）
        queryEntity = (queryEntity == null) ? new HrEmpHcmArchive() : queryEntity;

        // 构建 LambdaQueryWrapper 查询条件
        // buildQueryWrapper 方法会根据实体对象的非空字段动态拼装查询条件
        LambdaQueryWrapper<HrEmpHcmArchive> wrapper = buildQueryWrapper(queryEntity);

        // 执行查询并返回结果列表
        return baseMapper.selectList(wrapper);
    }

    /**
     * 条件查询（分页）
     *
     * <p>根据传入的实体对象，动态拼装非空字段的查询条件，并返回分页结果。
     * 如果查询实体为空，则查询所有数据并进行分页。</p>
     *
     * @param page 分页参数，包含当前页码和每页大小
     * @param queryEntity 查询条件实体对象，非空字段作为查询条件
     * @return 分页结果，包含总记录数和当前页数据
     */
    @Override
    public IPage<HrEmpHcmArchive> pageByCondition(IPage<HrEmpHcmArchive> page, HrEmpHcmArchive queryEntity) {
        // 如果查询实体为空，创建默认实体（查询所有数据）
        queryEntity = (queryEntity == null) ? new HrEmpHcmArchive() : queryEntity;

        // 构建 LambdaQueryWrapper 查询条件
        // buildQueryWrapper 方法会根据实体对象的非空字段动态拼装查询条件
        LambdaQueryWrapper<HrEmpHcmArchive> wrapper = buildQueryWrapper(queryEntity);

        // 执行分页查询
        // MyBatis-Plus 会自动处理分页逻辑，生成 LIMIT 语句
        return baseMapper.selectPage(page, wrapper);
    }

    /**
     * 构建查询条件
     *
     * <p>根据实体类中非空字段动态拼装 LambdaQueryWrapper 查询条件。
     * 支持的查询条件包括：
     * <ul>
     *     <li>userCode - 档案号（模糊查询）</li>
     *     <li>name - 姓名（模糊查询）</li>
     *     <li>dept - 部门编码（精确查询）</li>
     *     <li>gender - 性别（精确查询）</li>
     *     <li>nation - 民族（精确查询）</li>
     *     <li>nativePlace - 籍贯（模糊查询）</li>
     *     <li>birthDate - 出生日期（精确查询）</li>
     *     <li>workDate - 入职日期（精确查询）</li>
     *     <li>political - 政治面貌（精确查询）</li>
     *     <li>education - 学历（精确查询）</li>
     * </ul>
     * </p>
     *
     * <p>可以根据业务需求在此方法中添加更多字段的查询条件。</p>
     *
     * @param queryEntity 查询条件实体对象
     * @return 构建完成的 LambdaQueryWrapper
     */
    private LambdaQueryWrapper<HrEmpHcmArchive> buildQueryWrapper(HrEmpHcmArchive queryEntity) {
        // 创建空的 LambdaQueryWrapper
        LambdaQueryWrapper<HrEmpHcmArchive> wrapper = new LambdaQueryWrapper<>();

        // 条件1：如果档案号（DA2）不为空，添加档案号模糊查询条件
        // 使用 like 方法进行模糊匹配，相当于 SQL 的 LIKE '%关键字%'
        if (StringUtils.isNotBlank(queryEntity.getUserCode())) {
            wrapper.like(HrEmpHcmArchive::getUserCode, queryEntity.getUserCode());
        }

        // 条件2：如果姓名（DA4）不为空，添加姓名模糊查询条件
        // 支持中文姓名的模糊匹配
        if (StringUtils.isNotBlank(queryEntity.getName())) {
            wrapper.like(HrEmpHcmArchive::getName, queryEntity.getName());
        }

        // 条件3：如果部门编码（DA3）不为空，添加部门精确查询条件
        // 使用 eq 方法进行精确匹配
        if (StringUtils.isNotBlank(queryEntity.getDept())) {
            wrapper.eq(HrEmpHcmArchive::getDept, queryEntity.getDept());
        }

        // 条件4：如果性别（DA6）不为空，添加性别精确查询条件
        if (StringUtils.isNotBlank(queryEntity.getGender())) {
            wrapper.eq(HrEmpHcmArchive::getGender, queryEntity.getGender());
        }

        // 条件5：如果民族（DA7）不为空，添加民族精确查询条件
        if (StringUtils.isNotBlank(queryEntity.getNation())) {
            wrapper.eq(HrEmpHcmArchive::getNation, queryEntity.getNation());
        }

        // 条件6：如果籍贯（DA8）不为空，添加籍贯模糊查询条件
        if (StringUtils.isNotBlank(queryEntity.getNativePlace())) {
            wrapper.like(HrEmpHcmArchive::getNativePlace, queryEntity.getNativePlace());
        }

        // 条件7：如果出生日期（DA5）不为空，添加出生日期精确查询条件
        if (queryEntity.getBirthDate() != null) {
            wrapper.eq(HrEmpHcmArchive::getBirthDate, queryEntity.getBirthDate());
        }

        // 条件8：如果入职日期（DA16）不为空，添加入职日期精确查询条件
        if (queryEntity.getWorkDate() != null) {
            wrapper.eq(HrEmpHcmArchive::getWorkDate, queryEntity.getWorkDate());
        }

        // 条件9：如果政治面貌（DA28）不为空，添加政治面貌精确查询条件
        if (StringUtils.isNotBlank(queryEntity.getPolitical())) {
            wrapper.eq(HrEmpHcmArchive::getPolitical, queryEntity.getPolitical());
        }

        // 条件10：如果学历（DA22）不为空，添加学历精确查询条件
        if (StringUtils.isNotBlank(queryEntity.getEducation())) {
            wrapper.eq(HrEmpHcmArchive::getEducation, queryEntity.getEducation());
        }

        // 可以继续添加更多字段的查询条件...
        // 根据业务需求，可以扩展更多字段的查询逻辑

        // 返回构建完成的查询条件
        return wrapper;
    }
}
