package com.crrc.entity;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.*;
import com.crrc.annotation.QueryMethod;
import com.crrc.enums.QueryType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 * 字典表
 * </p>
 *
 * @author mayiyao
 * @since 2025-10-15 10:51:03
 */
@ExcelIgnoreUnannotated
@Accessors(chain = false)
@TableName("code_info")
@Schema(description = "字典表")
@Data
public class CodeInfo extends BaseEntity{

    /**
     * 注意，ID需要手动生成
     */
    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "租户id", example = "1")
    private Long tenantId;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"类型"}, index = 0)
    @Schema(description = "类型", example = "")
    private String type;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"类型名称"}, index = 1)
    @Schema(description = "类型名称", example = "")
    private String typeName;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"编码"}, index = 2)
    @Schema(description = "编码", example = "")
    private String code;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"码值"}, index = 3)
    @Schema(description = "码值", example = "")
    private String codeValue;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"表名"}, index = 4)
    @Schema(description = "表名", example = "")
    private String tableName;
}
