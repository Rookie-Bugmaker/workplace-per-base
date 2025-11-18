package com.crrc.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class SysEmployeeDTO {
    @Schema(description = "入职日期", example = "2020-01-01 00:00:00")
    private String inductionDate;

    @Schema(description = "离职日期（未离职为null）", example = "null")
    private String quitDate;

    @Schema(description = "所属部门列表")
    private List<SysDeptDTO> deptList;

    @Schema(description = "性别（M:男 F:女）", example = "M")
    private String gender;

    @Schema(description = "手机号", example = "18875012385")
    private String phone;

    @Schema(description = "工号", example = "1008611")
    private String jobNo;

    @Schema(description = "员工姓名", example = "谭永强")
    private String fullName;

    @Schema(description = "人员ID", example = "409416598202908672")
    private String personId;

    @Schema(description = "邮箱", example = "982186883@qq.com")
    private String email;

    @Schema(description = "状态（1:在职 0:离职）", example = "1")
    private Integer status;
}
