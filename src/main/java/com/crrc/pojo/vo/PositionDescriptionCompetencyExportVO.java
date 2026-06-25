package com.crrc.pojo.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
@ExcelIgnoreUnannotated
public class PositionDescriptionCompetencyExportVO {
    @ExcelProperty(value = "序号", index = 0)
    private Integer serialNo;
    @ExcelProperty(value = "职位编码", index = 1)
    private String positionCode;
    @ExcelProperty(value = "职位名称", index = 2)
    private String positionName;
    @ExcelProperty(value = "能力类别", index = 3)
    private String competencyCategory;
    @ExcelProperty(value = "能力名称", index = 4)
    private String competencyName;
    @ExcelProperty(value = "能力等级要求", index = 5)
    private String competencyLevel;
}
