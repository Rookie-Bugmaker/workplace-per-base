package com.crrc.pojo.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.baomidou.mybatisplus.annotation.TableName;
import com.crrc.annotation.WordTemplateRequire;
import com.crrc.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * 借调（借用）人员审批表表头
 * </p>
 *
 * @author mayiyao
 * @since 2026-03-04 16:26:51
 */
@ExcelIgnoreUnannotated
@TableName("personnel_secondment_request_relation")
@Schema(description = "借调（借用）人员需求计划申请表表头")
@Data
public class BorrowedPersonnelApprovalFormRelationTemplateVO extends BaseEntity {

    @WordTemplateRequire
    private String year;

    @WordTemplateRequire
    private String month;

    @WordTemplateRequire
    private String day;

    @WordTemplateRequire
    private String borrowingUnitName;

    @WordTemplateRequire
    private String lendingUnitName;

    @WordTemplateRequire
    private String startYear;

    @WordTemplateRequire
    private String startMonth;

    @WordTemplateRequire
    private String startDay;

    @WordTemplateRequire
    private String endYear;

    @WordTemplateRequire
    private String endMonth;

    @WordTemplateRequire
    private String endDay;

    @WordTemplateRequire
    private String workScopeRequirements;

    @WordTemplateRequire
    private String settlementMethod;
}
