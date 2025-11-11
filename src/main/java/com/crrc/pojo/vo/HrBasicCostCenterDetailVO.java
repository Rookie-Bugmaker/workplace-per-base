package com.crrc.pojo.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableField;
import com.crrc.annotation.QueryMethod;
import com.crrc.entity.HrEmpBasic;
import com.crrc.enums.QueryType;
import com.crrc.pojo.dto.HrEmpBasicDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author yukun
 */
@Data
public class HrBasicCostCenterDetailVO extends HrEmpBasicDTO {
    String costCenterName;
    String costCenterCode;
    /**
     * 其实是公司名(编码)
     */
    @QueryMethod(method = QueryType.EQ)
    private String collection;

    @ExcelProperty(value = {"表集合"}, index = 0)
    @Schema(description = "表集合")
    @TableField(exist = false)
    private String collectionName;
}
