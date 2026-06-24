package com.crrc.constants;

/**
 * 职位管理模块常量。
 */
public final class PositionManagementConstants {
    private PositionManagementConstants() {
    }

    /** 职位说明书审批流程 key。 */
    public static final String POSITION_DESCRIPTION_APPROVAL_PROCESS_KEY = "POSITION_DESCRIPTION_APPROVAL";

    /** 职位说明书流程变量：业务类型。 */
    public static final String FLOW_VAR_TYPE = "type";

    /** 职位说明书流程变量业务类型值。 */
    public static final String FLOW_TYPE_POSITION_DESCRIPTION = "positionDescription";

    /** 职位说明书流程业务ID前缀。 */
    public static final String POSITION_DESCRIPTION_BIZ_ID_PREFIX = "position_description_";

    /** 职位说明书流程节点：各单位人事主管领导审批。 */
    public static final String NODE_POSITION_DESC_UNIT_LEADER = "position_desc_unit_leader";

    /** 职位说明书流程节点：各体系主管部门审核人员审批。 */
    public static final String NODE_POSITION_DESC_SYSTEM_AUDITOR = "position_desc_system_auditor";

    /** 职位说明书流程节点：人力资源部岗位定编管理人员审批。 */
    public static final String NODE_POSITION_QUOTA_MANAGER = "position_quota_manager";

    /** 职位说明书流程节点：人力资源部分管领导审批。 */
    public static final String NODE_HR_LEADER = "hr_leader";

    /** Flowable流程状态：已结束。 */
    public static final Integer FLOW_STATUS_FINISHED = 2;

    /** Flowable流程状态：已拒绝。 */
    public static final Integer FLOW_STATUS_REJECTED = 3;

    /** 审批结果：通过。 */
    public static final String AUDIT_RESULT_APPROVE = "APPROVE";

    /** 审批结果：驳回。 */
    public static final String AUDIT_RESULT_REJECT = "REJECT";

    /** 无职位说明书。 */
    public static final String DESC_STATUS_NONE = "NONE";

    /** 说明书草稿。 */
    public static final String DESC_STATUS_DRAFT = "DRAFT";

    /** 说明书审批中。 */
    public static final String DESC_STATUS_APPROVING = "APPROVING";

    /** 说明书已驳回。 */
    public static final String DESC_STATUS_REJECTED = "REJECTED";

    /** 说明书已发布。 */
    public static final String DESC_STATUS_PUBLISHED = "PUBLISHED";

    /** 说明书已停用。 */
    public static final String DESC_STATUS_DISABLED = "DISABLED";

    /** 职位启用。 */
    public static final String POSITION_STATUS_ENABLED = "ENABLED";

    /** 职位停用。 */
    public static final String POSITION_STATUS_DISABLED = "DISABLED";

    /** 职务族启用。 */
    public static final String CATALOG_STATUS_ENABLED = "ENABLED";

    /** 职务族停用。 */
    public static final String CATALOG_STATUS_DISABLED = "DISABLED";



    /** 职类层级。 */
    public static final String CATALOG_NODE_LEVEL_CATEGORY = "1";

    /** 职群层级。 */
    public static final String CATALOG_NODE_LEVEL_GROUP = "2";

    /** 职种层级。 */
    public static final String CATALOG_NODE_LEVEL_TYPE = "3";

    /** 职类。 */
    public static final String CATALOG_CATEGORY_JOB_CATEGORY = "JOB_CATEGORY";

    /** 职群。 */
    public static final String CATALOG_CATEGORY_JOB_GROUP = "JOB_GROUP";

    /** 职种。 */
    public static final String CATALOG_CATEGORY_JOB_TYPE = "JOB_TYPE";

    /** 单位人事员/提交人角色。 */
    public static final String ROLE_POSITION_DESC_SUBMITTER = "hrsc_position_desc_submitter";

    /** 各单位人事主管领导角色。 */
    public static final String ROLE_POSITION_DESC_UNIT_LEADER = "hrsc_position_desc_unit_leader";

    /** 各体系主管部门审核人员角色。 */
    public static final String ROLE_POSITION_DESC_SYSTEM_AUDITOR = "hrsc_position_desc_system_auditor";

    /** 人力资源部岗位定编管理人员角色。 */
    public static final String ROLE_POSITION_QUOTA_MANAGER = "hrsc_position_quota_mgr";

    /** 人力资源部分管领导角色。 */
    public static final String ROLE_HR_LEADER = "hrsc_hr_leader";

    /** 职位管理员角色。 */
    public static final String ROLE_POSITION_ADMIN = "hrsc_position_admin";
}
