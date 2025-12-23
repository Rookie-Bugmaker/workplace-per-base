package com.crrc.entity;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import com.crrc.annotation.QueryMethod;
import com.crrc.converter.DictConverter;
import com.crrc.enums.QueryType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 *
 * @author yukun
 * @since 2025-12-19 11:03:43
 */
@ExcelIgnoreUnannotated
@Accessors(chain = false)
@TableName("position_setup_application")
@Data
@NoArgsConstructor
public class PositionSetupApplication extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 2001850905657630720L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableLogic(value = "0")
    @Schema(description = "删除", example = "false")
    private Integer deleted;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"标题"}, index = 0)
    @Schema(description = "标题", example = "")
    private String title;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"正文"}, index = 1)
    @Schema(description = "正文", example = "")
    private String content;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"附件"}, index = 2)
    @Schema(description = "附件", example = "")
    private String fileId;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"拟稿部门"}, index = 3)
    @Schema(description = "拟稿部门", example = "")
    private String dept;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"联系电话"}, index = 4)
    @Schema(description = "联系电话", example = "")
    private String phone;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"业务部门经办人档案号"}, index = 5)
    @Schema(description = "业务部门经办人档案号", example = "")
    private String handlingPersonnelCode;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"审批状态"}, index = 6)
    @Schema(description = "审批状态")
    private String approvalStatus;

    /**
     * 审批状态翻译
     */
    @TableField(exist = false)
    private String approvalStatusValue;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"流程模板Id"}, index = 7)
    @Schema(description = "流程模板Id")
    private String wfId;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"流程实例id"}, index = 8)
    @Schema(description = "流程实例id")
    private String processInstanceId;

    @Override
    public String toString() {
        return "PositionSetupApplication{" +
		"id=" + id +
		",title='" + title + "'" + 
		",content='" + content + "'" + 
		",fileId='" + fileId + "'" + 
		",dept='" + dept + "'" + 
		",phone='" + phone + "'" + 
		",handlingPersonnelCode='" + handlingPersonnelCode + "'" + 
        '}';
    }
}
