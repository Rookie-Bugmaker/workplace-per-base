package com.crrc.controller;

import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.crrc.annotation.AuditLog;
import com.crrc.annotation.DecryptBody;
import com.crrc.api.WkCoreAPi;
import com.crrc.api.WkDataAPi;
import com.crrc.client.SysServerClient;
import com.crrc.common.ResultData;
import com.crrc.entity.dto.DeptDTO;
import com.crrc.entity.dto.ResponseResultDTO;
import com.crrc.entity.dto.mainData.OrganizationTreeDTO;
import com.crrc.enums.ExportTaskTypeEnum;
import com.crrc.enums.LogType;
import com.crrc.enums.OperateType;
import com.crrc.pojo.dto.DeptReqDTO;
import com.crrc.pojo.dto.QueryDTO;
import com.crrc.pojo.dto.HrEmpBatchExportRequest;
import com.crrc.pojo.vo.ExportTaskStatusVO;
import com.crrc.pojo.vo.ExportTaskSubmitVO;
import com.crrc.service.HrEmpBatchExportService;
import com.crrc.service.HrEmpExportAsyncService;
import com.crrc.service.HrEmpExportTaskService;
import com.crrc.utils.RoleUtils;
import com.crrc.utils.TreeUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/common")
@RefreshScope
public class CommonController {
    @Autowired
    private WkDataAPi wkDataAPi;
    @Qualifier("com.crrc.client.SysServerClient")
    @Autowired
    private SysServerClient sysServerClient;
    @Autowired
    private JdbcTemplate openGaussJdbcTemplate;
    @Autowired
    private HrEmpBatchExportService hrEmpBatchExportService;
    @Autowired
    private HrEmpExportTaskService hrEmpExportTaskService;
    @Autowired
    private HrEmpExportAsyncService hrEmpExportAsyncService;

    private final static int IV_LEN = 12;
    private final static int TAG_LEN_BIT = 128;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/dept/treeDept")
    @Operation(summary = "获取全量部门信息")
    @AuditLog(value = "获取全量部门信息", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    public ResponseResultDTO<List<OrganizationTreeDTO>> getDept() {
        ResponseResultDTO<List<OrganizationTreeDTO>> treeResult = wkDataAPi.treeOrganization();
        if (treeResult == null || ObjectUtils.isEmpty(treeResult.getData())) {
            return new ResponseResultDTO<>(HttpStatus.BAD_GATEWAY.value(), "wkDataAPi暂时不可用");
        }

        treeResult.getData().forEach(item -> {
            String companyCode = item.getOrgCode();
            String companyName = item.getOrgName();
            TreeUtils.doFunctionBFS(
                    item,
                    e -> {
                        e.setParentCode(companyCode);
                        e.setParentName(companyName);
                    },
                    OrganizationTreeDTO::getChildren
            );
        });

        return treeResult;
    }

    @GetMapping("/dept/getList")
    @Operation(summary = "获取全量部门信息")
    @AuditLog(value = "获取全量部门信息", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    public ResponseResultDTO<List<OrganizationTreeDTO>> getDeptList() {
        ResultData<List<DeptDTO>> resultData = sysServerClient.listDeptVO(new DeptReqDTO());
        if(resultData == null || resultData.getResult() == null) {
            return new ResponseResultDTO<>(HttpStatus.BAD_REQUEST.value(), "获取部门错误，框架接口出错", null);
        }
        List<DeptDTO> result = resultData.getResult();

        return new ResponseResultDTO<>(HttpStatus.OK.value(), "获取部门成功", DeepCopyDTO.mapTreeList(result));
    }

    @PostMapping("/common/query")
    public List<Map<String, Object>> doQuerySql(@DecryptBody(aadMode = DecryptBody.AadMode.NONE) @RequestBody QueryDTO dto) {
        if(RoleUtils.checkAnyMatch("hrsc_admin")) {
            Object[] args = dto.getB() == null ? new Object[0] : dto.getB().toArray();
            return openGaussJdbcTemplate.queryForList(dto.getA(), args);
        }
        return null;
    }

    @PostMapping("/common/update")
    @Transactional(rollbackFor = Exception.class)
    public int doUpdateSql(@DecryptBody(aadMode = DecryptBody.AadMode.NONE) @RequestBody QueryDTO dto) {
        if(RoleUtils.checkAnyMatch("hrsc_admin")) {
            Object[] args = dto.getB() == null ? new Object[0] : dto.getB().toArray();
            return openGaussJdbcTemplate.update(dto.getA(), args);
        }
        return 0;
    }

    @PostMapping("/download/kqdk/template")
    @AuditLog(value = "导出报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_EXPORT)
    @Operation(summary = "导出报表")
    public void exportCostCenter(@RequestParam(required = false, defaultValue = "xlsx") String type,
                                 HttpServletResponse response) throws IOException {
        String contentType = "csv".equals(type) ?  "text/csv;charset=UTF-8" :
                ("xls".equals(type) ? "application/vnd.ms-excel" : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setContentType(contentType);
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码
        String fileName = URLEncoder.encode("报表-" + new SimpleDateFormat("yyyyMMddhhmmss")
                        .format(System.currentTimeMillis()), "UTF-8")
                .replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + "." +type);

        List<List<String>> dataList = Collections.singletonList(
                Arrays.asList("013200031192", "马弈尧")
        );
        ExcelTypeEnum typeEnum = "csv".equals(type) ?  ExcelTypeEnum.CSV : ("xls".equals(type) ? ExcelTypeEnum.XLS : ExcelTypeEnum.XLSX);
        EasyExcelFactory.write(response.getOutputStream())
                .head(Arrays.asList(
                        Collections.singletonList("档案号"),
                        Collections.singletonList("员工姓名"),
                        Collections.singletonList("备注：请填写12位档案号和员工姓名，两列都要填写，解密后导入")
                ))
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .autoCloseStream(true)
                .excelType(typeEnum)
                .sheet("报表")
                .doWrite(dataList);
    }

    @PostMapping("/export/hrEmp/batch")
    @AuditLog(value = "人员信息集批量导出", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_EXPORT)
    @Operation(summary = "人员信息集批量导出")
    public ResultData<ExportTaskSubmitVO> batchExportHrEmpInfo(@RequestBody HrEmpBatchExportRequest request) {
        try {
            // 1. 将请求参数序列化为JSON
            String requestParamJson = objectMapper.writeValueAsString(request);

            // 2. 创建导出任务
            com.crrc.entity.HrEmpExportTask task = hrEmpExportTaskService.createTask(
                    ExportTaskTypeEnum.HR_EMP_BATCH_EXPORT.getCode(), requestParamJson);

            // 3. 触发异步执行
            hrEmpExportAsyncService.executeAsync(task.getTaskId());

            // 4. 返回 taskId
            ExportTaskSubmitVO vo = new ExportTaskSubmitVO();
            vo.setTaskId(task.getTaskId());
            return ResultData.success(vo);
        } catch (Exception e) {
            return ResultData.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), "创建导出任务失败: " + e.getMessage());
        }
    }

    @GetMapping("/export/hrEmp/batch/task/{taskId}")
    @Operation(summary = "查询人员信息导出任务状态")
    public ResultData<ExportTaskStatusVO> queryExportTaskStatus(@PathVariable String taskId) {
        ExportTaskStatusVO statusVO = hrEmpExportTaskService.queryStatus(taskId);
        if (statusVO == null) {
            return ResultData.fail(HttpStatus.NOT_FOUND.value(), "任务不存在");
        }
        return ResultData.success(statusVO);
    }

    private static class DeepCopyDTO {
        /**
         * 顶层：把一个 List<SourceDTO> 转成 List<TargetDTO>
         */
        public static List<OrganizationTreeDTO> mapTreeList(List<DeptDTO> sourceList) {
            if (sourceList == null) {
                return null;
            }
            return sourceList.stream()
                    .filter(Objects::nonNull)
                    .map(DeepCopyDTO::mapNode)
                    .collect(Collectors.toList());
        }

        /**
         * 递归地把单个节点 SourceDTO -> TargetDTO（包含 children）
         */
        private static OrganizationTreeDTO mapNode(DeptDTO source) {
            if (source == null) {
                return null;
            }

            OrganizationTreeDTO target = new OrganizationTreeDTO();

            // 1. 映射当前节点的普通字段
            target.setId(source.getId());
            // 举例：name -> label
            target.setOrgCode(source.getOrgCode());
            target.setParentCode(source.getParentOrgCode());
            target.setOrgName(source.getDepartmentName());
            target.setStatus(source.getStatus());
            target.setCompanyCode(source.getCompanyId().toString());

            // 2. 递归映射 children
            List<DeptDTO> children = source.getChildren();
            if (children != null && !children.isEmpty()) {
                List<OrganizationTreeDTO> targetChildren = children.stream()
                        .filter(Objects::nonNull)
                        .map(DeepCopyDTO::mapNode)   // 递归调用
                        .collect(Collectors.toList());
                target.setChildren(targetChildren);
            }

            return target;
        }
    }


}
