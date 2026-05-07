package com.crrc.enums;

/**
 * 异步导出任务类型枚举
 */
public enum ExportTaskTypeEnum {
    HR_EMP_BATCH_EXPORT("HR_EMP_BATCH_EXPORT", "人事信息批量导出");

    private final String code;
    private final String desc;

    ExportTaskTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
