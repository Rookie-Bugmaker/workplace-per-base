package com.crrc.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;

import com.crrc.annotation.ExcelMethod;
import com.crrc.annotation.QueryMethod;
import com.crrc.enums.ExcelType;
import com.crrc.enums.QueryType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 * 额外人员信息关联表
 * </p>
 *
 * @author mayiyao
 * @since 2026-01-15 13:44:29
 */
@Accessors(chain = false)
@TableName("exception_case_person_relation")
@Schema(description = "额外人员信息关联表")
@Data
public class ExceptionCasePersonRelation extends BaseEntity implements Serializable {

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    @TableLogic(value = "0")
    @Schema(description = "删除", example = "false")
    private Integer deleted;

    @QueryMethod(method = QueryType.EQ)
    @ExcelMethod(method = ExcelType.BOTH, header = "借用原因", sorted = 0)
    @Schema(description = "借用原因", example = "")
    private String reason;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelMethod(method = ExcelType.BOTH, header = "借用类型，外部派入/借用", sorted = 1)
    @Schema(description = "借用类型，外部派入/借用", example = "")
    private String type;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelMethod(method = ExcelType.BOTH, header = "流程实例id", sorted = 2)
    @Schema(description = "流程实例id", example = "")
    private String processInstanceId;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelMethod(method = ExcelType.BOTH, header = "流程模板id", sorted = 3)
    @Schema(description = "流程模板id", example = "")
    private String wfid;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelMethod(method = ExcelType.BOTH, header = "审批状态 ", sorted = 4)
    @Schema(description = "审批状态 ", example = "")
    private String status;

}
