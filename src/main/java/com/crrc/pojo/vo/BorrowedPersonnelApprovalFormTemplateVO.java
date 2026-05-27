package com.crrc.pojo.vo;

import com.crrc.annotation.WordTemplateRequire;
import com.crrc.entity.BaseEntity;
import lombok.Data;

/**
 * <p>
 * 借调（借用）人员审批明细表
 * </p>
 *
 * @author mayiyao
 * @since 2026-03-04 16:26:51
 */
@Data
public class BorrowedPersonnelApprovalFormTemplateVO extends BaseEntity {

    @WordTemplateRequire(index = 0)
    private int index;

    @WordTemplateRequire(index = 1)
    private String userCode;

    @WordTemplateRequire(index = 2)
    private String name;

    @WordTemplateRequire(index = 3)
    private String jobType;

    @WordTemplateRequire(index = 4)
    private String remark;
}
