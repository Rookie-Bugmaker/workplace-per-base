package com.crrc.pojo.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.crrc.entity.HrEmpBasic;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 员工基本信息导出 VO
 * 继承 HrEmpBasic，额外包含职位、证件、户口、通讯、健康等关联字段
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Schema(description = "员工基本信息导出VO")
public class HrEmpBasicExportVO extends HrEmpBasic {

    @ExcelProperty(value = {"职位"})
    @Schema(description = "职位")
    private String position;

    @ExcelProperty(value = {"证件类型"})
    @Schema(description = "证件类型")
    private String certiType;

    @ExcelProperty(value = {"证件号码"})
    @Schema(description = "证件号码")
    private String certiId;

    @ExcelProperty(value = {"户口性质"})
    @Schema(description = "户口性质")
    private String houseHoldType;

    @ExcelProperty(value = {"户口所在地"})
    @Schema(description = "户口所在地")
    private String houseHoldLocation;

    @ExcelProperty(value = {"户口详细地址"})
    @Schema(description = "户口详细地址")
    private String houseHoldLocationDetail;

    @ExcelProperty(value = {"移动电话"})
    @Schema(description = "移动电话")
    private String mobilePhone;

    @ExcelProperty(value = {"工作邮箱"})
    @Schema(description = "工作邮箱")
    private String workEmail;

    @ExcelProperty(value = {"个人邮箱"})
    @Schema(description = "个人邮箱")
    private String personalEmail;

    @ExcelProperty(value = {"家庭地址"})
    @Schema(description = "家庭地址")
    private String address;

    @ExcelProperty(value = {"健康状况"})
    @Schema(description = "健康状况")
    private String healthStatus;
}
