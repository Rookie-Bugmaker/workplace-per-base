package com.crrc.pojo.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 树形职务族导入模板行。
 *
 * 使用文档模板字段顺序读取，兼容 number/name.zh_CN/ccar_category 等技术表头。
 */
@Data
@ExcelIgnoreUnannotated
public class PostCatalogInfoImportVO {
    @ExcelProperty(value = "number", index = 0)
    private String number;

    @ExcelProperty(value = "name.zh_CN", index = 1)
    private String name;

    @ExcelProperty(value = "ccar_category", index = 2)
    private String category;

    @ExcelProperty(value = "ccar_input_dept", index = 3)
    private String inputDept;

    @ExcelProperty(value = "ccar_modify_dept", index = 4)
    private String modifyDept;

    @ExcelProperty(value = "status", index = 5)
    private String status;

    @ExcelProperty(value = "ccar_description", index = 6)
    private String description;

    @ExcelProperty(value = "parent.number", index = 7)
    private String parentNumber;

    @ExcelProperty(value = "parent.name", index = 8)
    private String parentName;
}
