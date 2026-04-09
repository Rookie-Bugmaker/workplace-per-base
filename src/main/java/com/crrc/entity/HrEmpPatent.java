package com.crrc.entity;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

import com.crrc.annotation.QueryMethod;
import com.crrc.enums.QueryType;
import com.crrc.pojo.dto.SysFileDTO;
import com.crrc.utils.DiffMarkable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 * 专利信息表
 * </p>
 *
 * @author shenshuo
 * @since 2026-03-10 11:36:49
 */
@ExcelIgnoreUnannotated
@TableName("hr_emp_patent")
@Schema(description = "专利信息表")
@Data
@Accessors(chain = false)
public class HrEmpPatent extends BaseEntity implements Serializable, DiffMarkable {

    private static final long serialVersionUID = 2031212656209494016L;

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

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"授权日期"})
    @Schema(description = "授权日期")
    private LocalDate authorizationDate;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"专利/软著名称"})
    @Schema(description = "专利/软著名称")
    private String patentName;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"专利类型"})
    @Schema(description = "专利类型")
    private String patentType;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"专利级别"})
    @Schema(description = "专利级别")
    private String patentLevel;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"专利号"})
    @Schema(description = "专利号")
    private String patentNo;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"是否获奖"})
    @Schema(description = "是否获奖")
    private String isAwarded;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"获奖日期"})
    @Schema(description = "获奖日期")
    private LocalDate awardDate;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"奖励等次"})
    @Schema(description = "奖励等次")
    private String awardGrade;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"获奖级别"})
    @Schema(description = "获奖级别")
    private String awardLevel;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"个人排名"})
    @Schema(description = "个人排名")
    private String personalRank;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"排名总人数"})
    @Schema(description = "排名总人数")
    private String totalParticipants;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"附件挂载Id"})
    @Schema(description = "附件挂载Id")
    private String fileHeaderId;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"鉴定单位"})
    @Schema(description = "鉴定单位")
    private String appraisalAuthority;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"创作时间"})
    @Schema(description = "创作时间")
    private String creationDate;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"专利期限"})
    @Schema(description = "专利期限")
    private String patentTerm;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"状态"})
    @Schema(description = "状态")
    private String status;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"是否存在正在被修改的状态"})
    @Schema(description = "是否存在正在被修改的状态，0为否，1为是")
    private String isUpdating;

    @Schema(description = "通过fileHeaderId，查询回的文件列表")
    @TableField(exist = false)
    private List<SysFileDTO> files;

    @Schema(description = "变动字段统计")
    @TableField(exist = false)
    private List<String> markList;
}
