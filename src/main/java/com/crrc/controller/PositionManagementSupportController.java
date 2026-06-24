package com.crrc.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crrc.annotation.AuditLog;
import com.crrc.annotation.TenantIgnore;
import com.crrc.constants.PositionManagementConstants;
import com.crrc.entity.PositionDescription;
import com.crrc.entity.PositionInfo;
import com.crrc.entity.PostCatalogInfo;
import com.crrc.enums.LogType;
import com.crrc.enums.OperateType;
import com.crrc.pojo.ResponseResult;
import com.crrc.pojo.vo.PositionApiEndpointVO;
import com.crrc.pojo.vo.PositionManagementOverviewVO;
import com.crrc.pojo.vo.PositionOptionVO;
import com.crrc.service.impl.PositionInfoServiceImpl;
import com.crrc.service.impl.PostCatalogInfoServiceImpl;
import com.crrc.service.PositionDescriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 职位管理模块前端联调支撑接口。
 */
@RestController
@RequestMapping("/positionManagement")
@RefreshScope
@TenantIgnore
@Tag(name = "职位管理模块联调支撑", description = "状态字典、接口清单、首页统计")
public class PositionManagementSupportController {

    @Autowired
    private PositionInfoServiceImpl positionInfoService;

    @Autowired
    private PostCatalogInfoServiceImpl postCatalogInfoService;

    @Autowired
    private PositionDescriptionService positionDescriptionService;

    @GetMapping("/options")
    @Operation(summary = "职位管理模块前端下拉选项")
    @AuditLog(value = "职位管理模块前端下拉选项", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    public ResponseResult<Map<String, List<PositionOptionVO>>> options() {
        Map<String, List<PositionOptionVO>> result = new LinkedHashMap<>();
        result.put("positionStatus", Arrays.asList(
                option(PositionManagementConstants.POSITION_STATUS_ENABLED, "启用", "positionStatus", 1, null),
                option(PositionManagementConstants.POSITION_STATUS_DISABLED, "停用", "positionStatus", 2, null)
        ));
        result.put("descriptionStatus", Arrays.asList(
                option(PositionManagementConstants.DESC_STATUS_NONE, "无说明书", "descriptionStatus", 0, null),
                option(PositionManagementConstants.DESC_STATUS_DRAFT, "草稿", "descriptionStatus", 1, null),
                option(PositionManagementConstants.DESC_STATUS_APPROVING, "审批中", "descriptionStatus", 2, null),
                option(PositionManagementConstants.DESC_STATUS_REJECTED, "已驳回", "descriptionStatus", 3, null),
                option(PositionManagementConstants.DESC_STATUS_PUBLISHED, "已发布", "descriptionStatus", 4, null),
                option(PositionManagementConstants.DESC_STATUS_DISABLED, "已停用", "descriptionStatus", 5, null),
                option(PositionManagementConstants.DESC_STATUS_CANCELED, "已取消", "descriptionStatus", 6, null)
        ));
        result.put("catalogCategory", Arrays.asList(
                option(PositionManagementConstants.CATALOG_CATEGORY_JOB_CATEGORY, "职类", "catalogCategory", 1, "根节点"),
                option(PositionManagementConstants.CATALOG_CATEGORY_JOB_GROUP, "职群", "catalogCategory", 2, "上级必须为职类"),
                option(PositionManagementConstants.CATALOG_CATEGORY_JOB_TYPE, "职种", "catalogCategory", 3, "上级必须为职群")
        ));
        result.put("catalogStatus", Arrays.asList(
                option(PositionManagementConstants.CATALOG_STATUS_ENABLED, "启用", "catalogStatus", 1, null),
                option(PositionManagementConstants.CATALOG_STATUS_DISABLED, "停用", "catalogStatus", 2, null)
        ));
        result.put("workflowRole", Arrays.asList(
                option(PositionManagementConstants.ROLE_POSITION_DESC_SUBMITTER, "单位人事员/提交人", "workflowRole", 1, null),
                option(PositionManagementConstants.ROLE_POSITION_DESC_UNIT_LEADER, "各单位人事主管领导", "workflowRole", 2, null),
                option(PositionManagementConstants.ROLE_POSITION_DESC_SYSTEM_AUDITOR, "各体系主管部门审核人员", "workflowRole", 3, null),
                option(PositionManagementConstants.ROLE_POSITION_QUOTA_MANAGER, "人力资源部岗位定编管理人员", "workflowRole", 4, null),
                option(PositionManagementConstants.ROLE_HR_LEADER, "人力资源部分管领导", "workflowRole", 5, null),
                option(PositionManagementConstants.ROLE_POSITION_ADMIN, "职位管理员", "workflowRole", 6, null)
        ));
        result.put("workflowProcess", Arrays.asList(
                option(PositionManagementConstants.POSITION_DESCRIPTION_APPROVAL_PROCESS_KEY, "职位说明书审批流程", "workflowProcess", 1, "Flowable templateAlias/processId")
        ));
        result.put("exportType", Arrays.asList(
                option("xlsx", "Excel 2007+", "exportType", 1, null),
                option("xls", "Excel 97-2003", "exportType", 2, null),
                option("csv", "CSV", "exportType", 3, null)
        ));
        result.put("yesNo", Arrays.asList(
                option("1", "是", "yesNo", 1, null),
                option("0", "否", "yesNo", 2, null)
        ));
        return new ResponseResult<>(HttpStatus.OK.value(), "职位管理模块前端下拉选项查询成功", result);
    }

    @GetMapping("/apiMap")
    @Operation(summary = "职位管理模块菜单按钮与接口对照")
    @AuditLog(value = "职位管理模块菜单按钮与接口对照", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    public ResponseResult<List<PositionApiEndpointVO>> apiMap() {
        List<PositionApiEndpointVO> list = new ArrayList<>();
        addApi(list, "职位信息管理", "列表分页", "POST", "/positionInfo/page", "按组织、职位、职务族等条件分页查询职位", "修改", "PositionInfo + pageNo/pageSize", "IPage<PositionInfo>");
        addApi(list, "职位信息管理", "新增", "POST", "/positionInfo/create", "新增职位并校验职位编码唯一", "修改", "PositionInfo", "Boolean");
        addApi(list, "职位信息管理", "编辑", "POST", "/positionInfo/saveOrUpdate", "编辑职位并保留原字段返回", "修改", "PositionInfo", "Boolean");
        addApi(list, "职位信息管理", "删除", "POST", "/positionInfo/delete", "删除前实时校验在岗人员", "修改", "List<Integer>", "Boolean");
        addApi(list, "职位信息管理", "完整导入", "POST", "/positionInfo/importFull", "职位完整导入，按职位编码新增或更新", "新增", "MultipartFile", "PositionInfoImportResultVO");
        addApi(list, "职位信息管理", "复制到组织", "POST", "/positionInfo/copyToOrgUnit", "复制职位到指定组织单元", "新增", "PositionInfoCopyRequest", "List<PositionInfo>");
        addApi(list, "职位信息管理", "刷新在岗人员", "POST", "/positionInfo/refreshIncumbents", "刷新在岗人数/在岗人员快照", "新增", "List<String>，空为全部", "Integer");
        addApi(list, "职位信息管理", "基本版导出", "POST", "/positionInfo/exportBasic", "导出职位基本信息", "新增", "PositionInfo + type", "文件流");
        addApi(list, "职位信息管理", "含说明书导出", "POST", "/positionInfo/exportWithDescription", "导出职位及说明书正文", "新增", "PositionInfo + type", "文件流");

        addApi(list, "树形职务族", "树查询", "POST", "/postCatalogInfo/queryTree", "查询职类-职群-职种树", "修改", "无", "List<PostCatalogInfoTreeVO>");
        addApi(list, "树形职务族", "新增", "POST", "/postCatalogInfo/create", "新增并校验三层结构", "修改", "PostCatalogInfo", "Boolean");
        addApi(list, "树形职务族", "编辑", "POST", "/postCatalogInfo/update", "编辑并校验三层结构", "修改", "PostCatalogInfo", "Boolean");
        addApi(list, "树形职务族", "删除/停用", "GET", "/postCatalogInfo/delete", "有下级或引用时停用，无引用时逻辑删除", "修改", "id", "Boolean");
        addApi(list, "树形职务族", "完整导入", "POST", "/postCatalogInfo/importFull", "按number模板导入职务族", "新增", "MultipartFile", "PostCatalogInfoImportResultVO");
        addApi(list, "树形职务族", "模板导出", "POST", "/postCatalogInfo/exportTemplate", "导出ccar_job_family_tree模板", "新增", "无", "文件流");

        addApi(list, "职位说明书管理", "列表分页", "POST", "/positionDescription/page", "分页查询说明书", "新增", "PositionDescription + pageNo/pageSize", "IPage<PositionDescription>");
        addApi(list, "职位说明书管理", "详情", "GET", "/positionDescription/get", "聚合查询主表、子表、附件、审批记录", "新增", "id", "PositionDescriptionDetailVO");
        addApi(list, "职位说明书管理", "保存草稿", "POST", "/positionDescription/saveDraft", "保存主表和说明书子表，附件只保存fileId", "新增", "PositionDescriptionSaveDTO", "PositionDescriptionDetailVO");
        addApi(list, "职位说明书管理", "提交审批", "POST", "/positionDescription/submit", "启动Flowable流程", "新增", "id", "PositionDescription");
        addApi(list, "职位说明书管理", "待办", "GET", "/positionDescription/currentUserTodo", "查询当前用户职位说明书审批待办", "新增", "无", "List<PositionDescriptionTodoVO>");
        addApi(list, "职位说明书管理", "审批通过", "POST", "/positionDescription/approve", "Flowable审批通过并写审批记录", "新增", "PositionDescriptionAuditDTO", "Boolean");
        addApi(list, "职位说明书管理", "审批驳回", "POST", "/positionDescription/reject", "Flowable审批驳回并回写状态", "新增", "PositionDescriptionAuditDTO", "Boolean");
        addApi(list, "职位说明书管理", "取消/撤销", "POST", "/positionDescription/cancel", "草稿/驳回/审批中取消，审批中同步终止流程", "新增", "id + reason", "Boolean");
        addApi(list, "职位说明书管理", "流程跟踪", "GET", "/positionDescription/processTrace", "查看Flowable运行时流程图", "新增", "id", "ProcessRunningInfoVo");
        addApi(list, "职位说明书管理", "明细导出", "GET", "/positionDescription/exportDetail", "多Sheet导出说明书详情", "新增", "id", "文件流");
        return new ResponseResult<>(HttpStatus.OK.value(), "职位管理模块菜单按钮与接口对照查询成功", list);
    }

    @GetMapping("/overview")
    @Operation(summary = "职位管理模块首页统计概览")
    @AuditLog(value = "职位管理模块首页统计概览", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    public ResponseResult<PositionManagementOverviewVO> overview() {
        PositionManagementOverviewVO overview = new PositionManagementOverviewVO();
        overview.setPositionTotal(Long.valueOf(positionInfoService.count()));
        overview.setPositionEnabled(Long.valueOf(positionInfoService.count(new LambdaQueryWrapper<PositionInfo>()
                .eq(PositionInfo::getPositionStatus, PositionManagementConstants.POSITION_STATUS_ENABLED))));
        overview.setPositionDisabled(Long.valueOf(positionInfoService.count(new LambdaQueryWrapper<PositionInfo>()
                .eq(PositionInfo::getPositionStatus, PositionManagementConstants.POSITION_STATUS_DISABLED))));

        overview.setCatalogTotal(Long.valueOf(postCatalogInfoService.count()));
        overview.setCatalogCategoryCount(Long.valueOf(postCatalogInfoService.count(new LambdaQueryWrapper<PostCatalogInfo>()
                .eq(PostCatalogInfo::getCategory, PositionManagementConstants.CATALOG_CATEGORY_JOB_CATEGORY))));
        overview.setCatalogGroupCount(Long.valueOf(postCatalogInfoService.count(new LambdaQueryWrapper<PostCatalogInfo>()
                .eq(PostCatalogInfo::getCategory, PositionManagementConstants.CATALOG_CATEGORY_JOB_GROUP))));
        overview.setCatalogTypeCount(Long.valueOf(postCatalogInfoService.count(new LambdaQueryWrapper<PostCatalogInfo>()
                .eq(PostCatalogInfo::getCategory, PositionManagementConstants.CATALOG_CATEGORY_JOB_TYPE))));
        overview.setCatalogDisabled(Long.valueOf(postCatalogInfoService.count(new LambdaQueryWrapper<PostCatalogInfo>()
                .eq(PostCatalogInfo::getStatus, PositionManagementConstants.CATALOG_STATUS_DISABLED))));

        Map<String, Long> descStatus = positionDescriptionService.statusStatistics(null);
        overview.setDescriptionStatusCount(descStatus);
        long descTotal = 0L;
        for (Long count : descStatus.values()) {
            if (count != null) {
                descTotal += count;
            }
        }
        overview.setDescriptionTotal(descTotal);
        return new ResponseResult<>(HttpStatus.OK.value(), "职位管理模块首页统计概览查询成功", overview);
    }

    private PositionOptionVO option(String value, String label, String type, Integer sortNo, String remark) {
        return new PositionOptionVO(value, label, type, sortNo, remark);
    }

    private void addApi(List<PositionApiEndpointVO> list, String module, String action, String method, String path,
                        String description, String changeType, String requestRemark, String responseRemark) {
        list.add(new PositionApiEndpointVO(module, action, method, path, description, changeType, requestRemark, responseRemark));
    }
}
