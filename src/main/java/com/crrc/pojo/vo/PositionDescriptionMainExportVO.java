package com.crrc.pojo.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 职位说明书主信息导出行。
 */
@Data
@ExcelIgnoreUnannotated
public class PositionDescriptionMainExportVO {
    @ExcelProperty(value = "职位说明书ID", index = 0)
    private Long descriptionId;

    @ExcelProperty(value = "职位编码", index = 1)
    private String positionCode;

    @ExcelProperty(value = "职位名称", index = 2)
    private String positionName;

    @ExcelProperty(value = "版本", index = 3)
    private String version;

    @ExcelProperty(value = "生效日期", index = 4)
    private String effectiveDate;

    @ExcelProperty(value = "撤销日期", index = 5)
    private String revocationDate;

    @ExcelProperty(value = "所属组织单元", index = 6)
    private String orgUnit;

    @ExcelProperty(value = "职类", index = 7)
    private String jobCategory;

    @ExcelProperty(value = "职群", index = 8)
    private String jobGroup;

    @ExcelProperty(value = "职种", index = 9)
    private String jobType;

    @ExcelProperty(value = "职层", index = 10)
    private String jobLevel;

    @ExcelProperty(value = "工作地点", index = 11)
    private String workPlace;

    @ExcelProperty(value = "工作内容", index = 12)
    private String workContent;

    @ExcelProperty(value = "标准职级", index = 13)
    private String jobGrade;

    @ExcelProperty(value = "编制人数", index = 14)
    private Integer headcountQuota;

    @ExcelProperty(value = "控编模式", index = 15)
    private String controlMode;

    @ExcelProperty(value = "直接上级职位编码", index = 16)
    private String directSuperiorPositionCode;

    @ExcelProperty(value = "直接上级职位名称", index = 17)
    private String directSuperiorPositionName;

    @ExcelProperty(value = "直接下级职位编码", index = 18)
    private String directSubordinatePositionCodes;

    @ExcelProperty(value = "直接下级职位名称", index = 19)
    private String directSubordinatePositionNames;

    @ExcelProperty(value = "职位定位", index = 20)
    private String positionPurpose;

    @ExcelProperty(value = "工作环境", index = 21)
    private String workEnvironment;

    @ExcelProperty(value = "任职资格", index = 22)
    private String qualification;

    @ExcelProperty(value = "所需知识", index = 23)
    private String requiredKnowledge;

    @ExcelProperty(value = "状态", index = 24)
    private String status;

    @ExcelProperty(value = "流程实例ID", index = 25)
    private String procInstId;

    @ExcelProperty(value = "提交时间", index = 26)
    private String submitTime;

    @ExcelProperty(value = "发布时间", index = 27)
    private String publishTime;

    @ExcelProperty(value = "是否最新版本", index = 28)
    private String latestFlag;
}
