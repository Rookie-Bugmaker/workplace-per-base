package com.crrc.entity;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import com.crrc.annotation.QueryMethod;
import com.crrc.converter.DictConverter;
import com.crrc.enums.QueryType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

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
public class HrEmpPatent extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 2031212656209494016L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    @TableLogic(value = "0")
    @Schema(description = "删除", example = "false")
    private Integer deleted;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"工号"}, index = 1)
    @Schema(description = "工号")
    private String userCode;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"授权日期"}, index = 2)
    @Schema(description = "授权日期")
    private LocalDate authorizationdate;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"专利/软著名称"}, index = 3)
    @Schema(description = "专利/软著名称")
    private String patentName;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"专利类型"}, index = 4)
    @Schema(description = "专利类型")
    private String patentType;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"专利级别"}, index = 5)
    @Schema(description = "专利级别")
    private String patentLevel;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"专利号"}, index = 6)
    @Schema(description = "专利号")
    private String patentNo;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"是否获奖"}, index = 7)
    @Schema(description = "是否获奖")
    private String isAwarded;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"获奖日期"}, index = 8)
    @Schema(description = "获奖日期")
    private LocalDate awardDate;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"奖励等次"}, index = 9)
    @Schema(description = "奖励等次")
    private String awardGrade;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"获奖级别"}, index = 10)
    @Schema(description = "获奖级别")
    private String awardLevel;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"个人排名"}, index = 11)
    @Schema(description = "个人排名")
    private String personRank;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"排名总人数"}, index = 12)
    @Schema(description = "排名总人数")
    private Integer totalParticipants;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"附件挂载Id"}, index = 13)
    @Schema(description = "附件挂载Id")
    private String fileHeaderId;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"鉴定单位"}, index = 14)
    @Schema(description = "鉴定单位")
    private String appraisalAuthority;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"创作时间"}, index = 15)
    @Schema(description = "创作时间")
    private LocalDate creationDate;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"专利期限"}, index = 16)
    @Schema(description = "专利期限")
    private String patentTerm;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"状态"}, index = 17)
    @Schema(description = "状态")
    private String status;

}
