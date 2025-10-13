package com.crrc.pojo.dto;

import com.crrc.entity.HrEmpBasic;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用于返回个人信息里面的基本信息
 */
@Data
@NoArgsConstructor
public class HrEmpBasicDTO extends HrEmpBasic{

    @Schema(description = "最高学历")
    private String education;

    @Schema(description = "最高学位")
    private String degree;

    @Schema(description = "所学专业")
    private String major;

    @Schema(description = "专业技术资格")
    private String quaId;

    @Schema(description = "技能等级")
    private String skillLevel;

    @Schema(description = "学校")
    private String schoolName;

    @Schema(description = "证件类型")
    private String certiType;

    @Schema(description = "证件号码")
    private String certiId;

    @Schema(description = "户口性质")
    private String houseHoldType;

    @Schema(description = "户口所在地")
    private String houseHoldLocation;

    @Schema(description = "户口详细地址")
    private String houseHoldLocationDetail;

    @Schema(description = "移动电话")
    private String mobilePhone;

    @Schema(description = "办公邮箱")
    private String workEmail;

    @Schema(description = "个人邮箱")
    private String personalEmail;

    @Schema(description = "家庭地址")
    private String address;

    @Schema(description = "健康状况")
    private String healthStatus;

    @Schema(description = "流程模板id")
    private Long wfId;

    @Schema(description = "流程实例id")
    private String processInstanceId;

}
