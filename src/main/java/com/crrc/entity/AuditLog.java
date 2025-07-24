package com.crrc.entity;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import com.crrc.annotation.QueryMethod;
import com.crrc.converter.DictConverter;
import com.crrc.entity.BaseEntity;
import com.crrc.enums.QueryType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
* <p>
    * 日志表
    * </p>
*
* @author mayiyao
* @since 2025-07-24 09:07:55
*/
@ExcelIgnoreUnannotated
@TableName("sys_audit_log")
@Schema(description = "日志表")
@Data
public class AuditLog extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1948188362231443456L;

    @Schema(description = "主键")
    @TableId(value = "${column.tableColName}", type = IdType.ASSIGN_ID)
    private Long id;

    @TableLogic(value = "0")
    @Schema(description = "删除", example = "false")
    private Integer deleted;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"用户名"}, index = 0)
    @Schema(description = "用户名", example = "")
    private String userName;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"用户名"}, index = 1)
    @Schema(description = "用户名", example = "")
    private String funcName;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"ip地址"}, index = 2)
    @Schema(description = "ip地址", example = "")
    private String remoteIp;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"系统名称"}, index = 3)
    @Schema(description = "系统名称", example = "")
    private String sysName;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"日志类型"}, index = 4)
    @Schema(description = "日志类型", example = "")
    private String logType;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"操作类型"}, index = 5)
    @Schema(description = "操作类型", example = "")
    private String operationType;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"运行时间"}, index = 6)
    @Schema(description = "运行时间", example = "")
    private Long runTime;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"结果"}, index = 7)
    @Schema(description = "结果", example = "")
    private String result;

    @QueryMethod(method = QueryType.BETWEEN, start = "createDateStartTime", end = "createDateEndTime")
    @ExcelProperty(value = {"创建时间"}, index = 8)
    @Schema(description = "创建时间", example = "")
    private LocalDateTime createDate;

    @TableField(exist = false)
    @Schema(description = "创建时间-开始时间", example = "2024-01-01 00:00:00")
    private LocalDateTime createDateStartTime;

    @TableField(exist = false)
    @Schema(description = "创建时间-结束时间", example = "2024-01-01 00:00:00")
    private LocalDateTime createDateEndTime;

    public AuditLog() {
    }


    @Override
    public String toString() {
    return "AuditLog{" +
        "id=" + id +
        ",userName='" + userName + "'" +
        ",funcName='" + funcName + "'" +
        ",remoteIp='" + remoteIp + "'" +
        ",sysName='" + sysName + "'" +
        ",logType='" + logType + "'" +
        ",operationType='" + operationType + "'" +
        ",runTime='" + runTime + "'" +
        ",result='" + result + "'" +
        ",createDate='" + createDate + "'" +
        '}';
    }
}
