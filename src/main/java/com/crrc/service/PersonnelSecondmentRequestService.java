package com.crrc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.crrc.entity.PersonnelSecondmentRequest;

import java.util.List;

/**
 * <p>
 * 借调（借用）人员需求计划申请明细表 服务类
 * </p>
 *
 * @author mayiyao
 * @since 2026-03-04 16:26:51
 */
public interface PersonnelSecondmentRequestService extends IService<PersonnelSecondmentRequest> {
    /**
     * 根据表头id获得所有明细
     * @param relationId 表头id
     * @return 明细列表
     */
    List<PersonnelSecondmentRequest> getByRelationId(String relationId);

    /**
     * 根据reqVOList保存，删除或新增
     * @param relationId 关联的表头id
     * @param reqVOList 明细List
     * @return 是否成功
     */
    boolean saveDetails(String relationId, List<PersonnelSecondmentRequest> reqVOList);
}