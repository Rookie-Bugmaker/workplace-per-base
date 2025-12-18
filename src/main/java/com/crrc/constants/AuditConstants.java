package com.crrc.constants;

/**
 * <p>
 * 审批状态控制器
 * </p>
 *
 * @author yukun
 * @since 2025-12-18 14:32
 */
public final class AuditConstants {
    /**
     * 审批中
     */
    public static final String STATUS_AUDIT = "AUDIT";
    // 注意，虽然IDE可能提示未使用，但实际在ConstantsUtils中使用反射转为了filed->filed_DESC的map
    public static final String STATUS_AUDIT_DESC = "审批中";
    /**
     * 审批通过
     */
    public static final String STATUS_AUDIT_PASS = "AUDIT_PASS";
    public static final String STATUS_AUDIT_PASS_DESC = "审批通过";
    /**
     * 草稿
     */
    public static final String STATUS_DRAFT = "DRAFT";
    public static final String STATUS_DRAFT_DESC = "未提交";
    /**
     * 已拒绝
     */
    public static final String STATUS_REJECT = "REJECT";
    public static final String STATUS_REJECT_DESC = "已拒绝";
}
