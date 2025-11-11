package com.crrc.pojo.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.crrc.annotation.QueryMethod;
import com.crrc.enums.QueryType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@ExcelIgnoreUnannotated
@Data
@Accessors(chain=false)
@AllArgsConstructor
@NoArgsConstructor
public class CostCenterImportVO {
    /** 同时指定列名与列下标，既可按表头名匹配，也能在无表头时按第0列取值 */
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = "档案号", index = 0)
    @Schema(description = "档案号")
    private String archiveNo;
}
