package com.crrc.pojo.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 职位信息含职位说明书导出行。
 *
 * 子表内容按序号合并为文本，便于单 Sheet 批量导出和比对。
 */
@Data
@ExcelIgnoreUnannotated
public class PositionInfoDescriptionExportVO {
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

    @ExcelProperty(value = "职类", index = 8)
    private String jobCategoryValue;

    @ExcelProperty(value = "职群", index = 9)
    private String jobGroupValue;

    @ExcelProperty(value = "职种", index = 10)
    private String jobTypeValue;

    @ExcelProperty(value = "职层", index = 11)
    private String jobLevel;

    @ExcelProperty(value = "标准职级", index = 12)
    private String jobGrade;

    @ExcelProperty(value = "编制人数", index = 13)
    private Integer headcountQuota;

    @ExcelProperty(value = "控编模式", index = 14)
    private String controlMode;

    @ExcelProperty(value = "在岗人数", index = 15)
    private Integer incumbentCount;

    @ExcelProperty(value = "在岗人员", index = 16)
    private String incumbentPersons;

    @ExcelProperty(value = "工作地点", index = 17)
    private String workPlace;

    @ExcelProperty(value = "工作内容", index = 18)
    private String workContent;

    @ExcelProperty(value = "职位状态", index = 19)
    private String positionStatus;

    @ExcelProperty(value = "职位说明书状态", index = 20)
    private String positionDescStatus;

    @ExcelProperty(value = "说明书ID", index = 21)
    private Long descriptionId;

    @ExcelProperty(value = "说明书版本", index = 22)
    private String descriptionVersion;

    @ExcelProperty(value = "说明书状态", index = 23)
    private String descriptionStatus;

    @ExcelProperty(value = "说明书生效日期", index = 24)
    private String descriptionEffectiveDate;

    @ExcelProperty(value = "说明书撤销日期", index = 25)
    private String descriptionRevocationDate;

    @ExcelProperty(value = "直接上级职位编码", index = 26)
    private String directSuperiorPositionCode;

    @ExcelProperty(value = "直接上级职位名称", index = 27)
    private String directSuperiorPositionName;

    @ExcelProperty(value = "直接下级职位编码", index = 28)
    private String directSubordinatePositionCodes;

    @ExcelProperty(value = "直接下级职位名称", index = 29)
    private String directSubordinatePositionNames;

    @ExcelProperty(value = "职位定位", index = 30)
    private String positionPurpose;

    @ExcelProperty(value = "职位职责", index = 31)
    private String responsibilities;

    @ExcelProperty(value = "主要工作依据", index = 32)
    private String basisList;

    @ExcelProperty(value = "协调内容", index = 33)
    private String coordinationList;

    @ExcelProperty(value = "工作权限", index = 34)
    private String authorityList;

    @ExcelProperty(value = "主要考核方向", index = 35)
    private String assessmentList;

    @ExcelProperty(value = "能力要求", index = 36)
    private String competencyList;

    @ExcelProperty(value = "工作环境", index = 37)
    private String workEnvironment;

    @ExcelProperty(value = "任职资格", index = 38)
    private String qualification;

    @ExcelProperty(value = "所需知识", index = 39)
    private String requiredKnowledge;

    @ExcelProperty(value = "附件fileId", index = 40)
    private String attachmentFileIds;

    @ExcelProperty(value = "附件名称", index = 41)
    private String attachmentNames;
}
