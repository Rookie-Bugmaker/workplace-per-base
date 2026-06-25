package com.crrc.pojo.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
@ExcelIgnoreUnannotated
public class PositionDescriptionAttachmentExportVO {
    @ExcelProperty(value = "序号", index = 0)
    private Integer serialNo;
    @ExcelProperty(value = "职位编码", index = 1)
    private String positionCode;
    @ExcelProperty(value = "职位名称", index = 2)
    private String positionName;
    @ExcelProperty(value = "fileId", index = 3)
    private String fileId;
    @ExcelProperty(value = "文件名", index = 4)
    private String fileName;
    @ExcelProperty(value = "文件类型", index = 5)
    private String fileType;
}
