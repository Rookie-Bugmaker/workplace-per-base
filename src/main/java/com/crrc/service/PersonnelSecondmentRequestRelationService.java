package com.crrc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.crrc.entity.PersonnelSecondmentRequestRelation;
import com.crrc.pojo.ResponseResult;
import com.crrc.pojo.dto.WorkflowWriteBackParamDTO;
import com.crrc.pojo.vo.RequestAndBorrowedConcatVO;

import java.util.List;

/**
 * <p>
 * 借调（借用）人员需求计划申请表表头 服务类
 * </p>
 *
 * @author mayiyao
 * @since 2026-03-04 16:26:51
 */
public interface PersonnelSecondmentRequestRelationService extends IService<PersonnelSecondmentRequestRelation> {

    /**
     * 翻译字段
     * @param records 需要翻译的记录
     */
     void resolveFields(List<PersonnelSecondmentRequestRelation> records);

    /**
     * 审批流回写
     * @param param 审批流回写参数
     * @return 审批结果
     */
    ResponseResult<Boolean> writeBack(WorkflowWriteBackParamDTO param);

    /**
     * 提交审批
     * @param id 需要提交审批的审批计划id
     * @return 是否提交成功
     */
    ResponseResult<Boolean> submit(String id);

    /**
     * 推送到目标部门
     * @param id 需要推送的申请表
     * @param deptList 目标部门列表
     * @return 是否成功
     */
    Boolean sendToSourceDept(String id, List<String> deptList);

    /**
     * 根据目标部门进行分页查询
     * @param page 分页信息
     * @param pageReqVO 查询条件
     */
    void pageBySourceDept(Page<RequestAndBorrowedConcatVO> page, PersonnelSecondmentRequestRelation pageReqVO);
}