package com.crrc.pojo.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
@ExcelIgnoreUnannotated
public class PositionDescriptionResponsibilityExportVO {
    @ExcelProperty(value = "序号", index = 0)
    private Integer serialNo;
    @ExcelProperty(value = "职位编码", index = 1)
    private String positionCode;
    @ExcelProperty(value = "职位名称", index = 2)
    private String positionName;
    @ExcelProperty(value = "职责名称", index = 3)
    private String responsibilityName;
    @ExcelProperty(value = "职责描述", index = 4)
    private String responsibilityDesc;
}
