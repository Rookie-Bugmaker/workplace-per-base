package com.crrc.controller;


import cn.hutool.core.util.IdUtil;
import com.alibaba.nacos.client.naming.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.annotation.AuditLog;
import com.crrc.entity.ExceptionCasePerson;
import com.crrc.annotation.IgnoreTenant;
import com.crrc.client.SysServerClient;
import com.crrc.common.ResultData;
import com.crrc.constants.AuditConstants;
import com.crrc.entity.ExceptionCasePersonRelation;
import com.crrc.enums.LogType;
import com.crrc.enums.OperateType;
import com.crrc.pojo.ResponseResult;
import com.crrc.pojo.dto.ExceptionCasePersonDTO;
import com.crrc.pojo.vo.ExceptionCasePersonRelationQueryVO;
import com.crrc.pojo.vo.ExceptionCasePersonVO;
import com.crrc.pojo.dto.SysUserDTO;
import com.crrc.pojo.dto.WorkflowUserPageDTO;
import com.crrc.pojo.dto.WorkflowWriteBackParamDTO;
import com.crrc.service.impl.ExceptionCasePersonRelationServiceImpl;
import com.crrc.service.impl.ExceptionCasePersonServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 额外人员信息关联表 前端控制器
 * </p>
 *
 * @author mayiyao
 * @since 2026-01-15 13:44:29
 */
@RestController
@RequestMapping("/exceptionCasePersonRelation")
@RefreshScope
@Tag(name = "额外人员信息关联表", description = "额外人员信息关联表")
@Slf4j
public class ExceptionCasePersonRelationController {

    @Autowired
    private ExceptionCasePersonRelationServiceImpl exceptionCasePersonRelationService;
    @Qualifier("com.crrc.client.SysServerClient")
    @Autowired
    private SysServerClient sysServerClient;
    @Autowired
    private ExceptionCasePersonServiceImpl exceptionCasePersonService;

    /**
     * 工作流回写
     *
     * @param param 参数
     */
    @PutMapping("/write-back")
    @IgnoreTenant
    public ResponseResult<Boolean> writeBack(@RequestBody WorkflowWriteBackParamDTO param) {
        ExceptionCasePersonRelation po = new ExceptionCasePersonRelation();
        po.setId(param.getBizId());
        po.setWfid(param.getWfId());
        po.setProcessInstanceId(param.getProcessInstanceId());
        Integer processInstanceStatus = param.getProcessInstanceStatus();

        if (processInstanceStatus == 1) {
            // 进行中
            po.setStatus(AuditConstants.STATUS_AUDIT);
        } else if (processInstanceStatus == 2) {
            // 已结束
            po.setStatus(AuditConstants.STATUS_AUDIT_PASS);

        } else if (processInstanceStatus == 3) {
            // 已拒绝
            po.setStatus(AuditConstants.STATUS_REJECT);
            log.info("已拒绝");
        } else if (processInstanceStatus == 4) {
            // 已重置
            log.info("已重置");
            po.setStatus(AuditConstants.STATUS_DRAFT);
            po.setProcessInstanceId(null);
        }
        boolean result = exceptionCasePersonRelationService.updateById(po);
        if (!result) {
            log.error("工作流回写失败");
            return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "回写失败", false);
        }
        return new ResponseResult<>(HttpStatus.OK.value(), "回写成功", true);
    }

    @GetMapping("/findApproval")
    public ResponseResult<WorkflowUserPageDTO> findApproval(Integer page, Integer pageSize) {
        try {
            WorkflowUserPageDTO workflowUserPage = new WorkflowUserPageDTO();
            if (ObjectUtils.isEmpty(page) || ObjectUtils.isEmpty(pageSize)) {
                //不需要分页查询
                page = 1;
                pageSize = 0;
            }
            //根据agreementCode查询部门人员列表
            List<SysUserDTO> rows = new ArrayList<>();

            ResultData<List<SysUserDTO>> userByRoleId = sysServerClient.findUserByRoleId(424928747026169856L);

            if(userByRoleId == null || ObjectUtils.isEmpty(userByRoleId.getResult())) {
                return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "角色下用户为空");
            }

            rows.addAll(userByRoleId.getResult());

            workflowUserPage.setTotal((long) rows.size());
            workflowUserPage.setRows(rows);

            return ResponseResult.success(workflowUserPage);
        } catch (Exception e) {
            log.error("审批人查询错误", e);
            return ResponseResult.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), "ERROR");
        }
    }

    @PostMapping("/getFull")
    @Operation(summary = "获得额外人员信息关联表实体")
    @AuditLog(value = "获得额外人员信息关联表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    public ResponseResult<ExceptionCasePersonVO> getFullDetails(@Validated @RequestBody ExceptionCasePersonRelation personRelation,
                                                                       @RequestParam(required = false, defaultValue = "1") int pageNo,
                                                                       @RequestParam(required = false, defaultValue = "10")int pageSize) {
        try {
            // 传入的是表头的id，表头的id会和ExceptionCasePerson中的id关联
            if(ObjectUtils.isNotEmpty(personRelation) && StringUtils.isNotBlank(personRelation.getId())){
                // 查出表头
                ExceptionCasePersonRelation exceptionCasePersonRelation = exceptionCasePersonRelationService.getById(personRelation.getId());

                // 查出所有下面的明细
                List<ExceptionCasePerson> exceptionCasePeople = exceptionCasePersonService.selectByRelationId(exceptionCasePersonRelation.getId());
                ExceptionCasePersonVO exceptionCasePersonVO = new ExceptionCasePersonVO();
                exceptionCasePersonVO.setPersonList(exceptionCasePeople);
                exceptionCasePersonVO.setRelation(exceptionCasePersonRelation);
                return new ResponseResult<>(HttpStatus.OK.value(), "获取额外人员信息成功", exceptionCasePersonVO);

            } else {
                return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "未传入有效的表头id");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseResult<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "查询异常");
        }
    }

    @PostMapping("/getPersonRelationInfo")
    @Operation(summary = "获得额外人员信息关联表实体")
    @AuditLog(value = "获得额外人员信息关联表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    public ResponseResult<ExceptionCasePersonVO> getPersonRelationInfo(@Validated @RequestBody ExceptionCasePersonRelation personRelation,
                                                            @RequestParam(required = false, defaultValue = "1") int pageNo,
                                                            @RequestParam(required = false, defaultValue = "10")int pageSize) {
        try {
            // 传入的是表头的id，表头的id会和ExceptionCasePerson中的id关联
            if(ObjectUtils.isNotEmpty(personRelation) && StringUtils.isNotBlank(personRelation.getId())){
                // 查出表头
                ExceptionCasePersonRelation exceptionCasePersonRelation = exceptionCasePersonRelationService.getById(personRelation.getId());

                // 关联人员进行分页处理
                Page<ExceptionCasePerson> page = new Page<>(pageNo, pageSize);
                ExceptionCasePerson pageReqVO = new ExceptionCasePerson();
                pageReqVO.setRelationId(personRelation.getId());
                exceptionCasePersonService.query(page, pageReqVO);

                // 构造返回结果
                ExceptionCasePersonVO result = new ExceptionCasePersonVO();
                result.setRelation(exceptionCasePersonRelation);
                result.setPersonPage(page);

                return new ResponseResult<>(HttpStatus.OK.value(), "获取额外人员信息成功", result);

            } else {
                return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "未传入有效的表头id");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseResult<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "查询异常");
        }
    }


    @PostMapping("/saveOrUpdate")
    @Operation(summary = "保存或更新额外人员信息关联表实体")
    @AuditLog(value = "保存或更新额外人员信息关联表实体",
            logType = LogType.TYPE_OPERATE,
            operateType = OperateType.TYPE_ADD)
    @Transactional
    public ResponseResult<String> saveOrUpdatePersonInfo(@Validated @RequestBody ExceptionCasePersonDTO personDTO) {
        if (ObjectUtils.isEmpty(personDTO.getRelation())) {
            return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "未传入有效的表头信息");
        }
        if (CollectionUtils.isEmpty(personDTO.getPersonList())) {
            return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "人员列表不能为空");
        }
        try {
            // 处理表头ID
            String relationId = StringUtils.isNotBlank(personDTO.getRelation().getId())
                    ? personDTO.getRelation().getId()
                    : IdUtil.simpleUUID();
            personDTO.getRelation().setId(relationId);

            // 保存/更新表头
            exceptionCasePersonRelationService.saveOrUpdate(personDTO.getRelation());

            // 获取已存在的人员ID集合
            Set<String> existIds = exceptionCasePersonService.selectByRelationId(relationId)
                    .stream()
                    .map(ExceptionCasePerson::getId)
                    .collect(Collectors.toSet());

            // 处理人员列表
            personDTO.getPersonList().forEach(person -> {
                if (StringUtils.isBlank(person.getId())) {
                    person.setId(IdUtil.simpleUUID());
                } else {
                    existIds.remove(person.getId());
                }
                if (StringUtils.isBlank(person.getRelationId())) {
                    person.setRelationId(relationId);
                }
            });

            // 批量保存人员
            exceptionCasePersonService.saveOrUpdateBatch(personDTO.getPersonList());

            // 批量删除不再关联的人员
            if (!existIds.isEmpty()) {
                exceptionCasePersonService.removeByIds(existIds);
            }
            return new ResponseResult<>(HttpStatus.OK.value(), "保存额外人员信息成功");

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "保存失败", e.getMessage());
        }
    }


    @PostMapping("/page")
    @Operation(summary = "获取额外人员信息关联表列表 page")
    @AuditLog(value = "获取额外人员信息关联表列表 page", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO",description = "包含查询条件的实体对象", required = false)
    @Parameter(name = "pageNo", description = "页码", required = false, example = "1")
    @Parameter(name = "pageSize", description = "条数", required = false, example = "10")
    public ResponseResult<IPage> pageExceptionCasePersonRelation(@Validated @RequestBody(required = false) ExceptionCasePersonRelationQueryVO queryVO,
                                             @RequestParam(required = false, defaultValue = "1") int pageNo,
                                             @RequestParam(required = false, defaultValue = "10")int pageSize) {
        queryVO = (queryVO == null) ? new ExceptionCasePersonRelationQueryVO() : queryVO;
        Page<ExceptionCasePersonRelation> page = new Page<>(pageNo, pageSize);
        exceptionCasePersonRelationService.queryByVO(page, queryVO);
        return new ResponseResult<>(HttpStatus.OK.value(), "获取额外人员信息关联表列表 page成功", page);
    }

    @PostMapping("/query")
    @Operation(summary = "获取额外人员信息关联表列表")
    @AuditLog(value = "获取额外人员信息关联表列表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO", description = "包含查询条件的实体对象", required = false)
    public ResponseResult<List<ExceptionCasePersonRelation>> queryExceptionCasePersonRelation(@Validated @RequestBody(required = false) ExceptionCasePersonRelation queryReqVO) {
        queryReqVO = (queryReqVO == null) ? new ExceptionCasePersonRelation() : queryReqVO;
        return new ResponseResult<>(HttpStatus.OK.value(), "获取额外人员信息关联表列表成功", exceptionCasePersonRelationService.query(queryReqVO));
	}

    @GetMapping("/get")
    @Operation(summary = "获得额外人员信息关联表实体")
    @AuditLog(value = "获得额外人员信息关联表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<ExceptionCasePersonRelation> getExceptionCasePersonRelation(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "获取额外人员信息关联表实体成功", exceptionCasePersonRelationService.getById(id));
    }

    @Transactional
    @PostMapping("/create")
    @Operation(summary = "创建额外人员信息关联表实体")
    @AuditLog(value = "创建额外人员信息关联表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_ADD)
    @Parameter(name = "createReqVO", description = "需要创建的实体对象", required = true)
    public ResponseResult<Boolean> createExceptionCasePersonRelation(@Valid @RequestBody ExceptionCasePersonRelation createReqVO) {
        return new ResponseResult<>(HttpStatus.OK.value(), "创建额外人员信息关联表实体成功", exceptionCasePersonRelationService.save(createReqVO));
    }

    @Transactional
    @PostMapping("/update")
    @Operation(summary = "更新额外人员信息关联表实体")
    @AuditLog(value = "更新额外人员信息关联表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_MODIFY)
    @Parameter(name = "createReqVO", description = "需要更新的实体对象", required = true)
    public ResponseResult<Boolean> updateExceptionCasePersonRelation(@Valid @RequestBody ExceptionCasePersonRelation updateReqVO) {
        return new ResponseResult<>(HttpStatus.OK.value(), "更新额外人员信息关联表实体成功", exceptionCasePersonRelationService.saveOrUpdate(updateReqVO));
    }

    @Transactional
    @PostMapping("/delete")
    @Operation(summary = "删除额外人员信息关联表实体")
    @AuditLog(value = "删除额外人员信息关联表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_DELETE)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<Boolean> deleteExceptionCasePersonRelation(@RequestBody List<String> ids) {
        return new ResponseResult<>(HttpStatus.OK.value(), "删除额外人员信息关联表实体成功", exceptionCasePersonRelationService.removeBatchByIds(ids));
    }
    
    @PostMapping("/export")
    @AuditLog(value = "导出额外人员信息关联表报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_EXPORT)
    @Operation(summary = "导出额外人员信息关联表报表")
    public void exportExceptionCasePersonRelation(@Validated @RequestBody(required = false) ExceptionCasePersonRelation exportReqVO,
                              @RequestParam(required = false, defaultValue = "xlsx") String type,
                           HttpServletResponse response) throws IOException {
        exceptionCasePersonRelationService.exports(exportReqVO, response);
    }

    @PostMapping("/import")
    @AuditLog(value = "导入额外人员信息关联表报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_IMPORT)
    @Operation(summary = "导入额外人员信息关联表报表")
    public ResponseResult<Boolean> imports(MultipartFile file) throws IOException {
    	// imports方法会导入保存数据到数据库，reads方法会从文件读取数据但不保存
        List<ExceptionCasePersonRelation> importReqVO = exceptionCasePersonRelationService.imports(file);
        return new ResponseResult<>(HttpStatus.OK.value(), "导入额外人员信息关联表报表成功", true);
    }

}
