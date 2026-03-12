package com.crrc.entity;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import com.crrc.annotation.QueryMethod;
import com.crrc.converter.DictConverter;
import com.crrc.enums.QueryType;
import com.crrc.pojo.dto.SysFileDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * 荣誉信息表
 * </p>
 *
 * @author shenshuo
 * @since 2026-03-10 11:38:02
 */
@ExcelIgnoreUnannotated
@TableName("hr_emp_honor")
@Schema(description = "荣誉信息表")
@Data
public class HrEmpHonor extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 2031212962376908800L;

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
    @ExcelProperty(value = {"获奖日期"}, index = 2)
    @Schema(description = "获奖日期")
    private LocalDate awardDate;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"称号类别"}, index = 3)
    @Schema(description = "称号类别")
    private String titleType;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"称号级别"}, index = 4)
    @Schema(description = "称号级别")
    private String titleLevel;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"获奖年度"}, index = 5)
    @Schema(description = "获奖年度")
    private String awardYear;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"荣誉名称"}, index = 6)
    @Schema(description = "荣誉名称")
    private String honorName;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"集体/个人"}, index = 7)
    @Schema(description = "集体/个人")
    private String groupOrPersonal;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"授奖单位"}, index = 8)
    @Schema(description = "授奖单位")
    private String awardingUnit;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"是否为负责人"}, index = 9)
    @Schema(description = "是否为负责人")
    private String isPersonInCharge;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"荣誉类别"}, index = 10)
    @Schema(description = "荣誉类别")
    private String honorType;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"奖励类别"}, index = 11)
    @Schema(description = "奖励类别")
    private String rewardType;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"获奖原因"}, index = 12)
    @Schema(description = "获奖原因")
    private String awardReason;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"归口单位"}, index = 13)
    @Schema(description = "归口单位")
    private String responsibleUnit;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"个人排名"}, index = 14)
    @Schema(description = "个人排名")
    private String personalRank;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"取得方式"}, index = 15)
    @Schema(description = "取得方式")
    private String obtainMethod;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"所属类别"}, index = 16)
    @Schema(description = "所属类别")
    private String category;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"资金支持情况"}, index = 17)
    @Schema(description = "资金支持情况")
    private String fundingSupportStatus;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"政策兑现情况"}, index = 18)
    @Schema(description = "政策兑现情况")
    private String policyImplementationStatus;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"附件挂载Id"}, index = 19)
    @Schema(description = "附件挂载Id")
    private String fileHeaderId;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"是否存在正在被修改的状态"}, index = 20)
    @Schema(description = "是否存在正在被修改的状态，0为否，1为是")
    private String isUpdating;

    @Schema(description = "备注")
    @TableField(value = "remark")
    private String remark;

}
