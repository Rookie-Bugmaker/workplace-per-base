package com.crrc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.crrc.common.PaginationGrid;
import com.crrc.entity.PersonnelSecondmentRequestRelation;
import com.crrc.entity.dto.SysUserDTO;
import com.crrc.pojo.ResponseResult;
import com.crrc.pojo.dto.WorkflowWriteBackParamDTO;
import org.springframework.web.bind.annotation.RequestParam;

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
     * 查询需求单位用人领导
     * @param id 需要查询的审批计划id
     * @return 用人领导
     */
    ResponseResult<PaginationGrid<SysUserDTO>> findBorrowLeaderApproval(String id);

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
     * 生成需求计划表
     * @param id 主键
     * @return 生成后的fileId
     */
    String generateRequestWord(@RequestParam String id) throws Exception;
}