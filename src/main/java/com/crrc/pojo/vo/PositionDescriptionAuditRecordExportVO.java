package com.crrc.pojo.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
@ExcelIgnoreUnannotated
public class PositionDescriptionAuditRecordExportVO {
    @ExcelProperty(value = "序号", index = 0)
    private Integer serialNo;
    @ExcelProperty(value = "职位编码", index = 1)
    private String positionCode;
    @ExcelProperty(value = "职位名称", index = 2)
    private String positionName;
    @ExcelProperty(value = "节点Key", index = 3)
    private String nodeKey;
    @ExcelProperty(value = "节点名称", index = 4)
    private String nodeName;
    @ExcelProperty(value = "角色编码", index = 5)
    private String roleCode;
    @ExcelProperty(value = "审批人工号", index = 6)
    private String auditorCode;
    @ExcelProperty(value = "审批人姓名", index = 7)
    private String auditorName;
    @ExcelProperty(value = "审批结果", index = 8)
    private String auditResult;
    @ExcelProperty(value = "审批意见", index = 9)
    private String auditOpinion;
    @ExcelProperty(value = "审批时间", index = 10)
    private String auditTime;
}
