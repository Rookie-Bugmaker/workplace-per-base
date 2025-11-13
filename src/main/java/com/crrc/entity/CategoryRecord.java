package com.crrc.entity;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@ExcelIgnoreUnannotated
@Accessors(chain = false)
@TableName("category_record")
@Schema(description = "字典表")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryRecord extends BaseEntity {
    @TableLogic(value = "0")
    @Schema(description = "删除", example = "false")
    private Integer deleted;

    private String userCode;

    private String oldCategory1;

    private String oldCategory4;

    private String oldCategory7;

    private String oldCategory8;

    private String newCategory1;

    private String newCategory4;

    private String newCategory7;

    private String newCategory8;
}
