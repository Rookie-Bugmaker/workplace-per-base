package com.crrc.pojo.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
@ExcelIgnoreUnannotated
public class PositionDescriptionCoordinationExportVO {
    @ExcelProperty(value = "序号", index = 0)
    private Integer serialNo;
    @ExcelProperty(value = "职位编码", index = 1)
    private String positionCode;
    @ExcelProperty(value = "职位名称", index = 2)
    private String positionName;
    @ExcelProperty(value = "协调类型", index = 3)
    private String coordinationType;
    @ExcelProperty(value = "协调对象", index = 4)
    private String coordinationObject;
    @ExcelProperty(value = "协调内容", index = 5)
    private String coordinationContent;
}
