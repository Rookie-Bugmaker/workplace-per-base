package com.crrc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.crrc.entity.ExceptionCasePersonRelation;
import com.crrc.pojo.vo.ExceptionCasePersonRelationQueryVO;

/**
 * <p>
 * 额外人员信息关联表 服务类
 * </p>
 *
 * @author mayiyao
 * @since 2026-01-15 13:44:29
 */
public interface ExceptionCasePersonRelationService extends IService<ExceptionCasePersonRelation> {

    /**
     * 根据查询条件分页查询
     * @param page 分页条件
     * @param queryVO 查询条件
     */
     void queryByVO(Page<ExceptionCasePersonRelation> page, ExceptionCasePersonRelationQueryVO queryVO);
 }