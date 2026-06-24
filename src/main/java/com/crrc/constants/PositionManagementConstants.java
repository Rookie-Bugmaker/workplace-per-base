package com.crrc.constants;

/**
 * 职位管理模块常量。
 */
public final class PositionManagementConstants {
    private PositionManagementConstants() {
    }

    /** 职位说明书审批流程 key。 */
    public static final String POSITION_DESCRIPTION_APPROVAL_PROCESS_KEY = "position_description_approval";

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
