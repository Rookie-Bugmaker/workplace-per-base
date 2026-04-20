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
    @ExcelProperty(value = {"所属组织单元"})
    private String orgUnit;

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
}
