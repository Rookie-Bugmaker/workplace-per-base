package com.crrc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.crrc.common.PaginationGrid;
import com.crrc.entity.BorrowedPersonnelApprovalFormRelation;
import com.crrc.entity.PersonnelSecondmentRequestRelation;
import com.crrc.entity.dto.SysUserDTO;
import com.crrc.pojo.ResponseResult;
import com.crrc.pojo.dto.BorrowedPersonnelApprovalFormRelationDetailDTO;
import com.crrc.pojo.dto.WorkflowWriteBackParamDTO;
import com.crrc.pojo.vo.RequestAndBorrowedConcatVO;

import javax.validation.Valid;

/**
 * <p>
 * 借调（借用）人员审批表 服务类
 * </p>
 *
 * @author mayiyao
 * @since 2026-03-04 16:26:51
 */
public interface BorrowedPersonnelApprovalFormRelationService extends IService<BorrowedPersonnelApprovalFormRelation> {

    /**
     * 发起审批
     * @param id 需要发起审批的表单id
     * @return 是否成功
     */
    ResponseResult<Boolean> submit(String id);

    /**
     * 通过部门page
     * @param page 分页信息
     * @param pageReqVO 查询条件
     */
    void pageRequestBySourceDept(Page<RequestAndBorrowedConcatVO> page, PersonnelSecondmentRequestRelation pageReqVO);

    /**
     * 保存，修改或删除借用人员审批表及其明细
     * @param requestId 关联的需求表id
     * @param reqDTO 审批表及其明细
     */
    void saveDetails(String requestId, @Valid BorrowedPersonnelApprovalFormRelationDetailDTO reqDTO);

    /**
     * 查询单位主管领导
     */
    PaginationGrid<SysUserDTO> findBorrowingUnitSupervisorApproval(String id);

    /**
     * 回调接口
     */
    ResponseResult<Boolean> writeBack(WorkflowWriteBackParamDTO param);

    /**
     * 查询定额管理员
     */
    PaginationGrid<SysUserDTO> findQuotaApproval();

    /**
     * 查询公司主管领导
     */
    PaginationGrid<SysUserDTO> findCompanyLeaderApproval(String id);

    /**
     * 根据主键获得详情
     */
    BorrowedPersonnelApprovalFormRelationDetailDTO getDetailsById(String id);
}