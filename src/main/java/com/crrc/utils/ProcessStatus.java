package com.crrc.utils;

import lombok.Data;

/**
 * 流程状态实体类
 * (0未开始、1进行中、2已结束、3已拒绝、4已重置)
 */
@Data
public class ProcessStatus {
    /**
     * 框架审批流状态码 - 未开始
     */
    public static final Integer UN_START = 0;

    /**
     * 框架审批流状态码 - 进行中
     */
    public static final Integer IN_PROGRESS = 1;

    /**
     * 框架审批流状态码 - 已结束
     */
    public static final Integer COMPLETED = 2;

    /**
     * 框架审批流状态码 - 已拒绝
     */
    public static final Integer REJECTED = 3;

    /**
     * 框架审批流状态码 - 已重置
     */
    public static final Integer RESET = 4;
}
