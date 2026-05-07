package com.crrc.enums;

/**
 * 异步导出任务状态枚举
 */
public enum ExportTaskStatusEnum {
    PENDING("PENDING", "已创建，等待执行"),
    RUNNING("RUNNING", "执行中"),
    SUCCESS("SUCCESS", "执行成功"),
    FAILED("FAILED", "执行失败"),
    CANCELLED("CANCELLED", "已取消");

    private final String code;
    private final String desc;

    ExportTaskStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static ExportTaskStatusEnum fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (ExportTaskStatusEnum e : values()) {
            if (e.code.equals(code)) {
                return e;
            }
        }
        return null;
    }
}
