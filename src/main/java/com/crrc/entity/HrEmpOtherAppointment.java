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

import java.io.Serializable;
import java.time.LocalDate;

/**
 * <p>
 * 其他任职信息
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@ExcelIgnoreUnannotated
@Accessors(chain = false)
@TableName("hr_emp_other_appointment")
@Schema(description = "其他任职信息")
@Data
@NoArgsConstructor
public class HrEmpOtherAppointment extends BaseEntity implements Serializable {

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    @TableLogic(value = "0")
    @Schema(description = "删除", example = "false")
    private Integer deleted;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"开始日期"})
    @Schema(description = "开始日期")
    private LocalDate startDate;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"预计结束日期"})
    @Schema(description = "预计结束日期")
    private LocalDate expectedEndDate;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"结束日期"})
    @Schema(description = "结束日期")
    private LocalDate endDate;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"人员类别"})
    @Schema(description = "人员类别")
    private String personnelCategory;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"组织"})
    @Schema(description = "组织")
    private String org;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"部门"})
    @Schema(description = "部门")
    private String dept;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"组织单元"})
    @Schema(description = "组织单元")
    private String orgUnit;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"档案关系弱化"})
    @Schema(description = "档案关系弱化")
    private String archiveRelationWeaken;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"职位"})
    @Schema(description = "职位")
    private String position;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"其他任职类型"})
    @Schema(description = "其他任职类型")
    private String otherAppointmentType;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"集团外任职信息"})
    @Schema(description = "集团外任职信息")
    private String externalApp;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"职务类别"})
    @Schema(description = "职务类别")
    private String positionCategory;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"在岗状态"})
    @Schema(description = "在岗状态")
    private String dutyStatus;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"组织路径"})
    @Schema(description = "组织路径")
    private String orgPath;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"部门路径"})
    @Schema(description = "部门路径")
    private String deptPath;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"工号"})
    @Schema(description = "工号")
    private String userCode;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"职种编码"})
    @Schema(description = "职种编码")
    private String jobType;
}
