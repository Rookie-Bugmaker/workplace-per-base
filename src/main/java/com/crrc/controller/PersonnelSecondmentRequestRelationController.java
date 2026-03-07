package com.crrc.controller;


import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.annotation.AuditLog;
import com.crrc.annotation.TenantIgnore;
import com.crrc.annotation.TenantScope;
import com.crrc.common.PaginationGrid;
import com.crrc.constants.AuditConstants;
import com.crrc.entity.PersonnelSecondmentRequest;
import com.crrc.entity.PersonnelSecondmentRequestRelation;
import com.crrc.entity.dto.SysUserDTO;
import com.crrc.enums.LogType;
import com.crrc.enums.OperateType;
import com.crrc.pojo.ResponseResult;
import com.crrc.pojo.dto.PersonnelSecondmentRequestRelationDetailDTO;
import com.crrc.pojo.dto.WorkflowWriteBackParamDTO;
import com.crrc.service.impl.PersonnelSecondmentRequestRelationServiceImpl;
import com.crrc.service.impl.PersonnelSecondmentRequestServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 借调（借用）人员需求计划申请表表头 前端控制器
 * </p>
 *
 * @author mayiyao
 * @since 2026-03-04 16:26:51
 */
@RestController
@RequestMapping("/personnelSecondmentRequestRelation")
@RefreshScope
@Tag(name = "借调（借用）人员需求计划申请表表头", description = "借调（借用）人员需求计划申请表表头")
@TenantScope(filterRoleCode = {"DISPATCH_ADMIN", "QUOTA_ADMIN", "hrsc_deputy_minister"}, tenantLevel = TenantScope.TenantLevel.DEPT)
public class PersonnelSecondmentRequestRelationController {

    @Autowired
    private PersonnelSecondmentRequestRelationServiceImpl personnelSecondmentRequestRelationService;
    @Autowired
    private PersonnelSecondmentRequestServiceImpl personnelSecondmentRequestService;

    @TenantIgnore
    @GetMapping("/findBorrowLeaderApproval")
    public ResponseResult<PaginationGrid<SysUserDTO>> findBorrowLeaderApproval(
            Integer page, Integer pageSize,
            @RequestParam("id") String id
    ) {
        return personnelSecondmentRequestRelationService.findBorrowLeaderApproval(id);
    }

    @TenantIgnore
    @GetMapping("/findHrAuditApproval")
    public ResponseResult<PaginationGrid<SysUserDTO>> findHrAuditApproval(
            Integer page, Integer pageSize
    ) {
        return personnelSecondmentRequestRelationService.findHrAuditApproval();
    }

    @TenantIgnore
    @GetMapping("/findHrDeputyMinisterApproval")
    public ResponseResult<PaginationGrid<SysUserDTO>> findHrDeputyMinisterApproval(
            Integer page, Integer pageSize
    ) {
        return personnelSecondmentRequestRelationService.findHrDeputyMinisterApproval();
    }

    @TenantIgnore
    @PostMapping("/sendToSourceDept/{id}")
    public ResponseResult<Boolean> sendToSourceDept(@PathVariable("id") String id, @RequestBody List<String> deptList) {
        return ResponseResult.success(personnelSecondmentRequestRelationService.sendToSourceDept(id, deptList));
    }

    @PostMapping("/submit")
    public ResponseResult<Boolean> submitRequestRelation(@RequestParam("id") String id) {
        try {
            return personnelSecondmentRequestRelationService.submit(id);
        } catch (Exception e) {
            return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    /**
     * 人事令工作流回调接口
     */
    @TenantIgnore
    @Transactional
    @PutMapping("/write-back")
    public ResponseResult<Boolean> writeBack(@RequestBody WorkflowWriteBackParamDTO param) {
        return personnelSecondmentRequestRelationService.writeBack(param);
    }

    @PostMapping("/page")
    @Operation(summary = "获取借调（借用）人员需求计划申请表表头列表 page")
    @AuditLog(value = "获取借调（借用）人员需求计划申请表表头列表 page", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO",description = "包含查询条件的实体对象", required = false)
    @Parameter(name = "pageNo", description = "页码", required = false, example = "1")
    @Parameter(name = "pageSize", description = "条数", required = false, example = "10")
    public ResponseResult<IPage<PersonnelSecondmentRequestRelation>> pagePersonnelSecondmentRequestRelation(@Validated @RequestBody(required = false) PersonnelSecondmentRequestRelation pageReqVO,
                                             @RequestParam(required = false, defaultValue = "1") int pageNo,
                                             @RequestParam(required = false, defaultValue = "10")int pageSize) {
        pageReqVO = (pageReqVO == null) ? new PersonnelSecondmentRequestRelation() : pageReqVO;
        Page<PersonnelSecondmentRequestRelation> page = new Page<>(pageNo, pageSize);
        personnelSecondmentRequestRelationService.query(page, pageReqVO);
        personnelSecondmentRequestRelationService.resolveFields(page.getRecords());
        return new ResponseResult<>(HttpStatus.OK.value(), "获取借调（借用）人员需求计划申请表列表 page成功", page);
    }

    @PostMapping("/query")
    @Operation(summary = "获取借调（借用）人员需求计划申请表表头列表")
    @AuditLog(value = "获取借调（借用）人员需求计划申请表表头列表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO", description = "包含查询条件的实体对象")
    public ResponseResult<List<PersonnelSecondmentRequestRelation>> queryPersonnelSecondmentRequestRelation(@Validated @RequestBody(required = false) PersonnelSecondmentRequestRelation queryReqVO) {
        queryReqVO = (queryReqVO == null) ? new PersonnelSecondmentRequestRelation() : queryReqVO;
        return new ResponseResult<>(HttpStatus.OK.value(), "获取借调（借用）人员需求计划申请表列表成功", personnelSecondmentRequestRelationService.query(queryReqVO));
	}

    @GetMapping("/get")
    @Operation(summary = "获得借调（借用）人员需求计划申请表表头实体")
    @AuditLog(value = "获得借调（借用）人员需求计划申请表表头实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<PersonnelSecondmentRequestRelationDetailDTO> getPersonnelSecondmentRequestRelation(@RequestParam("id") String id) {
        PersonnelSecondmentRequestRelation relation = personnelSecondmentRequestRelationService.getById(id);
        personnelSecondmentRequestRelationService.resolveFields(Collections.singletonList(relation));
        List<PersonnelSecondmentRequest> requestList = personnelSecondmentRequestService.getByRelationId(id);
        PersonnelSecondmentRequestRelationDetailDTO detailDTO = new PersonnelSecondmentRequestRelationDetailDTO();
        detailDTO.setRelation(relation);
        detailDTO.setRequestList(requestList);
        return new ResponseResult<>(HttpStatus.OK.value(), "获取借调（借用）人员需求计划申请表成功", detailDTO);
    }

    @Transactional
    @PostMapping("/saveDetail")
    @Operation(summary = "保存借调（借用）人员需求计划申请明细表实体")
    @AuditLog(value = "借调（借用）人员需求计划申请明细表实体保存", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_ADD)
    @Parameter(name = "reqVO", description = "实体对象", required = true)
    public ResponseResult<Boolean> savePersonnelSecondmentRequest(@Valid @RequestBody PersonnelSecondmentRequestRelationDetailDTO requestDTO) {

        PersonnelSecondmentRequestRelation relation = requestDTO.getRelation();

        if(!StringUtils.hasText(relation.getId())) {
            relation.setId(IdUtil.randomUUID());
            relation.setOrderStatus(AuditConstants.STATUS_DRAFT);
        }

        personnelSecondmentRequestRelationService.saveOrUpdate(relation);

        List<PersonnelSecondmentRequest> reqVOList = requestDTO.getRequestList();

        reqVOList.forEach(
            item -> {
                if(!StringUtils.hasText(item.getId())) {
                    item.setId(IdUtil.randomUUID());
                    item.setRelationId(relation.getId());
                }
            }
        );

        boolean result = personnelSecondmentRequestService.saveDetails(relation.getId(), reqVOList);

        return new ResponseResult<>(HttpStatus.OK.value(), "保存借调（借用）人员需求计划申请明细表实体成功", result);
    }


    @Transactional
    @GetMapping("/delete")
    @Operation(summary = "删除借调（借用）人员需求计划申请表表头实体")
    @AuditLog(value = "删除借调（借用）人员需求计划申请表表头实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_DELETE)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<Boolean> deletePersonnelSecondmentRequestRelation(@RequestParam("id") String id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "删除借调（借用）人员需求计划申请表成功", personnelSecondmentRequestRelationService.removeById(id));
    }
}
