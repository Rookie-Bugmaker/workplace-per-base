package com.crrc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.crrc.entity.HrEmpEarliestArchive;
import com.crrc.mapper.HrEmpEarliestArchiveMapper;
import com.crrc.service.HrEmpEarliestArchiveService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 员工早期档案数据 服务实现类
 *
 * <p>所有数据操作使用 LambdaQueryWrapper 构建，不使用 XML 映射文件。</p>
 *
 * @author yukun
 * @since 2026-06-24
 */
@Service
@Slf4j
public class HrEmpEarliestArchiveServiceImpl
        extends WorkServiceImpl<HrEmpEarliestArchiveMapper, HrEmpEarliestArchive>
        implements HrEmpEarliestArchiveService {

    @Override
    public HrEmpEarliestArchive getByDa1(String da1) {
        if (StringUtils.isBlank(da1)) {
            log.warn("档案号为空，无法查询");
            return null;
        }

        LambdaQueryWrapper<HrEmpEarliestArchive> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HrEmpEarliestArchive::getDa1, da1);
        return baseMapper.selectOne(wrapper);
    }

    @Override
    public List<HrEmpEarliestArchive> queryByCondition(HrEmpEarliestArchive queryEntity) {
        queryEntity = (queryEntity == null) ? new HrEmpEarliestArchive() : queryEntity;
        LambdaQueryWrapper<HrEmpEarliestArchive> wrapper = buildQueryWrapper(queryEntity);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public IPage<HrEmpEarliestArchive> pageByCondition(IPage<HrEmpEarliestArchive> page, HrEmpEarliestArchive queryEntity) {
        queryEntity = (queryEntity == null) ? new HrEmpEarliestArchive() : queryEntity;
        LambdaQueryWrapper<HrEmpEarliestArchive> wrapper = buildQueryWrapper(queryEntity);
        return baseMapper.selectPage(page, wrapper);
    }

    /**
     * 构建查询条件
     *
     * <p>支持的查询条件：
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
     */
    private LambdaQueryWrapper<HrEmpEarliestArchive> buildQueryWrapper(HrEmpEarliestArchive queryEntity) {
        LambdaQueryWrapper<HrEmpEarliestArchive> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.isNotBlank(queryEntity.getDa1())) {
            wrapper.like(HrEmpEarliestArchive::getDa1, queryEntity.getDa1());
        }
        if (StringUtils.isNotBlank(queryEntity.getDa3())) {
            wrapper.like(HrEmpEarliestArchive::getDa3, queryEntity.getDa3());
        }
        if (StringUtils.isNotBlank(queryEntity.getDa2())) {
            wrapper.like(HrEmpEarliestArchive::getDa2, queryEntity.getDa2());
        }
        if (StringUtils.isNotBlank(queryEntity.getDa5())) {
            wrapper.eq(HrEmpEarliestArchive::getDa5, queryEntity.getDa5());
        }
        if (StringUtils.isNotBlank(queryEntity.getDa6())) {
            wrapper.eq(HrEmpEarliestArchive::getDa6, queryEntity.getDa6());
        }
        if (StringUtils.isNotBlank(queryEntity.getDa7())) {
            wrapper.like(HrEmpEarliestArchive::getDa7, queryEntity.getDa7());
        }
        if (StringUtils.isNotBlank(queryEntity.getDa4())) {
            wrapper.eq(HrEmpEarliestArchive::getDa4, queryEntity.getDa4());
        }
        if (StringUtils.isNotBlank(queryEntity.getDa24())) {
            wrapper.eq(HrEmpEarliestArchive::getDa24, queryEntity.getDa24());
        }
        if (StringUtils.isNotBlank(queryEntity.getDa52())) {
            wrapper.eq(HrEmpEarliestArchive::getDa52, queryEntity.getDa52());
        }
        if (StringUtils.isNotBlank(queryEntity.getDa9())) {
            wrapper.eq(HrEmpEarliestArchive::getDa9, queryEntity.getDa9());
        }

        return wrapper;
    }
}
