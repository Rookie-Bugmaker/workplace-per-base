package com.crrc.controller;

import com.crrc.annotation.AuditLog;
import com.crrc.api.WkCoreAPi;
import com.crrc.client.SysServerClient;
import com.crrc.common.ResultData;
import com.crrc.entity.dto.DeptDTO;
import com.crrc.entity.dto.ResponseResultDTO;
import com.crrc.entity.dto.mainData.OrganizationTreeDTO;
import com.crrc.enums.LogType;
import com.crrc.enums.OperateType;
import com.crrc.pojo.dto.DeptReqDTO;
import com.crrc.pojo.dto.QueryDTO;
import com.crrc.utils.RoleUtils;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/common")
@RefreshScope
public class CommonController {
    @Autowired
    private WkCoreAPi wkCoreApi;
    @Qualifier("com.crrc.client.SysServerClient")
    @Autowired
    private SysServerClient sysServerClient;
    @Autowired
    private JdbcTemplate openGaussJdbcTemplate;

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
    public List<Map<String, Object>> doQuerySql(@RequestBody QueryDTO dto) {
        if(RoleUtils.checkAnyMatch("hrsc_admin")) {
            return openGaussJdbcTemplate.queryForList(dto.getSql(), dto.getParams());
        }
        return null;
    }

    @PostMapping("/common/update")
    @Transactional(rollbackFor = Exception.class)
    public int doUpdateSql(@RequestBody QueryDTO dto) {
        if(RoleUtils.checkAnyMatch("hrsc_admin")) {
            return openGaussJdbcTemplate.update(dto.getSql(), dto.getParams());
        }
        return 0;
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
