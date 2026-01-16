package com.crrc.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.crrc.annotation.ExcelMethod;
import com.crrc.annotation.QueryMethod;
import com.crrc.enums.ExcelType;
import com.crrc.enums.QueryType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 * 额外人员信息表
 * </p>
 *
 * @author mayiyao
 * @since 2026-01-15 13:44:29
 */
@Accessors(chain = false)
@TableName("exception_case_person")
@Schema(description = "额外人员信息表")
@Data
public class ExceptionCasePerson extends BaseEntity implements Serializable {


    @Schema(description = "主键")
    @TableId(value = "id")
    private String id;

    @TableLogic(value = "0")
    @Schema(description = "删除", example = "false")
    private Integer deleted;

    @QueryMethod(method = QueryType.EQ)
    @ExcelMethod(method = ExcelType.BOTH, header = "数据外键，对应exception_case_person_relation的id", sorted = 0)
    @Schema(description = "数据外键，对应exception_case_person_relation的id", example = "")
    private String relationId;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelMethod(method = ExcelType.BOTH, header = "姓名", sorted = 1)
    @Schema(description = "姓名", example = "")
    private String name;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelMethod(method = ExcelType.BOTH, header = "档案号", sorted = 2)
    @Schema(description = "档案号", example = "")
    private String userCode;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelMethod(method = ExcelType.BOTH, header = "原属公司", sorted = 3)
    @Schema(description = "原属公司", example = "")
    private String formerCompany;

    @QueryMethod(method = QueryType.EQ)
    @ExcelMethod(method = ExcelType.BOTH, header = "原属公司编码")
    @Schema(description = "原属公司编码")
    private String formerCompanyCode;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelMethod(method = ExcelType.BOTH, header = "原属部门", sorted = 4)
    @Schema(description = "原属部门", example = "")
    private String formerDept;

    @QueryMethod(method = QueryType.EQ)
    @ExcelMethod(method = ExcelType.BOTH, header = "原属部门编码")
    @Schema(description = "原属部门编码")
    private String formerDeptCode;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelMethod(method = ExcelType.BOTH, header = "新属公司", sorted = 5)
    @Schema(description = "新属公司", example = "")
    private String newCompany;

    @QueryMethod(method = QueryType.EQ)
    @ExcelMethod(method = ExcelType.BOTH, header = "新属公司编码")
    @Schema(description = "新属公司编码")
    private String newCompanyCode;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelMethod(method = ExcelType.BOTH, header = "新属部门", sorted = 6)
    @Schema(description = "新属部门", example = "")
    private String newDept;

    @QueryMethod(method = QueryType.EQ)
    @ExcelMethod(method = ExcelType.BOTH, header = "新属部门编码")
    @Schema(description = "新属部门编码")
    private String newDeptCode;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelMethod(method = ExcelType.BOTH, header = "新属职位，手动输入", sorted = 7)
    @Schema(description = "新属职位，手动输入", example = "")
    private String position;

	@QueryMethod(method = QueryType.EQ)
    @ExcelMethod(method = ExcelType.BOTH, header = "开始时间", sorted = 8)
    @Schema(description = "开始时间", example = "")
    private LocalDateTime startDate;

	@QueryMethod(method = QueryType.EQ)
    @ExcelMethod(method = ExcelType.BOTH, header = "结束时间", sorted = 9)
    @Schema(description = "结束时间", example = "")
    private LocalDateTime endDate;

}
