package com.crrc.constants;

public final class AuditConstants {
    /**
     * 审批中
     */
    public static final String STATUS_AUDIT = "AUDIT";
    /**
     * 审批中
     */
    // 注意，虽然IDE可能提示未使用，但实际在ConstantsUtils中使用反射转为了filed->filed_DESC的map
    public static final String STATUS_AUDIT_DESC = "审批中";
    /**
     * 审批通过
     */
    public static final String STATUS_AUDIT_PASS = "AUDIT_PASS";
    /**
     * 审批通过
     */
    public static final String STATUS_AUDIT_PASS_DESC = "审批通过";
    /**
     * 审批通过
     */
    public static final String STATUS_SUBMITTED = "SUBMITTED";
    /**
     * 审批通过
     */
    public static final String STATUS_SUBMITTED_DESC = "部门已推送";
    /**
     * 等待自评
     */
    public static final String STATUS_SELF_EVAL = "SELF_EVAL";
    /**
     * 等待自评
     */
    public static final String STATUS_SELF_EVAL_DESC = "审批通过";
    /**
     * 自评审批中
     */
    public static final String STATUS_AUDIT_SELF_EVAL = "AUDIT_SELF_EVAL";
    /**
     * 自评审批中
     */
    public static final String STATUS_AUDIT_SELF_EVAL_DESC = "自评完成，等待打分";
    /**
     * 草稿
     */
    public static final String STATUS_DRAFT = "AUDIT_DRAFT";
    /**
     * 草稿
     */
    public static final String STATUS_DRAFT_DESC = "未提交";
    /**
     * 拒绝
     */
    public static final String STATUS_REJECT = "AUDIT_REJECT";
    /**
     * 拒绝
     */
    public static final String STATUS_REJECT_DESC = "已拒绝";

    private AuditConstants(){}
}
