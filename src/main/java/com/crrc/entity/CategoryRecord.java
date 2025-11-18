package com.crrc.entity;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
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
@NoArgsConstructor
public class CategoryRecord extends BaseEntity {
    private CategoryRecord(String userCode, String oldCategory1, String oldCategory4, String oldCategory7) {

    }

    @TableId(type = IdType.AUTO)
    private Long id;

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

    /**
     * 根据userCode，旧人员分类1478，新人员分类1478获得一个日志记录实体
     * @param userCode 记录的userCode
     * @param oldRecord 包含旧人员分类1478记录的HrEmpBasic实体
     * @param newRecord 包含新人员分类1478记录的HrEmpBasic实体
     * @return 创建的日志记录实体
     */
    public static CategoryRecord of(String userCode, HrEmpBasic oldRecord, HrEmpBasic newRecord) {
        CategoryRecord categoryRecord = new CategoryRecord();
        categoryRecord.userCode = userCode;
        categoryRecord.oldCategory1 = oldRecord.getCategory1();
        categoryRecord.oldCategory4 = oldRecord.getCategory4();
        categoryRecord.oldCategory7 = oldRecord.getCategory7();
        categoryRecord.oldCategory8 = oldRecord.getCategory8();

        categoryRecord.newCategory1 = newRecord.getCategory1();
        categoryRecord.newCategory4 = newRecord.getCategory4();
        categoryRecord.newCategory7 = newRecord.getCategory7();
        categoryRecord.newCategory8 = newRecord.getCategory8();
        return categoryRecord;
    }
}
