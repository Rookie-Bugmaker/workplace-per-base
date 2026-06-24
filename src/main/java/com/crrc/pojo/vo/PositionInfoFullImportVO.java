package com.crrc.pojo.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 职位信息完整导入模板行。
 * 日期字段使用字符串接收，业务层统一兼容 yyyy-MM-dd / yyyy/MM/dd / yyyyMMdd。
 */
@Data
@ExcelIgnoreUnannotated
public class PositionInfoFullImportVO {
    @ExcelProperty(value = "职位编码")
    private String code;

    @ExcelProperty(value = "职位名称")
    private String name;

    @ExcelProperty(value = "版本")
    private String version;

    @ExcelProperty(value = "生效日期")
    private String effectiveDate;

    @ExcelProperty(value = "撤销日期")
    private String revocationDate;

    @ExcelProperty(value = "所属组织单元")
    private String orgUnit;

    @ExcelProperty(value = "工作中心")
    private String workCenter;

    @ExcelProperty(value = "职类")
    private String jobCategory;

    @ExcelProperty(value = "职群")
    private String jobGroup;

    @ExcelProperty(value = "职种")
    private String jobType;

    @ExcelProperty(value = "职层")
    private String jobLevel;

    @ExcelProperty(value = "标准职级")
    private String jobGrade;

    @ExcelProperty(value = "编制人数")
    private String headcountQuota;

    @ExcelProperty(value = "控编模式")
    private String controlMode;

    @ExcelProperty(value = "部门代码")
    private String deptCode;

    @ExcelProperty(value = "部门名称")
    private String deptName;

    @ExcelProperty(value = "上级职位编码")
    private String parentPositionCode;

    @ExcelProperty(value = "上级职位名称")
    private String parentPositionName;

    @ExcelProperty(value = "关联职务")
    private String relatedPost;

    @ExcelProperty(value = "工作地点")
    private String workPlace;

    @ExcelProperty(value = "工作内容")
    private String workContent;

    @ExcelProperty(value = "职位状态")
    private String positionStatus;
}
