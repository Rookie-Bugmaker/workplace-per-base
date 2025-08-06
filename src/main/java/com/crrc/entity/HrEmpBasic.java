package com.crrc.entity;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.crrc.annotation.QueryMethod;
import com.crrc.enums.QueryType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * <p>
 * 员工基本信息表
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@ExcelIgnoreUnannotated
@Accessors(chain = false)
@TableName("hr_emp_basic")
@Schema(description = "员工基本信息表")
@Data
@NoArgsConstructor
public class HrEmpBasic extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1949021422334177280L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    @Schema(description = "备注", example = "demo")
    @QueryMethod(method = QueryType.LIKE)
    @TableField(exist = false)
    private String remark;

    @Schema(description = "所属部门id", example = "1")
    @TableField(exist = false)
    private Long orgId;

    @TableLogic(value = "0")
    @Schema(description = "删除", example = "false")
    private Integer deleted;

    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"工号"}, index = 1)
    @Schema(description = "工号")
    private String userCode;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"性别"}, index = 2)
    @Schema(description = "性别")
    private String gender;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"民族"}, index = 3)
    @Schema(description = "民族")
    private String nation;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"籍贯名称"}, index = 4)
    @Schema(description = "籍贯名称")
    private String nativePlaceName;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"国籍"}, index = 5)
    @Schema(description = "国籍")
    private String nationality;
    
    @QueryMethod(method = QueryType.BETWEEN, start = "birthDateStartTime", end = "birthDateEndTime")
    @ExcelProperty(value = {"出生日期"}, index = 6)
    @Schema(description = "出生日期")
    private LocalDate birthDate;
    
  	@TableField(exist = false)
    @Schema(description = "出生日期-开始时间", example = "2024-01-01 00:00:00")
	private LocalDateTime birthDateStartTime;
    
    @TableField(exist = false)
    @Schema(description = "出生日期-结束时间", example = "2024-01-01 00:00:00")
    private LocalDateTime birthDateEndTime;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"年龄"}, index = 7)
    @Schema(description = "年龄")
    private Integer age;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"出生地"}, index = 8)
    @Schema(description = "出生地")
    private String birthPlace;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"政治面貌"}, index = 9)
    @Schema(description = "政治面貌")
    private String political;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"姓名"}, index = 10)
    @Schema(description = "姓名")
    private String name;
    
    @QueryMethod(method = QueryType.BETWEEN, start = "partyJoinDateStartTime", end = "partyJoinDateEndTime")
    @ExcelProperty(value = {"加入党组织时间"}, index = 11)
    @Schema(description = "加入党组织时间")
    private LocalDate partyJoinDate;
    
  	@TableField(exist = false)
    @Schema(description = "加入党组织时间-开始时间", example = "2024-01-01 00:00:00")
	private LocalDateTime partyJoinDateStartTime;
    
    @TableField(exist = false)
    @Schema(description = "加入党组织时间-结束时间", example = "2024-01-01 00:00:00")
    private LocalDateTime partyJoinDateEndTime;
    
    @QueryMethod(method = QueryType.BETWEEN, start = "workDateStartTime", end = "workDateEndTime")
    @ExcelProperty(value = {"开始工作日期"}, index = 12)
    @Schema(description = "开始工作日期")
    private LocalDate workDate;
    
  	@TableField(exist = false)
    @Schema(description = "开始工作日期-开始时间", example = "2024-01-01 00:00:00")
	private LocalDateTime workDateStartTime;
    
    @TableField(exist = false)
    @Schema(description = "开始工作日期-结束时间", example = "2024-01-01 00:00:00")
    private LocalDateTime workDateEndTime;
    
    @QueryMethod(method = QueryType.BETWEEN, start = "entryDateStartTime", end = "entryDateEndTime")
    @ExcelProperty(value = {"入司日期"}, index = 13)
    @Schema(description = "入司日期")
    private LocalDate entryDate;
    
  	@TableField(exist = false)
    @Schema(description = "入司日期-开始时间", example = "2024-01-01 00:00:00")
	private LocalDateTime entryDateStartTime;
    
    @TableField(exist = false)
    @Schema(description = "入司日期-结束时间", example = "2024-01-01 00:00:00")
    private LocalDateTime entryDateEndTime;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"员工来源"}, index = 14)
    @Schema(description = "员工来源")
    private String empSource;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"婚姻状况"}, index = 15)
    @Schema(description = "婚姻状况")
    private String maritalStatus;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"身份类型"}, index = 16)
    @Schema(description = "身份类型")
    private String identityType;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"职务层级"}, index = 17)
    @Schema(description = "职务层级")
    private String empJob;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"职级层级"}, index = 18)
    @Schema(description = "职级层级")
    private String empRank;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"校招届数"}, index = 19)
    @Schema(description = "校招届数")
    private String campusPeriod;


	@QueryMethod(method = QueryType.EQ)
	@ExcelProperty(value = {"入职日期"}, index = 20)
	@Schema(description = "入职日期")
	private LocalDate hireDate;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"籍贯编码"}, index = 4)
    @Schema(description = "籍贯编码")
    private String nativePlaceCode;
}
