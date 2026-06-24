package com.crrc.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.annotation.AuditLog;
import com.crrc.annotation.TenantIgnore;
import com.crrc.entity.PositionDescription;
import com.crrc.enums.LogType;
import com.crrc.enums.OperateType;
import com.crrc.pojo.ResponseResult;
import com.crrc.pojo.dto.PositionDescriptionAuditDTO;
import com.crrc.pojo.dto.PositionDescriptionSaveDTO;
import com.crrc.pojo.dto.WorkflowWriteBackParamDTO;
import com.crrc.pojo.vo.PositionDescriptionDetailVO;
import com.crrc.pojo.vo.PositionDescriptionWorkflowRoleVO;
import com.crrc.pojo.vo.workflow.ProcessRunningInfoVo;
import com.crrc.service.PositionDescriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 职位说明书管理前端控制器。
 */
@RestController
@RequestMapping("/positionDescription")
@RefreshScope
@TenantIgnore
@Tag(name = "职位说明书管理", description = "职位说明书管理")
public class PositionDescriptionController {

    @Autowired
    private PositionDescriptionService positionDescriptionService;

    @PostMapping("/page")
    @Operation(summary = "职位说明书分页查询")
    @AuditLog(value = "职位说明书分页查询", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO", description = "包含查询条件的职位说明书对象", required = false)
    public ResponseResult<IPage<PositionDescription>> page(@Validated @RequestBody(required = false) PositionDescription pageReqVO,
                                                           @RequestParam(required = false, defaultValue = "1") int pageNo,
                                                           @RequestParam(required = false, defaultValue = "10") int pageSize) {
        Page<PositionDescription> page = new Page<>(pageNo, pageSize);
        positionDescriptionService.queryPage(page, pageReqVO);
        return new ResponseResult<>(HttpStatus.OK.value(), "职位说明书分页查询成功", page);
    }

    @PostMapping("/query")
    @Operation(summary = "职位说明书列表查询")
    @AuditLog(value = "职位说明书列表查询", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    public ResponseResult<List<PositionDescription>> query(@Validated @RequestBody(required = false) PositionDescription queryReqVO) {
        return new ResponseResult<>(HttpStatus.OK.value(), "职位说明书列表查询成功", positionDescriptionService.queryList(queryReqVO));
    }

    @GetMapping("/get")
    @Operation(summary = "职位说明书详情查询")
    @AuditLog(value = "职位说明书详情查询", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    public ResponseResult<PositionDescriptionDetailVO> get(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "职位说明书详情查询成功", positionDescriptionService.getDetail(id));
    }

    @GetMapping("/getLatestByPositionCode")
    @Operation(summary = "按职位编码查询当前说明书")
    @AuditLog(value = "按职位编码查询当前说明书", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    public ResponseResult<PositionDescriptionDetailVO> getLatestByPositionCode(@RequestParam("positionCode") String positionCode) {
        return new ResponseResult<>(HttpStatus.OK.value(), "按职位编码查询当前说明书成功", positionDescriptionService.getLatestByPositionCode(positionCode));
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/saveDraft")
    @Operation(summary = "保存职位说明书草稿")
    @AuditLog(value = "保存职位说明书草稿", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_MODIFY)
    public ResponseResult<PositionDescriptionDetailVO> saveDraft(@Valid @RequestBody PositionDescriptionSaveDTO request) {
        try {
            return new ResponseResult<>(HttpStatus.OK.value(), "职位说明书草稿保存成功", positionDescriptionService.saveDraft(request));
        } catch (IllegalArgumentException e) {
            return ResponseResult.fail(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/submit")
    @Operation(summary = "提交职位说明书审批")
    @AuditLog(value = "提交职位说明书审批", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_MODIFY)
    public ResponseResult<PositionDescription> submit(@RequestParam("id") Long id) {
        try {
            return new ResponseResult<>(HttpStatus.OK.value(), "职位说明书提交审批成功", positionDescriptionService.submit(id));
        } catch (IllegalArgumentException e) {
            return ResponseResult.fail(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/approve")
    @Operation(summary = "职位说明书审批通过")
    @AuditLog(value = "职位说明书审批通过", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_MODIFY)
    public ResponseResult<Boolean> approve(@RequestBody PositionDescriptionAuditDTO request) {
        try {
            return new ResponseResult<>(HttpStatus.OK.value(), "职位说明书审批通过成功", positionDescriptionService.approve(request));
        } catch (IllegalArgumentException e) {
            return ResponseResult.fail(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/reject")
    @Operation(summary = "职位说明书审批驳回")
    @AuditLog(value = "职位说明书审批驳回", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_MODIFY)
    public ResponseResult<Boolean> reject(@RequestBody PositionDescriptionAuditDTO request) {
        try {
            return new ResponseResult<>(HttpStatus.OK.value(), "职位说明书审批驳回成功", positionDescriptionService.reject(request));
        } catch (IllegalArgumentException e) {
            return ResponseResult.fail(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @PutMapping("/approval-callback")
    @Operation(summary = "职位说明书Flowable回调")
    @AuditLog(value = "职位说明书Flowable回调", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_MODIFY)
    public ResponseResult<Boolean> approvalCallback(@RequestBody WorkflowWriteBackParamDTO param) {
        try {
            return new ResponseResult<>(HttpStatus.OK.value(), "职位说明书Flowable回调处理成功", positionDescriptionService.workflowCallback(param));
        } catch (IllegalArgumentException e) {
            return ResponseResult.fail(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/publish")
    @Operation(summary = "发布职位说明书")
    @AuditLog(value = "发布职位说明书", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_MODIFY)
    public ResponseResult<Boolean> publish(@RequestParam("id") Long id) {
        try {
            return new ResponseResult<>(HttpStatus.OK.value(), "职位说明书发布成功", positionDescriptionService.publish(id));
        } catch (IllegalArgumentException e) {
            return ResponseResult.fail(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/disable")
    @Operation(summary = "停用职位说明书")
    @AuditLog(value = "停用职位说明书", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_MODIFY)
    public ResponseResult<Boolean> disable(@RequestParam("id") Long id) {
        try {
            return new ResponseResult<>(HttpStatus.OK.value(), "职位说明书停用成功", positionDescriptionService.disable(id));
        } catch (IllegalArgumentException e) {
            return ResponseResult.fail(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @GetMapping("/processTrace")
    @Operation(summary = "职位说明书审批流程跟踪")
    @AuditLog(value = "职位说明书审批流程跟踪", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    public ResponseResult<ProcessRunningInfoVo> processTrace(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "职位说明书审批流程跟踪查询成功", positionDescriptionService.runningSequenceGraph(id));
    }

    @GetMapping("/workflowRoles")
    @Operation(summary = "职位说明书审批角色配置查询")
    @AuditLog(value = "职位说明书审批角色配置查询", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    public ResponseResult<List<PositionDescriptionWorkflowRoleVO>> workflowRoles() {
        return new ResponseResult<>(HttpStatus.OK.value(), "职位说明书审批角色配置查询成功", positionDescriptionService.workflowRoles());
    }
}
