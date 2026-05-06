package com.crrc.entity;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.*;
import com.crrc.annotation.QueryMethod;
import com.crrc.enums.QueryType;
import com.crrc.pojo.dto.SysFileDTO;
import com.crrc.utils.DiffMarkable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 员工教育信息表
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@ExcelIgnoreUnannotated
@Accessors(chain = false)
@TableName("hr_emp_education")
@Schema(description = "员工教育信息表")
@Data
@NoArgsConstructor
public class HrEmpEducation extends BaseEntity implements Serializable, DiffMarkable {

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    @TableLogic(value = "0")
    @Schema(description = "删除", example = "false")
    private Integer deleted;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"工号"})
    @Schema(description = "工号")
    private String userCode;

    @ExcelProperty(value = {"入学时间"})
    @Schema(description = "入学时间")
    private LocalDate schoolEntryDate;

    @ExcelProperty(value = {"毕业时间"})
    @Schema(description = "毕业时间")
    private LocalDate graduationDate;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"学校名称"})
    @Schema(description = "学校名称")
    private String schoolName;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"专业名称"})
    @Schema(description = "专业名称")
    @TableField(exist = false)
    private String major;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"学院"})
    @Schema(description = "学院")
    private String college;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"是否主专业"})
    @Schema(description = "是否主专业")
    private String isMajor;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"是否全日制"})
    @Schema(description = "是否全日制")
    private String isFullTime;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"学历"})
    @Schema(description = "学历")
    private String education;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"学位"})
    @Schema(description = "学位")
    private String degree;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"是否最高学位"})
    @Schema(description = "是否最高学位")
    private String isHighestDegree;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"是否最高学历"})
    @Schema(description = "是否最高学历")
    private String isHighestEducation;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"学业完成情况"})
    @Schema(description = "学业完成情况")
    private String completionStatus;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"学位专业"})
    @Schema(description = "学位专业")
    private String degreeMajor;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"学校编码"})
    @Schema(description = "学校编码")
    private String schoolCode;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"专业编码"})
    @Schema(description = "专业编码")
    private String majorCode;

    @QueryMethod(method = QueryType.EQ)
    @Schema(description = "是否存在正在被修改的状态，0为否，1为是")
    private String isUpdating;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"附件挂载Id"})
    @Schema(description = "附件挂载Id")
    private String fileHeaderId;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"手动输入的学校名称"})
    @Schema(description = "手动输入的学校名称")
    private String inputSchoolName;

    @Schema(description = "通过fileHeaderId，查询回的文件列表")
    @TableField(exist = false)
    private List<SysFileDTO> files;

    @Schema(description = "变动字段统计")
    @TableField(exist = false)
    private List<String> markList;
}
