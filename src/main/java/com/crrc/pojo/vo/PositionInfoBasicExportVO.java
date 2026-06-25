package com.crrc.pojo.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 职位信息基本版导出行。
 */
@Data
@ExcelIgnoreUnannotated
public class PositionInfoBasicExportVO {
    @ExcelProperty(value = "序号", index = 0)
    private Integer serialNo;

    @ExcelProperty(value = "职位编码", index = 1)
    private String code;

    @ExcelProperty(value = "职位名称", index = 2)
    private String name;

    @ExcelProperty(value = "版本", index = 3)
    private String version;

    @ExcelProperty(value = "生效日期", index = 4)
    private String effectiveDate;

    @ExcelProperty(value = "撤销日期", index = 5)
    private String revocationDate;

    @ExcelProperty(value = "所属组织单元编码", index = 6)
    private String orgUnit;

    @ExcelProperty(value = "所属组织单元", index = 7)
    private String orgUnitValue;

    @ExcelProperty(value = "工作中心", index = 8)
    private String workCenter;

    @ExcelProperty(value = "职类编码", index = 9)
    private String jobCategory;

    @ExcelProperty(value = "职类", index = 10)
    private String jobCategoryValue;

    @ExcelProperty(value = "职群编码", index = 11)
    private String jobGroup;

    @ExcelProperty(value = "职群", index = 12)
    private String jobGroupValue;

    @ExcelProperty(value = "职种编码", index = 13)
    private String jobType;

    @ExcelProperty(value = "职种", index = 14)
    private String jobTypeValue;

    @ExcelProperty(value = "职层", index = 15)
    private String jobLevel;

    @ExcelProperty(value = "标准职级", index = 16)
    private String jobGrade;

    @ExcelProperty(value = "编制人数", index = 17)
    private Integer headcountQuota;

    @ExcelProperty(value = "控编模式", index = 18)
    private String controlMode;

    @ExcelProperty(value = "在岗人数", index = 19)
    private Integer incumbentCount;

    @ExcelProperty(value = "在岗人员", index = 20)
    private String incumbentPersons;

    @ExcelProperty(value = "部门代码", index = 21)
    private String deptCode;

    @ExcelProperty(value = "部门名称", index = 22)
    private String deptName;

    @ExcelProperty(value = "上级职位编码", index = 23)
    private String parentPositionCode;

    @ExcelProperty(value = "上级职位名称", index = 24)
    private String parentPositionName;

    @ExcelProperty(value = "关联职务", index = 25)
    private String relatedPost;

    @ExcelProperty(value = "工作地点", index = 26)
    private String workPlace;

    @ExcelProperty(value = "工作内容", index = 27)
    private String workContent;

    @ExcelProperty(value = "职位状态", index = 28)
    private String positionStatus;

    @ExcelProperty(value = "职位说明书状态", index = 29)
    private String positionDescStatus;
}
