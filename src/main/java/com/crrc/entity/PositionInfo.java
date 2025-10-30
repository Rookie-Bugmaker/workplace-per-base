package com.crrc.entity;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.crrc.annotation.QueryMethod;
import com.crrc.enums.QueryType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

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
    @ExcelProperty(value = {"所属组织单元"}, index = 1)
    private String orgUnit;

    /** 工作中心 */
    @Schema(description = "工作中心")
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"工作中心"}, index = 2)
    private String workCenter;

    /** 编码 */
    @Schema(description = "编码")
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"编码"}, index = 3)
    private String code;

    /** 名称 */
    @Schema(description = "名称")
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"名称"}, index = 4)
    private String name;

    /** 生效日期 */
    @Schema(description = "生效日期")
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"生效日期"}, index = 5)
    private Date effectiveDate;

    /** 关联职务 */
    @Schema(description = "关联职务")
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"关联职务"}, index = 6)
    private String relatedPost;

    /** 职种 */
    @Schema(description = "职种")
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"职种"}, index = 7)
    private String jobType;

    /** 部门代码 */
    @Schema(description = "部门代码")
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"部门代码"}, index = 8)
    private String deptCode;

    /** 部门名称 */
    @Schema(description = "部门名称")
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"部门名称"}, index = 9)
    private String deptName;

    /** 上级职位编码 */
    @Schema(description = "上级职位编码")
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"上级职位编码"}, index = 10)
    private String parentPositionCode;

    /** 上级职位名称 */
    @Schema(description = "上级职位名称")
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"上级职位名称"}, index = 11)
    private String parentPositionName;
}
