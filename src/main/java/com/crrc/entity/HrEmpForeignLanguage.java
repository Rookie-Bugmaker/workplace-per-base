package com.crrc.entity;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDateTime;
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
 * 外语能力信息表
 * </p>
 *
 * @author system
 * @since 2026-04-16
 */
@ExcelIgnoreUnannotated
@TableName("hr_emp_foreign_language")
@Schema(description = "外语能力信息表")
@Data
@Accessors(chain = false)
public class HrEmpForeignLanguage extends BaseEntity implements Serializable, DiffMarkable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    @TableLogic(value = "0")
    @Schema(description = "删除", example = "false")
    private Integer deleted;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"档案号"})
    @Schema(description = "档案号")
    private String userCode;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"语种"})
    @Schema(description = "语种")
    private String language;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"证书名称"})
    @Schema(description = "证书名称")
    private String certificateName;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"证书编号"})
    @Schema(description = "证书编号")
    private String certificateNo;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"分数/等级"})
    @Schema(description = "分数/等级")
    private String examScore;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"考试时间"})
    @Schema(description = "考试时间")
    private LocalDateTime examDate;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"证书取得时间"})
    @Schema(description = "证书取得时间")
    private LocalDateTime certificateDate;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"附件"})
    @Schema(description = "附件")
    private String attachment;

    @QueryMethod(method = QueryType.EQ)
    @Schema(description = "是否存在正在被修改的状态，0为否，1为是")
    private String isUpdating;

    @Schema(description = "变动字段统计")
    @TableField(exist = false)
    private List<String> markList;

    @Schema(description = "语种翻译值")
    @TableField(exist = false)
    private String languageValue;

    @Schema(description = "证书名称翻译值")
    @TableField(exist = false)
    private String certificateNameValue;

    @Schema(description = "证书名称手写内容（仅当证书名称码值为10时允许填写）")
    @TableField("certificate_name_write")
    private String certificateNameWrite;

}
