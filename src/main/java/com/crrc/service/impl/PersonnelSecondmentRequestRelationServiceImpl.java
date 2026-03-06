package com.crrc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.crrc.api.WkCoreAPi;
import com.crrc.api.WkWorkflowApi;
import com.crrc.common.PaginationGrid;
import com.crrc.constants.AuditConstants;
import com.crrc.entity.HrEmpBasic;
import com.crrc.entity.PersonnelSecondmentRequest;
import com.crrc.entity.PersonnelSecondmentRequestRelation;
import com.crrc.entity.dto.ProcessInstanceVo;
import com.crrc.entity.dto.StartProcessInstanceDTO;
import com.crrc.entity.dto.SysUserDTO;
import com.crrc.entity.dto.response.ResultData;
import com.crrc.entity.filedata.vo.FileDataStream;
import com.crrc.mapper.HrEmpBasicMapper;
import com.crrc.mapper.PersonnelSecondmentRequestMapper;
import com.crrc.mapper.PersonnelSecondmentRequestRelationMapper;
import com.crrc.pojo.ResponseResult;
import com.crrc.pojo.dto.WorkflowWriteBackParamDTO;
import com.crrc.pojo.vo.PersonnelSecondmentRequestRelationTemplateVO;
import com.crrc.pojo.vo.PersonnelSecondmentRequestTemplateVO;
import com.crrc.service.PersonnelSecondmentRequestRelationService;
import com.crrc.utils.DeptUtils;
import com.crrc.utils.WordTemplateProcessor;
import io.jsonwebtoken.lang.Objects;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 借调（借用）人员需求计划申请表表头 服务实现类
 * </p>
 *
 * @author mayiyao
 * @since 2026-03-04 16:26:51
 */
@Service
@Slf4j
public class PersonnelSecondmentRequestRelationServiceImpl extends WorkServiceImpl<PersonnelSecondmentRequestRelationMapper, PersonnelSecondmentRequestRelation> implements PersonnelSecondmentRequestRelationService {

    public static final String BORROW_TEMPLATE_ALIAS = "BORROW_AUDIT";
    @Autowired
    WkCoreAPi wkCoreAPi;
    @Autowired
    WkWorkflowApi wkWorkflowApi;
    @Autowired
    private HrEmpBasicMapper hrEmpBasicMapper;
    @Autowired
    private PersonnelSecondmentRequestMapper personnelSecondmentRequestMapper;
    @Autowired
    private FileServiceImpl fileService;

    @Value("${crrc.file.template.borrow.id}")
    private String templateFileId;
    @Value("${crrc.role.borrow.leader.id}")
    private long borrowLeaderRoleId;

    @Override
    public void resolveFields(List<PersonnelSecondmentRequestRelation> records) {
        Map<String, String> translateMap = records.stream().map(PersonnelSecondmentRequestRelation::getRequestingDepartment).distinct().collect(Collectors.toMap(
                k -> k,
                DeptUtils::getDeptNameByCode,
                (a, b) -> a
        ));

        records.forEach(item ->
            item.setRequestingDepartmentValue(
                translateMap.getOrDefault(item.getRequestingDepartment(), item.getRequestingDepartment())
            )
        );
    }

    @NotNull
    public ResponseResult<PaginationGrid<SysUserDTO>> findBorrowLeaderApproval(String id) {
        // 查询出当前需要审批的计划
        PersonnelSecondmentRequestRelation relation = getById(id);
        // 获得需求单位的编码
        String requestingDepartment = relation.getRequestingDepartment();

        List<SysUserDTO> roleUserListByRoleId = wkCoreAPi.getRoleUserListByRoleId(borrowLeaderRoleId);

        List<String> userCodes = roleUserListByRoleId.stream().map(SysUserDTO::getUserName).collect(Collectors.toList());

        LambdaQueryWrapper<HrEmpBasic> wrapper = new LambdaQueryWrapper<>();

        wrapper.in(HrEmpBasic::getUserCode, userCodes);

        List<HrEmpBasic> hrEmpBasicList = hrEmpBasicMapper.selectList(wrapper);

        Set<String> leaders = hrEmpBasicList.stream()
                .filter(emp -> Objects.nullSafeEquals(requestingDepartment, emp.getDept()))
                .map(HrEmpBasic::getUserCode)
                .collect(Collectors.toSet());

        List<SysUserDTO> sysUsers = roleUserListByRoleId.stream()
                .filter(user -> leaders.contains(user.getUserName()))
                .collect(Collectors.toList());

        PaginationGrid<SysUserDTO> result = new PaginationGrid<>();

        result.setRows(sysUsers);
        result.setTotal((long) sysUsers.size());

        return ResponseResult.success(result);
    }

    @Override
    public ResponseResult<Boolean> writeBack(WorkflowWriteBackParamDTO param) {
        PersonnelSecondmentRequestRelation relation = getById(param.getBizId());
        relation.setWfId(param.getWfId());
        relation.setProcessInstanceId(param.getProcessInstanceId());
        Integer processInstanceStatus = param.getProcessInstanceStatus();

        if (processInstanceStatus == 1) {
            // 进行中
            relation.setOrderStatus(AuditConstants.STATUS_AUDIT);
            updateById(relation);
        } else if (processInstanceStatus == 2) {
            // 已结束
            relation.setOrderStatus(AuditConstants.STATUS_AUDIT_PASS);
            updateById(relation);
        } else if (processInstanceStatus == 3) {
            // 已拒绝
            relation.setOrderStatus(AuditConstants.STATUS_REJECT);
            log.info("已拒绝");
            updateById(relation);
        } else if (processInstanceStatus == 4) {
            // 已重置
            log.info("已重置");
            LambdaUpdateWrapper<PersonnelSecondmentRequestRelation> wrapper = new LambdaUpdateWrapper<>();
            // 重置状态
            wrapper.set(PersonnelSecondmentRequestRelation::getOrderStatus, AuditConstants.STATUS_DRAFT);
            wrapper.set(PersonnelSecondmentRequestRelation::getProcessInstanceId, null);
            wrapper.eq(PersonnelSecondmentRequestRelation::getId, relation.getId());
            baseMapper.update(wrapper);
        }

        return new ResponseResult<>(HttpStatus.OK.value(), "OK", true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult<Boolean> submit(String id) {
        // 查出header
        PersonnelSecondmentRequestRelation relation = getById(id);

        // 4. 启动审批流程
        // 封装数据，提交审批流
        StartProcessInstanceDTO instanceDTO = new StartProcessInstanceDTO();
        instanceDTO.setBizCode(relation.getId());
        // 封装数据主键
        instanceDTO.setBizId(relation.getId());
        // 封装业务模板编码
        instanceDTO.setTemplateAlias(BORROW_TEMPLATE_ALIAS);
        // 封装业务表单数据
        Map<String, Object> formData = new HashMap<>();

        formData.put("id", id);

        instanceDTO.setFormData(formData);
        log.info("流程测试instanceDTO: {}", instanceDTO);
        // 返回流程实例id
        ResultData<ProcessInstanceVo> processInstanceVo = wkWorkflowApi.startProcessInstance(instanceDTO);
        log.info("流程测试processInstanceVo: {}", processInstanceVo);

        if(processInstanceVo == null || processInstanceVo.getResult() == null || processInstanceVo.getResult().getProcessInstanceId() == null) {
            throw new IllegalArgumentException(String.format("部门:%s 目前不存在审批人", relation.getRequestingDepartment()));
        }

        // 5. 更新流程实例ID
        relation.setProcessInstanceId(processInstanceVo.getResult().getProcessInstanceId());
        // 更新审批状态
        relation.setOrderStatus(AuditConstants.STATUS_AUDIT);

        // 生成协议书
        try {
            String fileId = generateRequestWord(id);
            relation.setFileId(fileId);
        } catch (Exception e) {
            throw new IllegalArgumentException("需求计划申请表生成出现错误");
        }

        saveOrUpdate(relation);
        log.info("流程测试success");
        return new ResponseResult<>(HttpStatus.OK.value(), "审批流发起成功", true);
    }

    public String generateRequestWord(@RequestParam String id) throws Exception {
        PersonnelSecondmentRequestRelation relation = getById(id);

        // 翻译部门
        resolveFields(Collections.singletonList(relation));

        PersonnelSecondmentRequestRelationTemplateVO relationVO = new PersonnelSecondmentRequestRelationTemplateVO();
        BeanUtils.copyProperties(relation, relationVO);
        // 需要单独把relation的翻译结果copy到relationVO中
        relationVO.setRequestingDepartment(relation.getRequestingDepartmentValue());

        LocalDate date = LocalDate.now();

        relationVO.setDay(String.valueOf(date.getDayOfMonth()));
        relationVO.setMonth(String.valueOf(date.getMonth().getValue()));
        relationVO.setYear(String.valueOf(date.getYear()));

        LambdaQueryWrapper<PersonnelSecondmentRequest> wrapper = new LambdaQueryWrapper<>();
        List<PersonnelSecondmentRequest> requestList = personnelSecondmentRequestMapper.selectList(wrapper);
        final int[] index = {1};
        List<PersonnelSecondmentRequestTemplateVO> requestVOList = requestList.stream().map(request -> {
            PersonnelSecondmentRequestTemplateVO vo = new PersonnelSecondmentRequestTemplateVO();
            BeanUtils.copyProperties(request, vo);

            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
            vo.setStartEndDate(String.format("%s至%s", request.getStartDate().format(dateTimeFormatter), request.getEndDate().format(dateTimeFormatter)));
            vo.setIndex(index[0]);
            index[0] = index[0] + 1;
            return vo;
        }).collect(Collectors.toList());

        FileDataStream fileStream = fileService.getFileStream(templateFileId);
        String fileId;
        try {
            MultipartFile docx = WordTemplateProcessor.processTemplate(fileStream.getStream(), relationVO, requestVOList);
            fileId = fileService.upload(docx);
        } catch (Exception e) {
            log.error("模板生成出现错误", e);
            throw e;
        }

        return fileId;
    }
}