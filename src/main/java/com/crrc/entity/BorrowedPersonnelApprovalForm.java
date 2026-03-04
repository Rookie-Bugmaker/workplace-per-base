package com.crrc.entity;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.*;
import com.crrc.annotation.QueryMethod;
import com.crrc.enums.QueryType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * 借调（借用）人员审批明细表
 * </p>
 *
 * @author mayiyao
 * @since 2026-03-04 16:26:51
 */
@ExcelIgnoreUnannotated
@TableName("borrowed_personnel_approval_form")
@Schema(description = "借调（借用）人员审批明细表")
@Data
public class BorrowedPersonnelApprovalForm extends BaseEntity{

    @Schema(description = "主键")
    @TableId(value = "id")
    private String id;

    @TableLogic(value = "0")
    @Schema(description = "删除")
    private Integer deleted;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"数据外键，对应borrowed_personnel_approval_form_relation的id"}, index = 0)
    @Schema(description = "数据外键，对应borrowed_personnel_approval_form_relation的id")
    private String relationId;

    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"档案号"})
    @Schema(description = "档案号")
    private String userCode;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"姓名"})
    @Schema(description = "姓名")
    private String name;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"工种"})
    @Schema(description = "工种")
    private String jobType;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"备注"})
    @Schema(description = "工种")
    private String remark;
}
