package com.crrc.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.annotation.AuditLog;
import com.crrc.annotation.TenantIgnore;
import com.crrc.annotation.TenantScope;
import com.crrc.common.PaginationGrid;
import com.crrc.entity.BorrowedPersonnelApprovalFormRelation;
import com.crrc.entity.dto.SysUserDTO;
import com.crrc.pojo.dto.BorrowedPersonnelApprovalFormRelationDetailDTO;
import com.crrc.entity.PersonnelSecondmentRequestRelation;
import com.crrc.enums.LogType;
import com.crrc.enums.OperateType;
import com.crrc.pojo.ResponseResult;
import com.crrc.pojo.dto.WorkflowWriteBackParamDTO;
import com.crrc.service.impl.BorrowedPersonnelApprovalFormRelationServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * <p>
 * 借调（借用）人员审批表 前端控制器
 * </p>
 *
 * @author mayiyao
 * @since 2026-03-04 16:26:51
 */
@RestController
@RequestMapping("/borrowedPersonnelApprovalFormRelation")
@RefreshScope
@Tag(name = "借调（借用）人员审批表", description = "借调（借用）人员审批表")
@TenantScope(filterRoleCode = {"DISPATCH_ADMIN", "QUOTA_ADMIN", "hrsc_deputy_minister"}, tenantLevel = TenantScope.TenantLevel.DEPT)
@Slf4j
public class BorrowedPersonnelApprovalFormRelationController {

    @Autowired
    private BorrowedPersonnelApprovalFormRelationServiceImpl borrowedPersonnelApprovalFormRelationService;

    /**
     * 人事令工作流回调接口
     */
    @TenantIgnore
    @Transactional
    @PutMapping("/write-back")
    public ResponseResult<Boolean> writeBack(@RequestBody WorkflowWriteBackParamDTO param) {
        return borrowedPersonnelApprovalFormRelationService.writeBack(param);
    }

    @TenantIgnore
    @GetMapping("/findBorrowingUnitSupervisorApproval")
    public ResponseResult<PaginationGrid<SysUserDTO>> findBorrowingUnitSupervisorApproval(
            Integer page, Integer pageSize,
            @RequestParam("id") String id
    ) {
        return ResponseResult.success(borrowedPersonnelApprovalFormRelationService.findBorrowingUnitSupervisorApproval(id));
    }

    @PostMapping("/submit")
    public ResponseResult<Boolean> submitApprovalFormRelation(@RequestParam("id") String id) {
        try {
            return borrowedPersonnelApprovalFormRelationService.submit(id);
        } catch (Exception e) {
            return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @PostMapping("/page")
    @Operation(summary = "获取借调（借用）人员审批表列表 page")
    @AuditLog(value = "获取借调（借用）人员审批表列表 page", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO",description = "包含查询条件的实体对象")
    @Parameter(name = "pageNo", description = "页码", example = "1")
    @Parameter(name = "pageSize", description = "条数", example = "10")
    public ResponseResult<IPage<PersonnelSecondmentRequestRelation>> pageBorrowedPersonnelApprovalFormRelation(
            @Validated @RequestBody(required = false) PersonnelSecondmentRequestRelation pageReqVO,
            @RequestParam(required = false, defaultValue = "1") int pageNo,
            @RequestParam(required = false, defaultValue = "10")int pageSize
    ) {
        pageReqVO = (pageReqVO == null) ? new PersonnelSecondmentRequestRelation() : pageReqVO;
        Page<PersonnelSecondmentRequestRelation> page = new Page<>(pageNo, pageSize);
        borrowedPersonnelApprovalFormRelationService.pageRequestBySourceDept(page, pageReqVO);
        return new ResponseResult<>(HttpStatus.OK.value(), "获取借调（借用）人员审批表列表 page成功", page);
    }

    @PostMapping("/query")
    @Operation(summary = "获取借调（借用）人员审批表列表")
    @AuditLog(value = "获取借调（借用）人员审批表列表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO", description = "包含查询条件的实体对象")
    public ResponseResult<List<BorrowedPersonnelApprovalFormRelation>> queryBorrowedPersonnelApprovalFormRelation(@Validated @RequestBody(required = false) BorrowedPersonnelApprovalFormRelation queryReqVO) {
        queryReqVO = (queryReqVO == null) ? new BorrowedPersonnelApprovalFormRelation() : queryReqVO;
        return new ResponseResult<>(HttpStatus.OK.value(), "获取借调（借用）人员审批表列表成功", borrowedPersonnelApprovalFormRelationService.query(queryReqVO));
	}

    @GetMapping("/get")
    @Operation(summary = "获得借调（借用）人员审批表实体")
    @AuditLog(value = "获得借调（借用）人员审批表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<BorrowedPersonnelApprovalFormRelation> getBorrowedPersonnelApprovalFormRelation(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "获取借调（借用）人员审批表实体成功", borrowedPersonnelApprovalFormRelationService.getById(id));
    }

    @Transactional
    @PostMapping("/saveDetails/{requestId}")
    @Operation(summary = "保存借调（借用）人员审批表实体")
    @AuditLog(value = "借调（借用）人员审批表实体保存", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_ADD)
    @Parameter(name = "reqVO", description = "实体对象", required = true)
    public ResponseResult<Boolean> saveBorrowedPersonnelApprovalFormRelation(@PathVariable String requestId, @Valid @RequestBody BorrowedPersonnelApprovalFormRelationDetailDTO reqDTO) {

        borrowedPersonnelApprovalFormRelationService.saveDetails(requestId, reqDTO);
    
        return new ResponseResult<>(HttpStatus.OK.value(), "保存借调（借用）人员审批表实体成功", true);
    }

    @Transactional
    @GetMapping("/delete")
    @Operation(summary = "删除借调（借用）人员审批表实体")
    @AuditLog(value = "删除借调（借用）人员审批表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_DELETE)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<Boolean> deleteBorrowedPersonnelApprovalFormRelation(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "删除借调（借用）人员审批表实体成功", borrowedPersonnelApprovalFormRelationService.removeById(id));
    }
}