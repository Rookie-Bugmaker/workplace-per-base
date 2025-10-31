package com.crrc.pojo.vo;

import com.crrc.entity.HrEmpBasic;
import lombok.Data;

/**
 * @author yukun
 */
@Data
public class HrBasicCostCenterDetailVO extends HrEmpBasic {
    String costCenterName;
    String costCenterCode;
}
