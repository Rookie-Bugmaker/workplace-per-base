package com.crrc.pojo.dto;

import com.crrc.entity.HrEmpBasic;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HrEmpBasicDTO extends HrEmpBasic{

    @Schema(description = "最高学历")
    public String education;

    @Schema(description = "最高学位")
    public String degree;

    @Schema(description = "所学专业")
    public String major;

    @Schema(description = "专业技术资格")
    public String skillName;

    @Schema(description = "技能等级")
    public String skillLevel;

    @Schema(description = "学校名称")
    public String schoolName;

    @Schema(description = "证件类型")
    public String certiType;

    @Schema(description = "证件号码")
    public String certiId;

    @Schema(description = "户口性质")
    public String houseHoldType;

    @Schema(description = "户口所在地")
    public String houseHoldLocation;

    @Schema(description = "户口详细地址")
    public String houseHoldLocationDetail;

    @Schema(description = "移动电话")
    public String mobilePhone;

    @Schema(description = "办公邮箱")
    public String workEmail;

    @Schema(description = "个人邮箱")
    public String personalEmail;

    @Schema(description = "家庭地址")
    public String address;

    @Schema(description = "健康状况")
    public String healthStatus;

}
