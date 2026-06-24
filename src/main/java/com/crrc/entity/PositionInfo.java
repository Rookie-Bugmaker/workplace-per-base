package com.crrc.entity;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.*;
import com.crrc.annotation.QueryMethod;
import com.crrc.enums.QueryType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDate;

/**
 * 职位信息实体类
 * @author yukun
 */
@Data
@TableName("position_info")
@Accessors(chain = false)
@Schema(description = "职位信息")
@NoArgsConstructor
@ExcelIgnoreUnannotated
public class PositionInfo extends BaseEntity {
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableLogic(value = "0")
    private Integer deleted;

    /** 所属组织单元 */
    @Schema(description = "所属组织单元")
    @QueryMethod(method = QueryType.EQ)
    private String orgUnit;

    /** 所属组织单元 */
    @TableField(exist = false)
    @ExcelProperty(value = {"所属组织单元"})
    private String orgUnitValue;

    /** 工作中心 */
    @Schema(description = "工作中心")
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"工作中心"})
    private String workCenter;

    /** 编码 */
    @Schema(description = "编码")
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"编码"})
    private String code;

    /** 名称 */
    @Schema(description = "名称")
    @QueryMethod(method = QueryType.LIKE)
    @ExcelProperty(value = {"名称"})
    private String name;

    /** 生效日期 */
    @Schema(description = "生效日期")
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"生效日期"})
    private LocalDate effectiveDate;

    /** 关联职务 */
    @Schema(description = "关联职务")
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"关联职务"})
    private String relatedPost;

    /** 职种 */
    @Schema(description = "职种")
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"职种"})
    private String jobType;

    @TableField(exist = false)
    private String jobTypeValue;

    @TableField(exist = false)
    private String jobCategoryValue;

    @TableField(exist = false)
    private String jobGroupValue;

    /** 部门代码 */
    @Schema(description = "部门代码")
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"部门代码"})
    private String deptCode;

    /** 部门名称 */
    @Schema(description = "部门名称")
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"部门名称"})
    private String deptName;

    /** 上级职位编码 */
    @Schema(description = "上级职位编码")
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"上级职位编码"})
    private String parentPositionCode;

    /** 上级职位名称 */
    @Schema(description = "上级职位名称")
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"上级职位名称"})
    private String parentPositionName;

    @Schema(description = "标准岗级")
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"标准岗级"})
    private String jobGrade;

    /** 版本 */
    @Schema(description = "版本")
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"版本"})
    private String version;

    /** 撤销日期 */
    @Schema(description = "撤销日期")
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"撤销日期"})
    private LocalDate revocationDate;

    /** 职类 */
    @Schema(description = "职类")
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"职类"})
    private String jobCategory;

    /** 职群 */
    @Schema(description = "职群")
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"职群"})
    private String jobGroup;

    /** 职层 */
    @Schema(description = "职层")
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"职层"})
    private String jobLevel;

    /** 编制人数 */
    @Schema(description = "编制人数")
    @ExcelProperty(value = {"编制人数"})
    private Integer headcountQuota;

    /** 控编模式 */
    @Schema(description = "控编模式")
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"控编模式"})
    private String controlMode;

    /** 在岗人数快照 */
    @Schema(description = "在岗人数")
    @ExcelProperty(value = {"在岗人数"})
    private Integer incumbentCount;

    /** 在岗人员快照 */
    @Schema(description = "在岗人员")
    @ExcelProperty(value = {"在岗人员"})
    private String incumbentPersons;

    /** 工作地点 */
    @Schema(description = "工作地点")
    @QueryMethod(method = QueryType.LIKE)
    @ExcelProperty(value = {"工作地点"})
    private String workPlace;

    /** 工作内容 */
    @Schema(description = "工作内容")
    @QueryMethod(method = QueryType.LIKE)
    @ExcelProperty(value = {"工作内容"})
    private String workContent;

    /** 职位状态 */
    @Schema(description = "职位状态")
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"职位状态"})
    private String positionStatus;

    /** 职位说明书状态快照 */
    @Schema(description = "职位说明书状态")
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"职位说明书状态"})
    private String positionDescStatus;

    /** 职位编码规则扩展预留字段 */
    @Schema(description = "职位编码规则扩展预留字段")
    private String codeRuleExt;
}
