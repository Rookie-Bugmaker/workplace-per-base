package com.crrc.pojo.dto;

import com.crrc.entity.HrEmpCareerPath;
import com.crrc.entity.HrEmpPenalties;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class HrEmpPenaltiesDTO {
    @NotBlank(message = "员工编码不能为空")
    private String userCode;

    @Valid
    @NotEmpty(message = "职业发展通道列表不能为空")
    private List<HrEmpPenalties> list;
}